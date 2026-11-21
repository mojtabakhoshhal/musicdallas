import React, { useMemo, useState } from 'react';
import { FlatList, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import data from '../data/artists.json';
import catalog from '../data/catalog.json';
import { Cover } from '../components/TrackRow';
import { F } from '../theme';

export default function ArtistsScreen({ navigation }) {
  const [q, setQ] = useState('');

  const { counts, covers } = useMemo(() => {
    const counts = {};
    const covers = {};
    for (const t of catalog.tracks || []) {
      counts[t.artist] = (counts[t.artist] || 0) + 1;
      if (t.cover && !covers[t.artist]) covers[t.artist] = t.cover;
    }
    return { counts, covers };
  }, []);

  const list = useMemo(() => {
    const query = q.trim();
    const all = data.artists;
    if (!query) return all;
    return all.filter(
      (a) =>
        a.name_normalized.includes(query) ||
        (a.latin || '').toLowerCase().includes(query.toLowerCase())
    );
  }, [q]);

  return (
    <View style={styles.root}>
      <TextInput
        style={styles.search}
        value={q}
        onChangeText={setQ}
        placeholder="جستجوی خواننده..."
        placeholderTextColor="#777"
        textAlign="right"
      />
      <FlatList
        data={list}
        numColumns={2}
        keyExtractor={(a) => String(a.id)}
        columnWrapperStyle={styles.rowWrap}
        contentContainerStyle={styles.listPad}
        renderItem={({ item }) => {
          const n = counts[item.name_normalized] || 0;
          return (
            <TouchableOpacity
              style={styles.card}
              activeOpacity={0.8}
              onPress={() => navigation.navigate('Tracks', { artist: item.name_normalized })}
            >
              <Cover uri={covers[item.name_normalized]} size={86} radius={43} />
              <Text style={styles.name} numberOfLines={1}>{item.name_normalized}</Text>
              <View style={styles.metaRow}>
                <Text style={styles.chip}>{item.genre === 'pop' ? 'پاپ' : 'رپ'}</Text>
                <Text style={styles.count}>{n} آهنگ</Text>
              </View>
            </TouchableOpacity>
          );
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  search: {
    ...F.reg,
    margin: 16, marginBottom: 4, color: '#fff', backgroundColor: '#17171f',
    borderWidth: 1, borderColor: '#2a2a33', borderRadius: 24,
    paddingHorizontal: 16, paddingVertical: 11, fontSize: 15,
  },
  listPad: { padding: 12 },
  rowWrap: { flexDirection: 'row-reverse', gap: 12 },
  card: {
    flex: 1, backgroundColor: '#17171f', borderWidth: 1, borderColor: '#2a2a33',
    borderRadius: 18, alignItems: 'center', paddingVertical: 18,
    paddingHorizontal: 8, marginBottom: 12,
  },
  name: { ...F.bold, color: '#fff', fontSize: 14.5, textAlign: 'center', marginTop: 12 },
  metaRow: { flexDirection: 'row-reverse', alignItems: 'center', gap: 8, marginTop: 7 },
  chip: {
    ...F.bold,
    color: '#111', backgroundColor: '#ffb800', fontSize: 11,
    paddingHorizontal: 8, paddingVertical: 2, borderRadius: 8, overflow: 'hidden',
  },
  count: { ...F.reg, color: '#9a9aad', fontSize: 12 },
});
