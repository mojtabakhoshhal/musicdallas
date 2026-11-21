import React, { useCallback, useMemo, useState } from 'react';
import {
  FlatList, Image, ScrollView, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { F } from '../theme';
import { ensureSeeded } from '../services/sync';
import { listAllTracks } from '../db/db';
import { playTrack } from '../services/player';
import { Cover } from '../components/TrackRow';
import artistsData from '../data/artists.json';
import bundled from '../data/catalog.json';

export default function HomeScreen({ navigation }) {
  const [tracks, setTracks] = useState(bundled.tracks || []);

  const load = useCallback(async () => {
    await ensureSeeded();
    const rows = await listAllTracks();
    if (rows && rows.length) {
      setTracks(rows.map((r) => ({ ...r, remoteUrl: r.remote_url })));
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const coverByArtist = useMemo(() => {
    const m = {};
    for (const t of tracks) {
      if (t.cover && !m[t.artist]) m[t.artist] = t.cover;
    }
    return m;
  }, [tracks]);

  const countByArtist = useMemo(() => {
    const m = {};
    for (const t of tracks) m[t.artist] = (m[t.artist] || 0) + 1;
    return m;
  }, [tracks]);

  const withCovers = useMemo(() => tracks.filter((t) => t.cover), [tracks]);
  const shelf = withCovers.slice(0, 14);
  const shelf2 = tracks.slice(0, 14);

  return (
    <ScrollView style={styles.root}>
      <View style={styles.header}>
        <Image source={require('../../assets/icon.png')} style={styles.logo} />
        <View>
          <Text style={styles.brand}>دالاس موزیک</Text>
          <Text style={styles.tagline}>{tracks.length} آهنگ • ۲۰ خواننده • آفلاین</Text>
        </View>
      </View>

      <Text style={styles.section}>خواننده‌ها</Text>
      <FlatList
        horizontal
        inverted
        showsHorizontalScrollIndicator={false}
        data={artistsData.artists}
        keyExtractor={(a) => String(a.id)}
        contentContainerStyle={styles.rail}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.artistItem}
            onPress={() => navigation.navigate('Tracks', { artist: item.name_normalized })}
          >
            <Cover uri={coverByArtist[item.name_normalized]} size={74} radius={37} />
            <Text style={styles.artistName} numberOfLines={1}>{item.name_normalized}</Text>
            <Text style={styles.artistCount}>{countByArtist[item.name_normalized] || 0} آهنگ</Text>
          </TouchableOpacity>
        )}
      />

      <Text style={styles.section}>تازه‌ها</Text>
      <FlatList
        horizontal
        inverted
        showsHorizontalScrollIndicator={false}
        data={shelf}
        keyExtractor={(t) => t.id}
        contentContainerStyle={styles.rail}
        renderItem={({ item }) => (
          <TouchableOpacity style={styles.cardItem} onPress={() => playTrack(item, shelf)}>
            <Cover uri={item.cover} size={132} radius={12} />
            <Text style={styles.cardTitle} numberOfLines={1}>{item.title}</Text>
            <Text style={styles.cardArtist} numberOfLines={1}>{item.artist}</Text>
          </TouchableOpacity>
        )}
      />

      <Text style={styles.section}>همه آهنگ‌ها</Text>
      {shelf2.map((t) => (
        <TouchableOpacity key={t.id} style={styles.row} onPress={() => playTrack(t, shelf2)}>
          <Cover uri={t.cover} size={50} radius={9} />
          <View style={styles.rowBody}>
            <Text style={styles.rowTitle} numberOfLines={1}>{t.title}</Text>
            <Text style={styles.rowArtist} numberOfLines={1}>{t.artist}</Text>
          </View>
          <Ionicons name="play" size={15} color="#ffb800" />
        </TouchableOpacity>
      ))}
      <View style={{ height: 18 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  header: { flexDirection: 'row-reverse', alignItems: 'center', gap: 14, padding: 18, paddingBottom: 4 },
  logo: { width: 54, height: 54, borderRadius: 14 },
  brand: { ...F.bold, color: '#fff', fontSize: 23, textAlign: 'right' },
  tagline: { ...F.reg, color: '#9a9aad', fontSize: 13, marginTop: 2, textAlign: 'right' },
  section: { ...F.bold, color: '#fff', fontSize: 18, textAlign: 'right', paddingHorizontal: 18, marginTop: 22, marginBottom: 10 },
  rail: { paddingHorizontal: 14, gap: 14 },
  artistItem: { alignItems: 'center', width: 84 },
  artistName: { ...F.reg, color: '#fff', fontSize: 12.5, marginTop: 7, textAlign: 'center' },
  artistCount: { ...F.reg, color: '#77778a', fontSize: 11, marginTop: 1 },
  cardItem: { width: 132 },
  cardTitle: { ...F.bold, color: '#fff', fontSize: 13, marginTop: 8, textAlign: 'right' },
  cardArtist: { ...F.reg, color: '#9a9aad', fontSize: 12, marginTop: 1, textAlign: 'right' },
  row: { flexDirection: 'row-reverse', alignItems: 'center', gap: 12, paddingHorizontal: 18, paddingVertical: 7 },
  rowBody: { flex: 1 },
  rowTitle: { ...F.bold, color: '#fff', fontSize: 14, textAlign: 'right' },
  rowArtist: { ...F.reg, color: '#9a9aad', fontSize: 12, textAlign: 'right', marginTop: 1 },
});
