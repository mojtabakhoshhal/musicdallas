import React, { useCallback, useState } from 'react';
import {
  FlatList,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { C, F } from '../theme';
import { createPlaylist, listPlaylists, listPlaylistTracks } from '../db/db';
import { playTrack } from '../services/player';

export default function PlaylistsScreen() {
  const [playlists, setPlaylists] = useState([]);
  const [name, setName] = useState('');
  const [openId, setOpenId] = useState(null);
  const [openTracks, setOpenTracks] = useState([]);

  const load = useCallback(async () => {
    setPlaylists(await listPlaylists());
  }, []);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function onCreate() {
    const trimmed = name.trim();
    if (!trimmed) return;
    await createPlaylist(trimmed);
    setName('');
    await load();
  }

  async function onOpen(pl) {
    if (openId === pl.id) {
      setOpenId(null);
      setOpenTracks([]);
      return;
    }
    setOpenId(pl.id);
    setOpenTracks(await listPlaylistTracks(pl.id));
  }

  return (
    <View style={styles.root}>
      <View style={styles.createRow}>
        <TouchableOpacity style={styles.createBtn} onPress={onCreate}>
          <Text style={styles.createBtnText}>ساختن</Text>
        </TouchableOpacity>
        <TextInput
          style={styles.input}
          value={name}
          onChangeText={setName}
          placeholder="اسم پلی‌لیست جدید"
          placeholderTextColor="#777"
          textAlign="right"
        />
      </View>
      <FlatList
        data={playlists}
        keyExtractor={(p) => p.id}
        ListEmptyComponent={<Text style={styles.empty}>هنوز پلی‌لیستی نساختی.</Text>}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <TouchableOpacity onPress={() => onOpen(item)} style={styles.plHead}>
              <Ionicons name="list" size={20} color={C.gold} />
              <Text style={styles.name}>{item.name}</Text>
              <Ionicons name={openId === item.id ? 'chevron-down' : 'chevron-back'} size={18} color={C.gold} />
            </TouchableOpacity>
            {openId === item.id &&
              openTracks.map((t) => (
                <TouchableOpacity key={t.id} onPress={() => playTrack(t)} style={styles.trackRow}>
                  <Ionicons name="play" size={11} color={C.gold} />
                  <Text style={styles.track}>
                    {t.title} — {t.artist} {t.local_path ? ' (آفلاین)' : ''}
                  </Text>
                </TouchableOpacity>
              ))}
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  createRow: { flexDirection: 'row', gap: 10, padding: 16 },
  input: {
    ...F.reg,
    flex: 1,
    color: '#fff',
    backgroundColor: '#17171f',
    borderWidth: 1,
    borderColor: '#2a2a33',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 11,
  },
  createBtn: { backgroundColor: '#ffb800', borderRadius: 12, justifyContent: 'center', paddingHorizontal: 18 },
  createBtnText: { ...F.bold, color: '#111' },
  empty: { ...F.reg, color: '#9a9aad', textAlign: 'center', marginTop: 32 },
  row: {
    backgroundColor: '#17171f',
    borderWidth: 1,
    borderColor: '#2a2a33',
    borderRadius: 14,
    paddingVertical: 14,
    paddingHorizontal: 16,
    marginHorizontal: 16,
    marginBottom: 10,
  },
  plHead: { flexDirection: 'row-reverse', alignItems: 'center', gap: 10 },
  name: { ...F.bold, color: '#fff', fontSize: 15.5, flex: 1, textAlign: 'right' },
  trackRow: { flexDirection: 'row-reverse', alignItems: 'center', gap: 8, marginTop: 10 },
  track: { ...F.reg, color: '#d7d7e0', fontSize: 13.5, textAlign: 'right', flex: 1 },
});
