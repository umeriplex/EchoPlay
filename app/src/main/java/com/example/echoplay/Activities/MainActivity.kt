package com.example.echoplay.Activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.echoplay.Adapters.MusicAdapter
import com.example.echoplay.POJO.Music
import com.example.echoplay.POJO.MusicPlaylist
import com.example.echoplay.R
import com.example.echoplay.Utils.AdHelper
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var adapter: MusicAdapter
    private val STORAGE_PERMISSION_OLD = Manifest.permission.READ_EXTERNAL_STORAGE
    private val STORAGE_PERMISSION_NEW = Manifest.permission.READ_MEDIA_AUDIO
    private val STORAGE_PERMISSION_USER_SELECTED = Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    private val REQUEST_STORAGE_PERMISSION = 100

    companion object{
       lateinit var musicListMainActivity : ArrayList<Music>
        var musicListFilterMA: ArrayList<Music> = ArrayList()
       var search = false
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reqPermission()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAdapter()
        // for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }
        binding.favouriteBtn.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.about_nav -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }

                R.id.feedBack_nav -> {
                    val intent = Intent(this, FeedbackActivity::class.java)
                    startActivity(intent)
                }

                R.id.exit_nav -> {
                    // show alert before close
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Exit")
                    alertDialog.setMessage("Are you sure you want to exit?")
                    alertDialog.setIcon(R.drawable.exit)
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        val musicService = PlayerActivity.musicService

                        if (musicService == null) {
                            // Service was never started, close the app directly
                            finish()
                            exitProcess(1)
                        } else {
                            val mediaPlayer = musicService.mediaPlayer
                            if (mediaPlayer != null) {
                                if (mediaPlayer.isPlaying) {
                                    // Music is playing, just finish the activity and keep the service running
                                    finish()
                                } else {
                                    // Music is not playing, stop the service and release resources
                                    musicService.stopForeground(true)
                                    mediaPlayer.release()
                                    PlayerActivity.musicService = null
                                    finish()
                                    exitProcess(1)
//                                    PlayerActivity.musicService!!.audioManager.abandonAudioFocus (PlayerActivity.musicService)

                                }
                            } else {
                                // MediaPlayer is null, finish the activity
                                finish()
                                exitProcess(1)
                            }
                        }
                    }

                    alertDialog.setNegativeButton("No") { _, _ ->

                    }
                    alertDialog.create().show()
                }
            }
            true
        }
        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("position", 0)
            intent.putExtra(Constants.INTENT_CLASS, "MainActivity")
            startActivity(intent)
        }

        binding.sortBtn.setOnClickListener {
            val options = arrayOf("Sort by Title", "Sort by Duration")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sort Options")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> sortMusicListBy(Constants.SORT_BY_TITLE)
                    1 -> sortMusicListBy(Constants.SORT_BY_DURATION)
                }
            }
            builder.create().show()
        }

        val sharedPreferences = getSharedPreferences(Constants.SharedPreferences_name, MODE_PRIVATE)
        val sortPreference = sharedPreferences.getString("sortPreference", Constants.SORT_BY_DATE)
        sortMusicListBy(sortPreference!!)


        // get fav songs from shared preference
        FavoriteActivity.favMusicList = ArrayList()
        val jsonString = sharedPreferences.getString(Constants.FAVORITE_MUSIC, null)
        val typeToken = object : com.google.gson.reflect.TypeToken<ArrayList<Music>>() {}.type
        if (jsonString != null){
            val data : ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavoriteActivity.favMusicList.addAll(data)
        }

        // get playlist from shared preference
        PlaylistActivity.musicPlaylist = ArrayList()
        val jsonStringPlaylist = sharedPreferences.getString(Constants.PLAYLIST_MUSIC, null)
        val typeTokenPlaylist = object : com.google.gson.reflect.TypeToken<ArrayList<MusicPlaylist>>() {}.type
        if (jsonStringPlaylist != null){
            val data : ArrayList<MusicPlaylist> = GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
            PlaylistActivity.musicPlaylist.addAll(data)
        }

    }

    private fun sortMusicListBy(sortBy: String) {
        val editor = getSharedPreferences(Constants.SharedPreferences_name, MODE_PRIVATE).edit()
        editor.putString("sortPreference", sortBy)
        editor.apply()

        when (sortBy) {
            Constants.SORT_BY_TITLE -> {
                musicListMainActivity.sortBy { it.musicName.toLowerCase() }
            }
            Constants.SORT_BY_DURATION -> {
                musicListMainActivity.sortBy { it.duration }
            }
        }

        adapter.updateSortMusicList(musicListMainActivity)
    }

    private fun setUpAdapter() {
        search = false
        musicListMainActivity = loadAllAudio()
        binding.totalSongs.text = "Total Songs: ${musicListMainActivity.size}"
        adapter = MusicAdapter(this, musicListMainActivity)
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }


    private fun loadAllAudio(): ArrayList<Music> {
        val tempAudioList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
            )
        val cursor = contentResolver.query(uri, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC", null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(0)
                    val musicName = cursor.getString(1)
                    val album = cursor.getString(2)
                    val artist = cursor.getString(3)
                    val duration = cursor.getLong(4)
                    val path = cursor.getString(6)
                    val albumId = cursor.getString(7)
                    val uriImage = Uri.parse("content://media/external/audio/albumart")
                    val imageUri = Uri.withAppendedPath(uriImage, albumId).toString()
                    val file = java.io.File(path)
                    if (file.exists()){
                        tempAudioList.add(Music(id, musicName, album, duration, artist, path, imageUri))
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            return tempAudioList
        } else {
            return tempAudioList
        }
    }



    // run time permission
    private fun reqPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // do nothing
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            setUpAdapter()
        } else {
            reqPermission()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onDestroy() {
        super.onDestroy()
        val musicService = PlayerActivity.musicService

        if (musicService == null) {
            // Service was never started, close the app directly
            exitProcess(1)
        } else {
            val mediaPlayer = musicService.mediaPlayer
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying) {
                    // Music is playing, close the app but keep the service running
                    return
                } else {
                    // Music is not playing, stop the service and release resources
                    musicService.stopForeground(true)
                    mediaPlayer.release()
                    PlayerActivity.musicService = null
//                    PlayerActivity.musicService!!.audioManager.abandonAudioFocus (PlayerActivity.musicService)

                }
            }
        }

        // Close the app
        exitProcess(1)

    }

    override fun onResume() {
        super.onResume()
        // for  storing fav songs

        val editor = getSharedPreferences(Constants.SharedPreferences_name, MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavoriteActivity.favMusicList)
        editor.putString(Constants.FAVORITE_MUSIC, jsonString)
        // for playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString(Constants.PLAYLIST_MUSIC, jsonStringPlaylist)
        editor.apply()

        if (musicListMainActivity.isEmpty()){
            binding.shuffleBtn.visibility = android.view.View.GONE
        }else{
            binding.shuffleBtn.visibility = android.view.View.VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu?.findItem(R.id.searchView)?.actionView as androidx.appcompat.widget.SearchView
        searchItem.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // If the search query is empty, show all songs
                    musicListFilterMA.clear()
                    musicListFilterMA.addAll(musicListMainActivity)
                    search = false
                } else {
                    // Filter songs based on the search query
                    musicListFilterMA.clear()
                    val userInput = newText.toLowerCase()
                    for (music in musicListMainActivity) {
                        if (music.musicName.toLowerCase().contains(userInput)) {
                            musicListFilterMA.add(music)
                        }
                    }
                    search = true
                }
                adapter.updateMusicList(musicListFilterMA)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}