import React, { useCallback, useState } from 'react';
import {
  FlatList, StyleSheet, Text, TouchableOpacity, View,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { C, F } from '../theme';
import {
  listDownloadedTracks, listFavoriteIds, listFavoriteTracks, toggleFavorite,
} from '../db/db';
import { playTrack } from '../services/player';
import TrackRow from '../components/TrackRow';

export default function LibraryScreen({ navigation }) {
  const [tab, setTab] = useState('fav');
  const [favs, setFavs] = useState([]);
  const [dls, setDls] = useState([]);
  const [favIds, setFavIds] = useState(new Set());

  const load = useCallback(async () => {
    setFavs(await listFavoriteTracks());
    setDls(await listDownloadedTracks());
    setFavIds(await listFavoriteIds());
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const data = tab === 'fav' ? favs : dls;

  return (
    <View style={styles.root}>
      <TouchableOpacity style={styles.plEntry} onPress={() => navigation.navigate('Playlists')}>
        <Ionicons name="list" size={22} color={C.gold} />
        <View style={styles.plBody}>
          <Text style={styles.plTitle}>پلی‌لیست‌های من</Text>
          <Text style={styles.plSub}>ساختن و مدیریت لیست‌های دلخواه</Text>
        </View>
        <Ionicons name="chevron-back" size={20} color={C.gold} />
      </TouchableOpacity>

      <TouchableOpacity style={styles.plEntry} onPress={() => navigation.navigate('About')}>
        <Ionicons name="people" size={22} color={C.gold} />
        <View style={styles.plBody}>
          <Text style={styles.plTitle}>توسعه‌دهندگان</Text>
          <Text style={styles.plSub}>مجتبی خوشحال • امیر خوشحال</Text>
        </View>
        <Ionicons name="chevron-back" size={20} color={C.gold} />
      </TouchableOpacity>

      <View style={styles.tabs}>
        <TouchableOpacity
          style={[styles.tab, tab === 'fav' && styles.tabOn]}
          onPress={() => setTab('fav')}
        >
          <Ionicons name="heart" size={15} color={tab === 'fav' ? '#111' : '#d7d7e0'} />
          <Text style={[styles.tabText, tab === 'fav' && styles.tabTextOn]}>پسندیده‌ها ({favs.length})</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, tab === 'dl' && styles.tabOn]}
          onPress={() => setTab('dl')}
        >
          <Ionicons name="arrow-down-circle" size={15} color={tab === 'dl' ? '#111' : '#d7d7e0'} />
          <Text style={[styles.tabText, tab === 'dl' && styles.tabTextOn]}>دانلودها ({dls.length})</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={data}
        keyExtractor={(t) => t.id}
        ListEmptyComponent={
          <Text style={styles.empty}>
            {tab === 'fav' ? 'هنوز آهنگی رو نپسندیدی.' : 'هنوز آهنگی دانلود نکردی.'}
          </Text>
        }
        renderItem={({ item }) => (
          <TrackRow
            track={item}
            list={data}
            onPlay={(t, list) => playTrack(t, list)}
            isFav={favIds.has(item.id)}
            onToggleFav={async (t) => {
              await toggleFavorite(t.id);
              load();
            }}
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  plEntry: {
    flexDirection: 'row-reverse', alignItems: 'center', gap: 12,
    backgroundColor: '#17171f', borderWidth: 1, borderColor: '#2a2a33',
    borderRadius: 14, padding: 16, margin: 16, marginBottom: 6,
  },
  plBody: { flex: 1 },
  plTitle: { ...F.bold, color: '#fff', fontSize: 15.5, textAlign: 'right' },
  plSub: { ...F.reg, color: '#9a9aad', fontSize: 12.5, textAlign: 'right', marginTop: 2 },
  tabs: { flexDirection: 'row-reverse', gap: 10, paddingHorizontal: 16, paddingVertical: 10 },
  tab: { borderWidth: 1, borderColor: '#2a2a33', borderRadius: 18, paddingVertical: 8, paddingHorizontal: 14, flexDirection: 'row-reverse', alignItems: 'center', gap: 6 },
  tabOn: { backgroundColor: '#ffb800', borderColor: '#ffb800' },
  tabText: { ...F.bold, color: '#d7d7e0', fontSize: 13 },
  tabTextOn: { color: '#111' },
  empty: { ...F.reg, color: '#9a9aad', textAlign: 'center', marginTop: 30 },
});
