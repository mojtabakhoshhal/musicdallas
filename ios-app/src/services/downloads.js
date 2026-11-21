// Downloads: fetch a track file into the app's own document directory.
// Files here persist until the user deletes them — this is what makes
// offline playback real on both iOS and Android.
import * as FileSystem from 'expo-file-system';
import { markDownloaded } from '../db/db';

const MUSIC_DIR = FileSystem.documentDirectory + 'music/';

async function ensureDir() {
  const info = await FileSystem.getInfoAsync(MUSIC_DIR);
  if (!info.exists) await FileSystem.makeDirectoryAsync(MUSIC_DIR, { intermediates: true });
}

export async function downloadTrack(track, onProgress) {
  await ensureDir();
  const safeId = String(track.id).replace(/[^a-zA-Z0-9_-]/g, '_');
  const dest = MUSIC_DIR + safeId + '.mp3';
  const existing = await FileSystem.getInfoAsync(dest);
  if (existing.exists) {
    await markDownloaded(track.id, dest);
    return dest;
  }
  const task = FileSystem.createDownloadResumable(track.remote_url || track.remoteUrl, dest, {}, (p) => {
    if (onProgress && p.totalBytesExpectedToWrite > 0) {
      onProgress(p.totalBytesWritten / p.totalBytesExpectedToWrite);
    }
  });
  const result = await task.downloadAsync();
  if (!result || result.status !== 200) throw new Error('download failed for ' + track.id);
  await markDownloaded(track.id, result.uri);
  return result.uri;
}

export async function deleteDownload(track) {
  if (!track.local_path) return;
  const info = await FileSystem.getInfoAsync(track.local_path);
  if (info.exists) await FileSystem.deleteAsync(track.local_path, { idempotent: true });
  await markDownloaded(track.id, null);
}

// What the player should use: local file when downloaded, else the remote URL.
export function playableUri(track) {
  return track.local_path || track.remote_url || null;
}
