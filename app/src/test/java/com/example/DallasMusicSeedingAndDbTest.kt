package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CatalogSeeder
import com.example.data.DallasMusicDatabase
import com.example.data.DallasMusicRepository
import com.example.data.PersianCatalogNormalizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DallasMusicSeedingAndDbTest {

    private lateinit var context: Context
    private lateinit var db: DallasMusicDatabase
    private lateinit var repository: DallasMusicRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, DallasMusicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DallasMusicRepository(context, db.musicDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `empty database seeds bundled catalog with all 20 artists and valid discographies`() = runTest {
        assertEquals(0, db.musicDao().getTrackCount())

        val seededCount = repository.ensureCatalogSeeded()
        assertTrue("Expected at least 100 seeded tracks, got $seededCount", seededCount >= 100)

        // Second call must be idempotent
        assertEquals(0, repository.ensureCatalogSeeded())

        val artists = repository.artists.first()
        assertEquals("Expected exactly 20 artists", 20, artists.size)

        val expectedArtists = setOf(
            "علی سورنا", "بهرام نورایی", "شایع", "مهیار", "دایان",
            "امین تیجی", "محسن چاوشی", "امیر تتلو", "حصین", "حسین تیام",
            "حسام تیام", "بامداد", "یاس", "بیگ شگی", "گاد پوری",
            "سمی لون", "پیدار", "فرشاد", "سجاد شاهی", "مهراد هیدن",
        )
        assertEquals(expectedArtists, artists.map { it.artistName }.toSet())

        // Verify per-artist queries and strict artist separation
        val bahramTracks = repository.observeTracksByArtist("bahram_nouraei").first()
        assertTrue(bahramTracks.isNotEmpty())
        assertTrue(bahramTracks.all { it.artist == "بهرام نورایی" })

        val hosseinTiamTracks = repository.observeTracksByArtist("hossein_tiam").first()
        val hesamTiamTracks = repository.observeTracksByArtist("hesam_tiam").first()
        assertTrue(hosseinTiamTracks.isNotEmpty())
        assertTrue(hesamTiamTracks.isNotEmpty())
        assertTrue(hosseinTiamTracks.map { it.title }.intersect(hesamTiamTracks.map { it.title }.toSet()).isEmpty())

        val samiloneTracks = repository.observeTracksByArtist("samilone").first()
        assertTrue(samiloneTracks.isNotEmpty())
        assertTrue(samiloneTracks.all { it.artist == "سمی لون" })

        // Verify zero remixes or collections entered catalog
        val allTracks = repository.allTracks.first()
        assertTrue(allTracks.all { PersianCatalogNormalizer.isOriginalTrack(it.title) })
    }

    @Test
    fun `playlist creation adding tracks favorites and offline download work end to end`() = runTest {
        repository.ensureCatalogSeeded()
        val sorenaTracks = repository.observeTracksByArtist("ali_sorena").first()
        val track = sorenaTracks.first()

        // Create playlist and add track
        val playlistId = repository.createPlaylist("شب‌های تهران")
        repository.addTrackToPlaylist(playlistId, track.id)

        val playlists = repository.playlistsWithTracks.first()
        assertEquals(1, playlists.size)
        assertEquals("شب‌های تهران", playlists.first().playlist.name)
        assertEquals(listOf(track.id), playlists.first().tracks.map { it.id })

        // Favorite toggle
        repository.toggleFavorite(track.id)
        val favs = repository.favoriteTracks.first()
        assertTrue(favs.any { it.id == track.id })

        // Download toggle saves local offline file
        val downloaded = repository.toggleDownload(track)
        assertTrue(downloaded)
        val downloads = repository.downloadedTracks.first()
        assertEquals(1, downloads.size)
        assertTrue(!downloads.first().localFilePath.isNullOrBlank())
    }

    @Test
    fun `persian normalizer rejects remixes and normalizes arabic characters`() {
        assertEquals("بهرام نورایی", PersianCatalogNormalizer.normalizePersian("بهرام نورايي"))
        assertEquals("کالکشن", PersianCatalogNormalizer.normalizePersian("كالكشن"))
        assertEquals("بغلم کن", PersianCatalogNormalizer.cleanTitle("دانلود آهنگ دایان به نام بغلم كن"))
        assertFalse(PersianCatalogNormalizer.isOriginalTrack("ریمیکس بهترین آهنگها"))
        assertFalse(PersianCatalogNormalizer.isOriginalTrack("Mix Playlist 2025"))
        assertTrue(PersianCatalogNormalizer.isOriginalTrack("مجنون شهر"))

        val filtered = CatalogSeeder.parseCatalogJson(
            """
            [
              {"id":"t1","artist":"علي سورنا","artist_id":"ali_sorena","title":"مجنون شهر"},
              {"id":"t2","artist":"علي سورنا","artist_id":"ali_sorena","title":"ريميكس رپ فارسي"}
            ]
            """.trimIndent(),
        )
        assertEquals(1, filtered.size)
        assertEquals("علی سورنا", filtered.first().artist)
        assertEquals("مجنون شهر", filtered.first().title)
    }

    @Test
    fun `remote catalog sync adds new tracks and preserves user favorites and downloads`() = runTest {
        repository.ensureCatalogSeeded()
        val sorenaFirst = repository.observeTracksByArtist("ali_sorena").first().first()
        repository.toggleFavorite(sorenaFirst.id)
        repository.toggleDownload(sorenaFirst)

        val remoteJson = """
            [
              {
                "id": "${sorenaFirst.id}",
                "artist": "علی سورنا",
                "artist_id": "ali_sorena",
                "title": "${sorenaFirst.title}",
                "album": "آوار (۱۳۹۲)",
                "source": "github-cdn",
                "remoteUrl": "https://cdn.example.com/sorena/majnoon.mp3",
                "lyrics": "متن بروزرسانی شده",
                "cover": "drawable://img_cover_underground",
                "isNew": true
              },
              {
                "id": "ali_sorena_99",
                "artist": "علی سورنا",
                "artist_id": "ali_sorena",
                "title": "خون خورشید",
                "album": "تک‌آهنگ جدید",
                "source": "github-cdn",
                "remoteUrl": "",
                "lyrics": "قطعه جدید اضافه شده از گیت‌هاب",
                "cover": "drawable://img_cover_underground",
                "isNew": true
              },
              {
                "id": "bad_remix_01",
                "artist": "علی سورنا",
                "artist_id": "ali_sorena",
                "title": "ریمیکس غیرمجاز",
                "source": "github-cdn"
              }
            ]
        """.trimIndent()

        val parsed = CatalogSeeder.parseCatalogJson(remoteJson)
        val result = repository.mergeFetchedTracks(parsed)

        assertEquals(2, result.totalFetched)
        assertEquals(1, result.newlyAdded)
        assertEquals(1, result.updatedExisting)

        val updatedSorenaTracks = repository.observeTracksByArtist("ali_sorena").first()
        val refreshedFirst = updatedSorenaTracks.first { it.id == sorenaFirst.id }
        assertTrue("Favorite state must be preserved", refreshedFirst.isFavorite)
        assertTrue("Downloaded state must be preserved", refreshedFirst.isDownloaded)
        assertEquals("https://cdn.example.com/sorena/majnoon.mp3", refreshedFirst.remoteUrl)
        assertTrue("Newly synced track must appear", updatedSorenaTracks.any { it.id == "ali_sorena_99" })
        assertFalse("Remix must be rejected", updatedSorenaTracks.any { it.id == "bad_remix_01" })
    }
}
