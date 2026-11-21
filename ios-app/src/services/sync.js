// Sync: pull the latest catalog + version info from the shared GitHub repo
// (mojtabakhoshhal/musicdallas, branch main):
//   catalog.json — https://raw.githubusercontent.com/mojtabakhoshhal/musicdallas/main/catalog.json
//   version.json — https://raw.githubusercontent.com/mojtabakhoshhal/musicdallas/main/version.json
// The app never needs the network to *start* — it works fully offline with
// the catalog bundled in the app, seeded into expo-sqlite on first launch.
// Remote merges preserve favorites, playlists and offline downloads: only
// the tracks table is upserted, user tables are never touched.
import sources from '../data/sources.json';
import bundledCatalog from '../data/catalog.json';
import { upsertTracks } from '../db/db';
import { mergeTracks, normalizeCatalog, isUpdateAvailable } from '../utils/musicUtils';

// This build's version code — keep in sync with app.json
// (android.versionCode / ios.buildNumber) and version.json latestVersionCode.
export const CURRENT_VERSION_CODE = 2;

let seedPromise = null;

// Seed the local DB from the catalog bundled with the app, then try a remote
// refresh. Safe to call from anywhere, any number of times — it runs once
// per app launch.
export function ensureSeeded() {
  if (!seedPromise) {
    seedPromise = (async () => {
      try {
        const { listAllTracks } = await import('../db/db');
        const existing = await listAllTracks();
        const res = await syncCatalog(existing);
        return res;
      } catch (e) {
        return { updated: 0, error: String(e && e.message ? e.message : e) };
      }
    })();
  }
  return seedPromise;
}

export async function syncCatalog(localTracks) {
  // 1) Always seed from the bundled catalog first (zero user action,
  //    works fully offline, also covers a private/unreachable repo).
  const bundled = normalizeCatalog(bundledCatalog);
  const have = new Set((localTracks || []).map((t) => t.id));
  const freshBundled = bundled.filter((t) => !have.has(t.id));
  if (freshBundled.length) await upsertTracks(freshBundled);

  // 2) Then try the always-online GitHub catalog and merge new/updated
  //    tracks in. Failures are silent — the bundled seed already ran.
  const url = sources.backend && sources.backend.catalogUrl;
  if (!url) return { updated: freshBundled.length, offlineOnly: true, seeded: true };
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error('catalog fetch failed: ' + res.status);
    const remote = normalizeCatalog(await res.json());
    const known = new Set([...have, ...freshBundled.map((t) => t.id)]);
    const freshRemote = remote.filter((t) => !known.has(t.id));
    if (freshRemote.length) await upsertTracks(freshRemote);
    return { updated: freshBundled.length + freshRemote.length, offlineOnly: false, seeded: true };
  } catch (e) {
    return { updated: freshBundled.length, offlineOnly: true, seeded: true };
  }
}

// Check version.json on launch. Returns the version info object when a
// newer build exists (latestVersionCode > CURRENT_VERSION_CODE), else null.
export async function checkForUpdate() {
  const url = sources.backend && sources.backend.versionUrl;
  if (!url) return null;
  try {
    const res = await fetch(url);
    if (!res.ok) return null;
    const info = await res.json();
    return isUpdateAvailable(CURRENT_VERSION_CODE, info) ? info : null;
  } catch (e) {
    return null;
  }
}

export { mergeTracks };
