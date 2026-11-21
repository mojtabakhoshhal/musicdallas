import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { getPlayerState, next, subscribePlayer, togglePlay } from '../services/player';
import { Cover } from './TrackRow';
import { C, F } from '../theme';

export default function MiniPlayer({ navigation }) {
  const [st, setSt] = useState(getPlayerState());

  useEffect(() => {
    const unsub = subscribePlayer(setSt);
    const iv = setInterval(() => setSt(getPlayerState()), 600);
    return () => { unsub(); clearInterval(iv); };
  }, []);

  if (!st.track) return null;
  const pct = st.duration > 0 ? Math.min(100, (st.position / st.duration) * 100) : 0;

  return (
    <TouchableOpacity style={styles.wrap} onPress={() => navigation.navigate('Player')} activeOpacity={0.9}>
      <View style={styles.progressTrack}>
        <View style={[styles.progressFill, { width: pct + '%' }]} />
      </View>
      <View style={styles.row}>
        <Cover uri={st.track.cover} size={44} radius={8} />
        <View style={styles.body}>
          <Text style={styles.title} numberOfLines={1}>{st.track.title}</Text>
          <Text style={styles.artist} numberOfLines={1}>{st.track.artist}</Text>
        </View>
        <TouchableOpacity onPress={next} hitSlop={10} style={styles.btn}>
          <Ionicons name="play-skip-forward" size={21} color="#d7d7e0" />
        </TouchableOpacity>
        <TouchableOpacity onPress={togglePlay} hitSlop={10} style={[styles.btn, styles.playBtn]}>
          <Ionicons name={st.playing ? 'pause' : 'play'} size={19} color="#111" />
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  wrap: { backgroundColor: '#17171f', borderTopWidth: 1, borderTopColor: '#26262f' },
  progressTrack: { height: 2.5, backgroundColor: '#2a2a33' },
  progressFill: { height: 2.5, backgroundColor: C.gold },
  row: { flexDirection: 'row-reverse', alignItems: 'center', gap: 12, paddingHorizontal: 12, paddingVertical: 8 },
  body: { flex: 1 },
  title: { ...F.bold, color: '#fff', fontSize: 13.5, textAlign: 'right' },
  artist: { ...F.reg, color: '#9a9aad', fontSize: 12, textAlign: 'right', marginTop: 1 },
  btn: { padding: 6 },
  playBtn: { backgroundColor: C.gold, borderRadius: 20, width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
});
