import React, { useEffect } from 'react';
import { Alert, Linking, StyleSheet, View } from 'react-native';
import { NavigationContainer, useNavigation } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useFonts } from 'expo-font';
import { Ionicons } from '@expo/vector-icons';
import { F } from './src/theme';
import HomeScreen from './src/screens/HomeScreen';
import ArtistsScreen from './src/screens/ArtistsScreen';
import TracksScreen from './src/screens/TracksScreen';
import SearchScreen from './src/screens/SearchScreen';
import LibraryScreen from './src/screens/LibraryScreen';
import PlaylistsScreen from './src/screens/PlaylistsScreen';
import PlayerScreen from './src/screens/PlayerScreen';
import AboutScreen from './src/screens/AboutScreen';
import MiniPlayer from './src/components/MiniPlayer';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const headerOpts = {
  headerStyle: { backgroundColor: '#0b0b0f' },
  headerTintColor: '#fff',
  headerTitleStyle: { ...F.bold, fontSize: 17 },
  contentStyle: { backgroundColor: '#0b0b0f' },
};

function HomeStack() {
  return (
    <Stack.Navigator screenOptions={headerOpts}>
      <Stack.Screen name="HomeMain" component={HomeScreen} options={{ headerShown: false }} />
      <Stack.Screen name="Artists" component={ArtistsScreen} options={{ title: 'خواننده‌ها' }} />
      <Stack.Screen
        name="Tracks"
        component={TracksScreen}
        options={({ route }) => ({ title: route.params.artist })}
      />
      <Stack.Screen name="About" component={AboutScreen} options={{ title: 'توسعه‌دهندگان' }} />
    </Stack.Navigator>
  );
}

function SearchStack() {
  return (
    <Stack.Navigator screenOptions={headerOpts}>
      <Stack.Screen name="SearchMain" component={SearchScreen} options={{ title: 'جستجو' }} />
      <Stack.Screen
        name="Tracks"
        component={TracksScreen}
        options={({ route }) => ({ title: route.params.artist })}
      />
    </Stack.Navigator>
  );
}

function LibraryStack() {
  return (
    <Stack.Navigator screenOptions={headerOpts}>
      <Stack.Screen name="LibraryMain" component={LibraryScreen} options={{ title: 'کتابخانه' }} />
      <Stack.Screen name="Playlists" component={PlaylistsScreen} options={{ title: 'پلی‌لیست‌ها' }} />
      <Stack.Screen name="About" component={AboutScreen} options={{ title: 'توسعه‌دهندگان' }} />
    </Stack.Navigator>
  );
}

function Tabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarStyle: { backgroundColor: '#0e0e13', borderTopColor: '#23232c', height: 58, paddingBottom: 6 },
        tabBarActiveTintColor: '#ffb800',
        tabBarInactiveTintColor: '#8a8a9a',
        tabBarLabelStyle: { ...F.bold, fontSize: 12 },
      }}
    >
      <Tab.Screen
        name="HomeTab"
        component={HomeStack}
        options={{ title: 'خانه', tabBarIcon: ({ color, size }) => <Ionicons name="home" size={size} color={color} /> }}
      />
      <Tab.Screen
        name="SearchTab"
        component={SearchStack}
        options={{ title: 'جستجو', tabBarIcon: ({ color, size }) => <Ionicons name="search" size={size} color={color} /> }}
      />
      <Tab.Screen
        name="LibraryTab"
        component={LibraryStack}
        options={{ title: 'کتابخانه', tabBarIcon: ({ color, size }) => <Ionicons name="library" size={size} color={color} /> }}
      />
    </Tab.Navigator>
  );
}

function Shell() {
  const navigation = useNavigation();
  return (
    <View style={styles.shell}>
      <View style={styles.flex}>
        <Tabs />
      </View>
      <MiniPlayer navigation={navigation} />
    </View>
  );
}

const RootStack = createNativeStackNavigator();

export default function App() {
  const [fontsLoaded, fontError] = useFonts({
    Vazirmatn: require('./assets/Vazirmatn-Regular.ttf'),
    'Vazirmatn-Bold': require('./assets/Vazirmatn-Bold.ttf'),
  });
  useEffect(() => {
    // Seed the local catalog DB from the bundled catalog on first launch,
    // so the app has real content fully offline from the first second.
    import('./src/services/sync')
      .then(async (m) => {
        await m.ensureSeeded();
        // version.json check: prompt with the release link when a newer
        // build is published in mojtabakhoshhal/musicdallas.
        const info = await m.checkForUpdate();
        if (info) {
          const url = info.iosDownloadUrl || info.androidDownloadUrl ||
            'https://github.com/mojtabakhoshhal/musicdallas/releases/latest';
          Alert.alert(
            'نسخه جدید دالاس موزیک',
            `نسخه ${info.latestVersionName || ''} منتشر شده است.${info.releaseNotes ? '\n' + info.releaseNotes : ''}`,
            [
              { text: 'بعداً', style: 'cancel' },
              { text: 'دانلود نسخه جدید', onPress: () => Linking.openURL(url) },
            ]
          );
        }
      })
      .catch(() => {});
  }, []);
  if (!fontsLoaded && !fontError) {
    return <View style={styles.shell} />;
  }
  return (
    <NavigationContainer>
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        <RootStack.Screen name="Main" component={Shell} />
        <RootStack.Screen
          name="Player"
          component={PlayerScreen}
          options={{
            headerShown: true,
            title: 'در حال پخش',
            presentation: 'modal',
            ...headerOpts,
          }}
        />
      </RootStack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  shell: { flex: 1, backgroundColor: '#0b0b0f' },
  flex: { flex: 1 },
});
