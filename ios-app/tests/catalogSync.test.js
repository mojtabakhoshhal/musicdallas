// Jest tests for the shared-catalog sync logic (GitHub catalog.json schema).
const {
  normalizeCatalog,
  mergeTracks,
  isUpdateAvailable,
} = require('../src/utils/musicUtils');

describe('normalizeCatalog', () => {
  test('empty payload seeds zero tracks', () => {
    expect(normalizeCatalog([])).toEqual([]);
    expect(normalizeCatalog({ tracks: [] })).toEqual([]);
    expect(normalizeCatalog(null)).toEqual([]);
  });

  test('accepts the GitHub root catalog array schema', () => {
    const rows = normalizeCatalog([
      {
        id: 'x1', artist: 'یاس', artist_id: 'yas', title: 'سپر', album: '',
        source: 'rap3da', remoteUrl: 'https://example.com/separ.mp3',
        lyrics: null, cover: 'https://example.com/c.jpg', isNew: true,
      },
    ]);
    expect(rows).toHaveLength(1);
    expect(rows[0].remoteUrl).toContain('.mp3');
    expect(rows[0].isNew).toBe(true);
  });

  test('drops placeholder entries without a playable URL', () => {
    const rows = normalizeCatalog([
      { id: 'bad', artist: 'یاس', title: 'بدون لینک', remoteUrl: '' },
      { id: 'ok', artist: 'یاس', title: 'با لینک', remoteUrl: 'https://example.com/a.mp3' },
    ]);
    expect(rows.map((t) => t.id)).toEqual(['ok']);
  });
});

describe('remote merge preserving user state', () => {
  test('merge keeps existing entries (favorites/downloads live on them) and adds new ones', () => {
    const existing = [
      { id: '1', artist: 'یاس', title: 'باغ', local_path: '/data/bagh.mp3' },
    ];
    const incoming = normalizeCatalog([
      { id: '2', artist: 'ياس', title: 'باغ', remoteUrl: 'https://example.com/bagh.mp3' },
      { id: '3', artist: 'یاس', title: 'سپر', remoteUrl: 'https://example.com/separ.mp3' },
    ]);
    const merged = mergeTracks(existing, incoming);
    expect(merged).toHaveLength(2);
    expect(merged[0].local_path).toBe('/data/bagh.mp3');
    expect(merged.some((t) => t.title === 'سپر')).toBe(true);
  });

  test('playlist creation helpers keep order and dedupe', () => {
    const { addToPlaylist, removeFromPlaylist } = require('../src/utils/musicUtils');
    let ids = addToPlaylist([], 'a');
    ids = addToPlaylist(ids, 'b');
    ids = addToPlaylist(ids, 'a');
    expect(ids).toEqual(['a', 'b']);
    expect(removeFromPlaylist(ids, 'a')).toEqual(['b']);
  });
});

describe('version check', () => {
  test('update only when latestVersionCode increases', () => {
    expect(isUpdateAvailable(2, { latestVersionCode: 3 })).toBe(true);
    expect(isUpdateAvailable(2, { latestVersionCode: 2 })).toBe(false);
    expect(isUpdateAvailable(2, {})).toBe(false);
  });
});
