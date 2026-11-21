import React from 'react';
import { Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { F } from '../theme';

const DEVELOPERS = [
  {
    name: 'مجتبی خوشحال',
    role: 'برنامه‌نویسی و توسعه',
    photo: require('../../assets/dev-mojtaba.jpg'),
  },
  {
    name: 'امیر خوشحال',
    role: 'ایده‌پردازی و مارکتینگ',
    photo: require('../../assets/dev-amir.jpg'),
  },
];

export default function AboutScreen() {
  return (
    <ScrollView style={styles.root} contentContainerStyle={styles.content}>
      <Image source={require('../../assets/icon.png')} style={styles.logo} />
      <Text style={styles.brand}>DALLAS MUSIC</Text>
      <Text style={styles.tagline}>توسعه‌دهندگان</Text>
      {DEVELOPERS.map((d) => (
        <View key={d.name} style={styles.card}>
          <Image source={d.photo} style={styles.photo} />
          <Text style={styles.name}>{d.name}</Text>
          <Text style={styles.role}>{d.role}</Text>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0b0b0f' },
  content: { alignItems: 'center', padding: 24, gap: 10 },
  logo: { width: 96, height: 96, borderRadius: 22 },
  brand: { ...F.bold, color: '#fff', fontSize: 22, letterSpacing: 2 },
  tagline: { ...F.reg, color: '#9a9aad', fontSize: 15, marginBottom: 10 },
  card: {
    width: '100%',
    backgroundColor: '#17171f',
    borderWidth: 1,
    borderColor: '#2a2a33',
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    marginTop: 10,
  },
  photo: { width: '100%', height: 320, borderRadius: 12 },
  name: { ...F.bold, color: '#fff', fontSize: 19, marginTop: 14 },
  role: { ...F.reg, color: '#ffb800', fontSize: 15, marginTop: 4 },
});
