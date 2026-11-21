import React, { useCallback, useMemo, useState } from 'react';
import { Alert, FlatList, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { F } from '../theme';
import { useFocusEffect } from '@react-navigation/native';
import {
  addTrackToPlaylist, listFavoriteIds, listPlaylists, listTracksByArtist, toggleFavorite,
} from '../db/db';
import { ensureSeeded } from '../services/sync';
import bundledCatalog from '../data/catalog.json';
import { getPlayerState, playTrack, subscribePlayer } from '../services/player';
import TrackRow, { Cover } from '../components/TrackRow';

export default function TracksScreen({ route }) {
  const { artist } = route.params;
  const [tracks, setTracks] = useState([]);
  const [favIds, setFavIds] = useState(new Set());
  const [currentId, setCurrentId] = useState(null);

  const load = useCallback(async () => {
    await ensureSeeded();
    let rows = await listTracksByArtist(artist);
    if (!rows.length) {
      rows = (bundledCatalog.tracks || [])
        .filter((t) => t.artist === artist)
        .map((t) => ({
          id: t.id, artist: t.artist, title: t.title, lyrics: t.lyrics || null,
          remote_url: t.remoteUrl || null, local_path: null, cover: t.cover || null,
        }));
    }
    setTracks(rows);
    setFavIds(await listFavoriteIds());
  }, [artist]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  React.useEffect(() => subscribePlayer((s) => setCurrentId(s.track ? s.track.id : null)), []);
  React.useEffect(() => {
    const s = getPlayerState();
    setCurrentId(s.track ? s.track.id : null);
  }, []);

  const headerCover = useMemo(() => {
    const t = tracks.find((x) => x.cover);
    return t ? t.cover : null;
  }, [tracks]);

  async function onAddToPlaylist(track) {
    const playlists = await listPlaylists();
    if (!playlists.length) {
      Alert.alert('پلی‌لیست نداری', 'اول از بخش کتابخانه یکی بساز.');
      return;
    }
    await addTrackToPlaylist(playlists[0].id, track.id);
    Alert.alert('اضافه شد', 'به پلی‌لیست «' + playlists[0].name + '» اضافه شد.');
  }

  return (
    <View style={styles.root}>
      <View style={styles.header}>
        <Cover uri={headerCover} size={120} radius={16} />
        <Text style={styles.artistName}>{artist}</Text>
        <Text style={styles.count}>{tracks.length} آهنگ</Text>
        {tracks.length > 0 && (
          <TouchableOpacity
            style={styles.shuffleBtn}
            onPress={() => {
              const idx = Math.floor(Math.random() * tracks.length);
              playTrack(tracks[idx], tracks);
            }}
          >
            <Ionicons name="shuffle" size={18} color="#111" />
            <Text style={styles.shuffleText}>پخش تصادفی</Text>
          </TouchableOpacity>
        )}
      </View>
      {tracks.length === 0 ? (
        <Text style={styles.empty}>
          هنوز آهنگی برای این خواننده پیدا نشده — کاتالوگ در حال تکمیله و با آپدیت بعدی اپ اضافه می‌شه.
        </Text>
      ) : (
        <FlatList
          data={tracks}
          keyExtractor={(t) => t.id}
          renderItem={({ item }) => (
            <TrackRow
              track={item}
              list={tracks}
              active={currentId === item.id}
              onPlay={(t, list) => playTrack(t, list)}
              isFav={favIds.has(item.id)}
              onToggleFav={async (t) => {
                await toggleFavorite(t.id);
                setFavIds(await listFavoriteIds());
              }}
              onAdd={onAddToPlaylist}
            />
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  header: { alignItems: 'center', paddingTop: 22, paddingBottom: 14 },
  artistName: { ...F.bold, color: '#fff', fontSize: 23, marginTop: 14 },
  count: { ...F.reg, color: '#9a9aad', fontSize: 13.5, marginTop: 4 },
  shuffleBtn: { backgroundColor: '#ffb800', borderRadius: 22, paddingVertical: 10, paddingHorizontal: 22, marginTop: 14, flexDirection: 'row-reverse', alignItems: 'center', gap: 7 },
  shuffleText: { ...F.bold, color: '#111', fontSize: 14 },
  empty: { ...F.reg, color: '#9a9aad', textAlign: 'center', marginTop: 30, paddingHorizontal: 24, lineHeight: 24 },
});
