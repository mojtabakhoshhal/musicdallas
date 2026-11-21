import React, { useEffect, useRef, useState } from 'react';
import {
  ScrollView, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import {
  getPlayerState, next, prev, seekTo, setRepeat, setShuffle,
  subscribePlayer, togglePlay,
} from '../services/player';
import { listFavoriteIds, toggleFavorite } from '../db/db';
import { downloadTrack } from '../services/downloads';
import { Cover } from '../components/TrackRow';
import { C, F } from '../theme';

function fmt(sec) {
  if (!sec || !isFinite(sec)) return '0:00';
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${m}:${s < 10 ? '0' : ''}${s}`;
}

export default function PlayerScreen() {
  const [st, setSt] = useState(getPlayerState());
  const [fav, setFav] = useState(false);
  const [showLyrics, setShowLyrics] = useState(false);
  const barW = useRef(0);

  useEffect(() => {
    const unsub = subscribePlayer(setSt);
    const iv = setInterval(() => setSt(getPlayerState()), 500);
    return () => { unsub(); clearInterval(iv); };
  }, []);

  useEffect(() => {
    (async () => {
      if (st.track) setFav((await listFavoriteIds()).has(st.track.id));
    })();
  }, [st.track && st.track.id]);

  if (!st.track) {
    return (
      <View style={styles.root}>
        <Text style={styles.empty}>هنوز آهنگی پخش نشده.</Text>
      </View>
    );
  }
  const t = st.track;
  const pct = st.duration > 0 ? st.position / st.duration : 0;

  function onSeek(e) {
    if (!barW.current || !st.duration) return;
    const x = e.nativeEvent.locationX;
    const ratio = Math.max(0, Math.min(1, 1 - x / barW.current)); // RTL bar
    seekTo(ratio * st.duration);
  }

  return (
    <ScrollView style={styles.root} contentContainerStyle={styles.content}>
      <Cover uri={t.cover} size={300} radius={18} />
      <Text style={styles.title}>{t.title}</Text>
      <Text style={styles.artist}>{t.artist}</Text>

      <TouchableOpacity
        style={styles.bar}
        onPress={onSeek}
        onLayout={(e) => { barW.current = e.nativeEvent.layout.width; }}
        activeOpacity={1}
      >
        <View style={styles.barTrack}>
          <View style={[styles.barFill, { width: (pct * 100) + '%' }]} />
        </View>
      </TouchableOpacity>
      <View style={styles.times}>
        <Text style={styles.time}>{fmt(st.duration)}</Text>
        <Text style={styles.time}>{fmt(st.position)}</Text>
      </View>

      <View style={styles.controls}>
        <TouchableOpacity onPress={() => setRepeat(!st.repeat)} hitSlop={8}>
          <Ionicons name="repeat" size={24} color={st.repeat ? C.gold : '#6d6d7d'} />
        </TouchableOpacity>
        <TouchableOpacity onPress={prev} hitSlop={8}>
          <Ionicons name="play-skip-back" size={32} color="#fff" />
        </TouchableOpacity>
        <TouchableOpacity onPress={togglePlay} style={styles.playBtn}>
          <Ionicons name={st.playing ? 'pause' : 'play'} size={30} color="#111" />
        </TouchableOpacity>
        <TouchableOpacity onPress={next} hitSlop={8}>
          <Ionicons name="play-skip-forward" size={32} color="#fff" />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => setShuffle(!st.shuffle)} hitSlop={8}>
          <Ionicons name="shuffle" size={24} color={st.shuffle ? C.gold : '#6d6d7d'} />
        </TouchableOpacity>
      </View>

      <View style={styles.actions}>
        <TouchableOpacity
          style={styles.actionBtn}
          onPress={async () => setFav(await toggleFavorite(t.id))}
        >
          <Ionicons name={fav ? 'heart' : 'heart-outline'} size={17} color={fav ? C.gold : '#e8e8ef'} />
          <Text style={styles.actionText}>{fav ? 'پسندیده شد' : 'پسندیدن'}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionBtn} onPress={() => downloadTrack(t).catch(() => {})}>
          <Ionicons name="arrow-down-circle-outline" size={17} color="#e8e8ef" />
          <Text style={styles.actionText}>دانلود آفلاین</Text>
        </TouchableOpacity>
        {t.lyrics ? (
          <TouchableOpacity style={styles.actionBtn} onPress={() => setShowLyrics(!showLyrics)}>
            <Ionicons name="document-text-outline" size={17} color="#e8e8ef" />
            <Text style={styles.actionText}>متن آهنگ</Text>
          </TouchableOpacity>
        ) : null}
      </View>

      {showLyrics && t.lyrics ? <Text style={styles.lyrics}>{t.lyrics}</Text> : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.bg },
  content: { alignItems: 'center', padding: 24, paddingTop: 30 },
  empty: { ...F.reg, color: '#9a9aad', textAlign: 'center', marginTop: 60 },
  title: { ...F.bold, color: '#fff', fontSize: 22, marginTop: 24, textAlign: 'center' },
  artist: { ...F.reg, color: C.gold, fontSize: 15.5, marginTop: 5 },
  bar: { width: '100%', marginTop: 30, paddingVertical: 8 },
  barTrack: { height: 5, borderRadius: 3, backgroundColor: '#2a2a33', overflow: 'hidden', flexDirection: 'row-reverse' },
  barFill: { height: 5, backgroundColor: C.gold },
  times: { width: '100%', flexDirection: 'row', justifyContent: 'space-between' },
  time: { ...F.reg, color: '#9a9aad', fontSize: 12.5 },
  controls: { flexDirection: 'row', alignItems: 'center', gap: 26, marginTop: 18 },
  playBtn: { backgroundColor: C.gold, width: 68, height: 68, borderRadius: 34, alignItems: 'center', justifyContent: 'center' },
  actions: { flexDirection: 'row-reverse', gap: 10, marginTop: 26, flexWrap: 'wrap', justifyContent: 'center' },
  actionBtn: { backgroundColor: '#17171f', borderWidth: 1, borderColor: '#2a2a33', borderRadius: 20, paddingVertical: 9, paddingHorizontal: 14, flexDirection: 'row-reverse', alignItems: 'center', gap: 6 },
  actionText: { ...F.reg, color: '#e8e8ef', fontSize: 13 },
  lyrics: { ...F.reg, color: '#d7d7e0', fontSize: 14.5, lineHeight: 27, textAlign: 'right', marginTop: 22, width: '100%' },
});
