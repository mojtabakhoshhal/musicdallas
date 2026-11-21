// Pure logic — no React Native imports, so it can be unit-tested with plain node.

// Normalize Persian/Arabic text for search & dedupe:
// Arabic yeh/kaf -> Persian, remove diacritics/tatweel, collapse spaces, lowercase latin.
function normalizeFa(input) {
  if (!input) return '';
  return String(input)
    .replace(/ي/g, 'ی')
    .replace(/ك/g, 'ک')
    .replace(/ة/g, 'ه')
    .replace(/ؤ/g, 'و')
    .replace(/[ً-ٰٟ]/g, '')
    .replace(/ـ/g, '')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

// Stable key for a track: same artist + same title from two sources = one track.
function trackKey(track) {
  return normalizeFa(track.artist) + '::' + normalizeFa(track.title);
}

// Merge incoming tracks into existing list without duplicates.
// Existing entries win (they may already carry a local download path).
function mergeTracks(existing, incoming) {
  const seen = new Map();
  for (const t of existing) seen.set(trackKey(t), t);
  for (const t of incoming) {
    const k = trackKey(t);
    if (!seen.has(k)) seen.set(k, t);
  }
  return Array.from(seen.values());
}

// Case/orthography-insensitive search over artist + title.
function searchTracks(tracks, query) {
  const q = normalizeFa(query);
  if (!q) return tracks.slice();
  return tracks.filter(
    (t) => normalizeFa(t.artist).includes(q) || normalizeFa(t.title).includes(q)
  );
}

// Playlist helpers (operate on plain arrays of track ids, order preserved).
function addToPlaylist(ids, trackId) {
  return ids.includes(trackId) ? ids.slice() : [...ids, trackId];
}
function removeFromPlaylist(ids, trackId) {
  return ids.filter((id) => id !== trackId);
}
function moveInPlaylist(ids, from, to) {
  const next = ids.slice();
  if (from < 0 || from >= next.length || to < 0 || to >= next.length) return next;
  const [item] = next.splice(from, 1);
  next.splice(to, 0, item);
  return next;
}


// Normalize a catalog payload from any Dallas Music source into a plain
// track array. Accepts: a raw array (GitHub root catalog.json schema:
// [{id, artist, artist_id, title, album, source, remoteUrl, lyrics, cover,
// isNew}]), or the bundled wrapper {version, tracks:[...]}. Entries without
// a playable remoteUrl or without artist/title are dropped — no placeholders.
function normalizeCatalog(data) {
  const list = Array.isArray(data) ? data : (data && Array.isArray(data.tracks) ? data.tracks : []);
  const out = [];
  for (const t of list) {
    if (!t || !t.artist || !t.title) continue;
    const url = t.remoteUrl || t.remote_url || t.audio_url || '';
    if (!/^https?:\/\//.test(url)) continue;
    out.push({
      id: String(t.id || (normalizeFa(t.artist) + '::' + normalizeFa(t.title))),
      artist: t.artist,
      artist_id: t.artist_id != null ? t.artist_id : null,
      title: t.title,
      album: t.album || '',
      source: t.source || 'github',
      remoteUrl: url,
      lyrics: t.lyrics || null,
      cover: t.cover && /^https?:\/\//.test(t.cover) ? t.cover : null,
      isNew: !!t.isNew,
    });
  }
  return out;
}

// True when remote latestVersionCode is strictly newer than the current one.
function isUpdateAvailable(currentCode, versionInfo) {
  const latest = versionInfo && Number(versionInfo.latestVersionCode);
  return Number.isFinite(latest) && latest > Number(currentCode || 0);
}

module.exports = {
  normalizeFa,
  trackKey,
  mergeTracks,
  searchTracks,
  addToPlaylist,
  removeFromPlaylist,
  moveInPlaylist,
  normalizeCatalog,
  isUpdateAvailable,
};
