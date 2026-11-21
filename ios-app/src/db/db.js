// Local data layer.
// Native (iOS/Android): expo-sqlite — everything offline lives here:
// tracks catalog, lyrics, playlists, favorites and download records.
// Web: in-memory store with the same API (used for the web preview/QA build;
// the web platform has no bundled SQLite native module in this setup).
import { Platform } from 'react-native';

const IS_WEB = Platform.OS === 'web';
let SQLite = null;
if (!IS_WEB) {
  // eslint-disable-next-line global-require
  SQLite = require('expo-sqlite');
}

/* ------------------------------ SQLite (native) ------------------------------ */

let dbPromise = null;

export function getDb() {
  if (!dbPromise) {
    dbPromise = (async () => {
      const db = await SQLite.openDatabaseAsync('raptunes.db');
      await db.execAsync(`
        CREATE TABLE IF NOT EXISTS tracks (
          id TEXT PRIMARY KEY,
          artist TEXT NOT NULL,
          title TEXT NOT NULL,
          source TEXT,
          remote_url TEXT,
          local_path TEXT,
          lyrics TEXT,
          updated_at INTEGER DEFAULT 0
        );
        CREATE TABLE IF NOT EXISTS playlists (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL,
          created_at INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS playlist_tracks (
          playlist_id TEXT NOT NULL,
          track_id TEXT NOT NULL,
          position INTEGER NOT NULL,
          PRIMARY KEY (playlist_id, track_id)
        );
        CREATE INDEX IF NOT EXISTS idx_tracks_artist ON tracks(artist);
        CREATE TABLE IF NOT EXISTS favorites (
          track_id TEXT PRIMARY KEY,
          created_at INTEGER NOT NULL
        );
      `);
      try { await db.execAsync('ALTER TABLE tracks ADD COLUMN cover TEXT'); } catch (e) { /* already there */ }
      return db;
    })();
  }
  return dbPromise;
}

/* ------------------------------ In-memory (web) ------------------------------ */

const mem = {
  tracks: new Map(), // id -> row
  playlists: [], // {id, name, created_at}
  playlistTracks: [], // {playlist_id, track_id, position}
  favorites: new Map(), // track_id -> created_at
};

/* --------------------------------- API --------------------------------- */

export async function upsertTracks(tracks) {
  if (IS_WEB) {
    for (const t of tracks) {
      const prev = mem.tracks.get(t.id);
      mem.tracks.set(t.id, {
        id: t.id,
        artist: t.artist,
        title: t.title,
        source: t.source || null,
        remote_url: t.remoteUrl || null,
        local_path: prev ? prev.local_path : null,
        lyrics: t.lyrics || (prev ? prev.lyrics : null),
        cover: t.cover || (prev ? prev.cover : null),
        updated_at: Date.now(),
      });
    }
    return;
  }
  const db = await getDb();
  for (const t of tracks) {
    await db.runAsync(
      `INSERT INTO tracks (id, artist, title, source, remote_url, lyrics, cover, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(id) DO UPDATE SET title=excluded.title, remote_url=excluded.remote_url,
         lyrics=COALESCE(excluded.lyrics, tracks.lyrics), cover=COALESCE(excluded.cover, tracks.cover),
         updated_at=excluded.updated_at`,
      [t.id, t.artist, t.title, t.source || null, t.remoteUrl || null, t.lyrics || null, t.cover || null, Date.now()]
    );
  }
}

export async function listAllTracks() {
  if (IS_WEB) return [...mem.tracks.values()];
  const db = await getDb();
  return db.getAllAsync('SELECT * FROM tracks');
}

export async function listTracksByArtist(artist) {
  if (IS_WEB) {
    return [...mem.tracks.values()]
      .filter((t) => t.artist === artist)
      .sort((a, b) => b.updated_at - a.updated_at);
  }
  const db = await getDb();
  return db.getAllAsync('SELECT * FROM tracks WHERE artist = ? ORDER BY updated_at DESC', [artist]);
}

export async function markDownloaded(trackId, localPath) {
  if (IS_WEB) {
    const t = mem.tracks.get(trackId);
    if (t) t.local_path = localPath;
    return;
  }
  const db = await getDb();
  await db.runAsync('UPDATE tracks SET local_path = ? WHERE id = ?', [localPath, trackId]);
}

export async function createPlaylist(name) {
  const id = 'pl_' + Date.now().toString(36);
  if (IS_WEB) {
    mem.playlists.push({ id, name, created_at: Date.now() });
    return id;
  }
  const db = await getDb();
  await db.runAsync('INSERT INTO playlists (id, name, created_at) VALUES (?, ?, ?)', [id, name, Date.now()]);
  return id;
}

export async function listPlaylists() {
  if (IS_WEB) return [...mem.playlists].sort((a, b) => b.created_at - a.created_at);
  const db = await getDb();
  return db.getAllAsync('SELECT * FROM playlists ORDER BY created_at DESC');
}

export async function addTrackToPlaylist(playlistId, trackId) {
  if (IS_WEB) {
    if (mem.playlistTracks.some((r) => r.playlist_id === playlistId && r.track_id === trackId)) return;
    const maxp = mem.playlistTracks
      .filter((r) => r.playlist_id === playlistId)
      .reduce((m, r) => Math.max(m, r.position), -1);
    mem.playlistTracks.push({ playlist_id: playlistId, track_id: trackId, position: maxp + 1 });
    return;
  }
  const db = await getDb();
  const rows = await db.getAllAsync(
    'SELECT COALESCE(MAX(position), -1) AS maxp FROM playlist_tracks WHERE playlist_id = ?',
    [playlistId]
  );
  const next = (rows[0]?.maxp ?? -1) + 1;
  await db.runAsync(
    'INSERT OR IGNORE INTO playlist_tracks (playlist_id, track_id, position) VALUES (?, ?, ?)',
    [playlistId, trackId, next]
  );
}

export async function listPlaylistTracks(playlistId) {
  if (IS_WEB) {
    return mem.playlistTracks
      .filter((r) => r.playlist_id === playlistId)
      .sort((a, b) => a.position - b.position)
      .map((r) => mem.tracks.get(r.track_id))
      .filter(Boolean);
  }
  const db = await getDb();
  return db.getAllAsync(
    `SELECT t.* FROM tracks t
     JOIN playlist_tracks pt ON pt.track_id = t.id
     WHERE pt.playlist_id = ? ORDER BY pt.position ASC`,
    [playlistId]
  );
}

export async function toggleFavorite(trackId) {
  if (IS_WEB) {
    if (mem.favorites.has(trackId)) {
      mem.favorites.delete(trackId);
      return false;
    }
    mem.favorites.set(trackId, Date.now());
    return true;
  }
  const db = await getDb();
  const rows = await db.getAllAsync('SELECT track_id FROM favorites WHERE track_id = ?', [trackId]);
  if (rows.length) {
    await db.runAsync('DELETE FROM favorites WHERE track_id = ?', [trackId]);
    return false;
  }
  await db.runAsync('INSERT INTO favorites (track_id, created_at) VALUES (?, ?)', [trackId, Date.now()]);
  return true;
}

export async function listFavoriteIds() {
  if (IS_WEB) return new Set(mem.favorites.keys());
  const db = await getDb();
  const rows = await db.getAllAsync('SELECT track_id FROM favorites');
  return new Set(rows.map((r) => r.track_id));
}

export async function listFavoriteTracks() {
  if (IS_WEB) {
    return [...mem.favorites.entries()]
      .sort((a, b) => b[1] - a[1])
      .map(([id]) => mem.tracks.get(id))
      .filter(Boolean);
  }
  const db = await getDb();
  return db.getAllAsync(
    `SELECT t.* FROM tracks t JOIN favorites f ON f.track_id = t.id ORDER BY f.created_at DESC`
  );
}

export async function listDownloadedTracks() {
  if (IS_WEB) {
    return [...mem.tracks.values()]
      .filter((t) => t.local_path)
      .sort((a, b) => (a.artist + a.title).localeCompare(b.artist + b.title));
  }
  const db = await getDb();
  return db.getAllAsync('SELECT * FROM tracks WHERE local_path IS NOT NULL ORDER BY artist, title');
}

export async function searchTracks(q) {
  if (IS_WEB) {
    const needle = q.toLowerCase();
    return [...mem.tracks.values()]
      .filter((t) => t.title.toLowerCase().includes(needle) || t.artist.toLowerCase().includes(needle))
      .slice(0, 100);
  }
  const db = await getDb();
  const like = '%' + q + '%';
  return db.getAllAsync(
    'SELECT * FROM tracks WHERE title LIKE ? OR artist LIKE ? ORDER BY artist, title LIMIT 100',
    [like, like]
  );
}
