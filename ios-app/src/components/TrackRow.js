import React, { useState } from 'react';
import { ActivityIndicator, Image, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { downloadTrack, deleteDownload } from '../services/downloads';
import { C, F } from '../theme';

export function Cover({ uri, size, radius }) {
  const [err, setErr] = useState(false);
  if (!uri || err) {
    return (
      <View style={[styles.fallback, { width: size, height: size, borderRadius: radius ?? size / 2 }]}>
        <Ionicons name="musical-note" size={size * 0.42} color={C.gold} />
      </View>
    );
  }
  return (
    <Image
      source={{ uri }}
      style={{ width: size, height: size, borderRadius: radius ?? size / 2 }}
      onError={() => setErr(true)}
    />
  );
}

export default function TrackRow({ track, list, onPlay, isFav, onToggleFav, onAdd, active }) {
  const [dl, setDl] = useState(!!track.local_path);
  const [busy, setBusy] = useState(false);

  async function onDl() {
    if (busy) return;
    setBusy(true);
    try {
      if (dl) {
        await deleteDownload(track);
        setDl(false);
      } else {
        await downloadTrack(track);
        setDl(true);
      }
    } catch (e) { /* surfaced by state staying unchanged */ }
    setBusy(false);
  }

  return (
    <TouchableOpacity style={[styles.row, active && styles.rowActive]} onPress={() => onPlay(track, list)}>
      <Cover uri={track.cover} size={52} radius={10} />
      <View style={styles.body}>
        <Text style={[styles.title, active && styles.titleActive]} numberOfLines={1}>{track.title}</Text>
        <Text style={styles.artist} numberOfLines={1}>{track.artist}{dl ? ' • آفلاین' : ''}</Text>
      </View>
      {onAdd && (
        <TouchableOpacity onPress={() => onAdd(track)} hitSlop={8} style={styles.iconBtn}>
          <Ionicons name="add-circle-outline" size={23} color={C.muted} />
        </TouchableOpacity>
      )}
      {onToggleFav && (
        <TouchableOpacity onPress={() => onToggleFav(track)} hitSlop={8} style={styles.iconBtn}>
          <Ionicons name={isFav ? 'heart' : 'heart-outline'} size={23} color={isFav ? C.gold : C.muted} />
        </TouchableOpacity>
      )}
      <TouchableOpacity onPress={onDl} hitSlop={8} style={styles.iconBtn}>
        {busy
          ? <ActivityIndicator size="small" color={C.gold} />
          : <Ionicons
              name={dl ? 'checkmark-circle' : 'arrow-down-circle-outline'}
              size={23}
              color={dl ? '#35d07f' : C.muted}
            />}
      </TouchableOpacity>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  fallback: { backgroundColor: '#23232e', alignItems: 'center', justifyContent: 'center' },
  row: { flexDirection: 'row-reverse', alignItems: 'center', paddingVertical: 8, paddingHorizontal: 14, gap: 12 },
  rowActive: { backgroundColor: '#1b1b24' },
  body: { flex: 1 },
  title: { ...F.bold, color: '#fff', fontSize: 14.5, textAlign: 'right' },
  titleActive: { color: C.gold },
  artist: { ...F.reg, color: '#9a9aad', fontSize: 12.5, textAlign: 'right', marginTop: 2 },
  iconBtn: { padding: 4 },
});
