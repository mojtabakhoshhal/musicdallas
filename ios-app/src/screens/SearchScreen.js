import React, { useCallback, useState } from 'react';
import {
  FlatList, StyleSheet, Text, TextInput, TouchableOpacity, View,
} from 'react-native';
import { searchTracks, listFavoriteIds, toggleFavorite } from '../db/db';
import { playTrack } from '../services/player';
import TrackRow from '../components/TrackRow';
import artistsData from '../data/artists.json';
import { F } from '../theme';

export default function SearchScreen({ navigation }) {
  const [q, setQ] = useState('');
  const [results, setResults] = useState([]);
  const [favIds, setFavIds] = useState(new Set());

  const run = useCallback(async (text) => {
    setQ(text);
    if (text.trim().length < 2) {
      setResults([]);
      return;
    }
    const rows = await searchTracks(text.trim());
    setResults(rows);
    setFavIds(await listFavoriteIds());
  }, []);

  const artists = q.trim().length >= 2
    ? artistsData.artists.filter((a) => a.name_normalized.includes(q.trim()))
    : [];

  return (
    <View style={styles.root}>
      <TextInput
        style={styles.input}
        value={q}
        onChangeText={run}
        placeholder="جستجوی آهنگ یا خواننده…"
        placeholderTextColor="#777"
        textAlign="right"
      />
      {artists.map((a) => (
        <TouchableOpacity
          key={a.id}
          style={styles.artistHit}
          onPress={() => navigation.navigate('Tracks', { artist: a.name_normalized })}
        >
          <Text style={styles.artistHitText}>خواننده: {a.name_normalized}</Text>
        </TouchableOpacity>
      ))}
      <FlatList
        data={results}
        keyExtractor={(t) => t.id}
        ListEmptyComponent={
          q.trim().length >= 2 ? <Text style={styles.empty}>چیزی پیدا نشد.</Text> : null
        }
        renderItem={({ item }) => (
          <TrackRow
            track={item}
            list={results}
            onPlay={(t, list) => playTrack(t, list)}
            isFav={favIds.has(item.id)}
            onToggleFav={async (t) => {
              await toggleFavorite(t.id);
              setFavIds(await listFavoriteIds());
            }}
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  input: {
    ...F.reg,
    color: '#fff', backgroundColor: '#17171f', borderWidth: 1, borderColor: '#2a2a33',
    borderRadius: 24, paddingHorizontal: 18, paddingVertical: 12, margin: 16, fontSize: 15,
  },
  artistHit: { paddingHorizontal: 20, paddingVertical: 10 },
  artistHitText: { ...F.bold, color: '#ffb800', fontSize: 15, textAlign: 'right' },
  empty: { ...F.reg, color: '#9a9aad', textAlign: 'center', marginTop: 26 },
});
