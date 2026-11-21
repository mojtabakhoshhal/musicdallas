// Plain-node tests for the pure logic layer. Run: npm run test:logic
const assert = require('assert');
const {
  normalizeFa,
  trackKey,
  mergeTracks,
  searchTracks,
  addToPlaylist,
  removeFromPlaylist,
  moveInPlaylist,
} = require('../src/utils/musicUtils');

// normalizeFa: Arabic yeh/kaf unify with Persian
assert.strictEqual(normalizeFa('علي سورنا'), normalizeFa('علی سورنا'));
assert.strictEqual(normalizeFa('  آهنگ   جديد '), 'آهنگ جدید');

// trackKey: same artist/title in different orthography = same key
assert.strictEqual(
  trackKey({ artist: 'حسين تي ام', title: 'F22' }),
  trackKey({ artist: 'حسین تی ام', title: 'f22' })
);

// mergeTracks: dedupe across sources, existing wins
const existing = [{ id: '1', artist: 'یاس', title: 'باغ', local_path: '/x.mp3' }];
const incoming = [
  { id: '2', artist: 'ياس', title: 'باغ' },
  { id: '3', artist: 'یاس', title: '۱۴۰۴' },
];
const merged = mergeTracks(existing, incoming);
assert.strictEqual(merged.length, 2);
assert.strictEqual(merged[0].local_path, '/x.mp3');

// searchTracks: orthography-insensitive
assert.strictEqual(searchTracks(merged, 'ياس').length, 2);
assert.strictEqual(searchTracks(merged, 'باغ').length, 1);
assert.strictEqual(searchTracks(merged, 'تتلو').length, 0);

// playlist ops
let ids = addToPlaylist([], 'a');
ids = addToPlaylist(ids, 'b');
ids = addToPlaylist(ids, 'a'); // duplicate ignored
assert.deepStrictEqual(ids, ['a', 'b']);
ids = moveInPlaylist(ids, 1, 0);
assert.deepStrictEqual(ids, ['b', 'a']);
ids = removeFromPlaylist(ids, 'b');
assert.deepStrictEqual(ids, ['a']);

console.log('musicUtils: ALL TESTS PASSED');
