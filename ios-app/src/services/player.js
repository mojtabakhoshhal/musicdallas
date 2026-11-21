// Player wrapper over expo-audio. Queue + shuffle/repeat + progress state
// for the Spotify-style UI. Background playback via app.json config.
import { createAudioPlayer, setAudioModeAsync } from 'expo-audio';
import { playableUri } from './downloads';

let player = null;
let modeReady = null;
let current = null;
let queue = [];
let queueIndex = -1;
let shuffle = false;
let repeat = false;
const listeners = new Set();

function emit() {
  const s = getPlayerState();
  listeners.forEach((fn) => { try { fn(s); } catch (e) { /* noop */ } });
}

export function subscribePlayer(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

function ensureMode() {
  if (!modeReady) {
    modeReady = setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: true,
      interruptionMode: 'doNotMix',
    });
  }
  return modeReady;
}

async function startTrack(track) {
  const uri = playableUri(track);
  if (!uri) throw new Error('no playable source for track ' + track.id);
  await ensureMode();
  if (player) {
    player.replace({ uri });
  } else {
    player = createAudioPlayer({ uri });
  }
  current = track;
  player.play();
  emit();
}

export async function playTrack(track, list) {
  if (Array.isArray(list) && list.length) {
    const idx = list.findIndex((t) => t.id === track.id);
    queue = list.slice();
    queueIndex = idx >= 0 ? idx : 0;
  } else {
    queue = [track];
    queueIndex = 0;
  }
  await startTrack(track);
}

export async function togglePlay() {
  if (!player) return;
  if (player.playing) player.pause(); else player.play();
  emit();
}

export function pause() {
  if (player) player.pause();
  emit();
}

export function resume() {
  if (player) player.play();
  emit();
}

export async function next() {
  if (!queue.length) return;
  let idx;
  if (shuffle && queue.length > 1) {
    do { idx = Math.floor(Math.random() * queue.length); } while (idx === queueIndex);
  } else {
    idx = queueIndex + 1;
    if (idx >= queue.length) idx = 0;
  }
  queueIndex = idx;
  await startTrack(queue[idx]);
}

export async function prev() {
  if (player && player.currentTime > 3) {
    await player.seekTo(0);
    emit();
    return;
  }
  if (!queue.length) return;
  let idx = queueIndex - 1;
  if (idx < 0) idx = queue.length - 1;
  queueIndex = idx;
  await startTrack(queue[idx]);
}

export async function seekTo(seconds) {
  if (player) {
    await player.seekTo(Math.max(0, seconds));
    emit();
  }
}

export function setShuffle(v) { shuffle = v; emit(); }
export function setRepeat(v) { repeat = v; emit(); }

export function getPlayerState() {
  return {
    track: current,
    playing: !!(player && player.playing),
    position: player ? player.currentTime || 0 : 0,
    duration: player ? player.duration || 0 : 0,
    shuffle,
    repeat,
    queueLength: queue.length,
    queueIndex,
  };
}

export function currentPlayer() {
  return player;
}

// auto-advance / repeat when a track ends
setInterval(() => {
  if (player && current && player.duration > 0 && !player.playing
      && player.currentTime >= player.duration - 0.4) {
    if (repeat) {
      player.seekTo(0).then(() => { player.play(); emit(); }).catch(() => {});
    } else if (queue.length > 1) {
      next();
    }
  }
}, 700);
