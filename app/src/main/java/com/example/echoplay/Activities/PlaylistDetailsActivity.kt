package com.example.echoplay.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.echoplay.Adapters.MusicAdapter
import com.example.echoplay.POJO.MusicPlaylist
import com.example.echoplay.POJO.checkSongExistence
import com.example.echoplay.POJO.updateSharedPreferences
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.ActivityPlaylistDetailsBinding
import com.google.gson.GsonBuilder

class PlaylistDetailsActivity : AppCompatActivity(){
    lateinit var binding: ActivityPlaylistDetailsBinding

    companion object{
        var currentPlaylistPosition = -1
        lateinit var playlistDetailsAdapter: MusicAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPosition = intent.extras?.get("index") as Int
        try {
            PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist =
                checkSongExistence(PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist)
        } catch (e: Exception) {
            Log.d("PlaylistDetailsActivityError", "Error: ${e.message.toString()}")
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.playlistDetailsTitle.isSelected = true
        binding.playlistDetailsTitle.text = PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlistName

        binding.shufflePlaylistBtn.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("position", 0)
            intent.putExtra(Constants.INTENT_CLASS, "PlaylistDetailsActivity")
            startActivity(intent)
        }
        binding.addPlaylistBtn.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            intent.putExtra(Constants.INTENT_CLASS, "PlaylistDetailsActivity")
            startActivity(intent)
        }

        binding.removePlaylistBtn.setOnClickListener {
            val alertDialog = android.app.AlertDialog.Builder(this)
            alertDialog.setTitle("Remove")
            alertDialog.setMessage("Are you sure you want to remove all songs from this playlist?")
            alertDialog.setPositiveButton("Yes") { dialog, which ->
                PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist.clear()
                playlistDetailsAdapter.refreshPlaylist()
                binding.shufflePlaylistBtn.visibility = android.view.View.GONE
                binding.noPlaylistText.visibility = android.view.View.VISIBLE
                binding.playlistDetTotalSongs.text = "Total 0 Songs"
            }
            alertDialog.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            if (PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist.isNotEmpty()) {
                alertDialog.show()
            }else{
                Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
            }

        }

        setupAdapter()
    }

    private fun setupAdapter() {
        binding.playlistDetailsRv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        playlistDetailsAdapter = MusicAdapter(this, PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist, true)
        binding.playlistDetailsRv.adapter = playlistDetailsAdapter
    }

    fun updateUi(){
        binding.playlistDetName.isSelected = true
        binding.playlistDetTotalSongs.isSelected = true
        binding.playlistDetDate.isSelected = true
        binding.playlistDetName.text = "~${ PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlistName }"
        binding.playlistDetTotalSongs.text = "Total ${ playlistDetailsAdapter.itemCount} Songs"
        val date = PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].dateCreated
        val formateDate = date.split(" ")
        binding.playlistDetDate.text = "Created On: ${formateDate }"

        if (PlaylistActivity.musicPlaylist[0].ref[currentPlaylistPosition].playlist.isEmpty()) {
            binding.shufflePlaylistBtn.visibility = android.view.View.GONE
            binding.noPlaylistText.visibility = android.view.View.VISIBLE
        } else {
            binding.shufflePlaylistBtn.visibility = android.view.View.VISIBLE
            binding.noPlaylistText.visibility = android.view.View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
        playlistDetailsAdapter.notifyDataSetChanged()
        updateSharedPreferences(this)
        // store playlist in shared preferences
        val editor = getSharedPreferences(Constants.SharedPreferences_name, MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString(Constants.PLAYLIST_MUSIC, jsonStringPlaylist)
        editor.apply()
    }
}