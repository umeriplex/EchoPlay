package com.example.echoplay.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.echoplay.Adapters.PlaylistAdapter
import com.example.echoplay.POJO.MusicPlaylist
import com.example.echoplay.POJO.Playlist
import com.example.echoplay.R
import com.example.echoplay.Utils.AdHelper.loadBannerAd
import com.example.echoplay.databinding.ActivityPlaylistBinding
import com.example.echoplay.databinding.AddPlaylistLayoutBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError

class PlaylistActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlaylistBinding

    companion object {
        lateinit var playlistAdapter: PlaylistAdapter
        var musicPlaylist = ArrayList<MusicPlaylist>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        setupAdapter()
        binding.addPlaylistBtn.setOnClickListener {
            customAlertDialog()
        }
        binding.noPlaylistText.setOnClickListener {
            customAlertDialog()
        }


        loadBannerAd(this,binding.plalistBannerAd)
    }



    private fun setupAdapter() {
        if (musicPlaylist.isNotEmpty()) {
            musicPlaylist[0].ref.reverse()
            playlistAdapter = PlaylistAdapter(this, musicPlaylist[0].ref)
            binding.noPlaylistText.visibility = android.view.View.GONE
        } else {
            playlistAdapter = PlaylistAdapter(this, ArrayList())
            binding.noPlaylistText.visibility = android.view.View.VISIBLE
        }
        binding.playlistRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.playlistRecyclerView.adapter = playlistAdapter
    }

    private fun customAlertDialog() {
        val dialogView = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist_layout, binding.root, false)
        val binder = AddPlaylistLayoutBinding.bind(dialogView)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val playlistName = binder.addPlaylistName.text.toString()
            val createdBy = binder.addYourName.text.toString()

            if (playlistName.isEmpty() || createdBy.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (isPlaylistExist(playlistName)) {
                Toast.makeText(this, "Playlist already exists", Toast.LENGTH_SHORT).show()
            } else {
                addPlayList(playlistName, createdBy)
                dialog.dismiss()
                binding.noPlaylistText.visibility = android.view.View.GONE
            }
        }
    }

    private fun isPlaylistExist(name: String): Boolean {
        for (i in musicPlaylist) {
            for (playlist in i.ref) {
                if (playlist.playlistName == name) {
                    return true
                }
            }
        }
        return false
    }

    private fun addPlayList(name: String, createdBy: String) {
        val currentDate = java.util.Calendar.getInstance().time.toString()
        val date = currentDate.split(" ")
        val formattedDate = "${date[1]} ${date[2]} ${date[5]}"
        val tempPlaylist = Playlist(name, ArrayList(), createdBy, formattedDate)

        if (musicPlaylist.isEmpty()) {
            musicPlaylist.add(MusicPlaylist(arrayListOf(tempPlaylist)))
        } else {
            musicPlaylist[0].ref.add(tempPlaylist)
        }

        playlistAdapter.refreshList()
    }

    fun updateNoPlaylistTextVisibility() {
        if (musicPlaylist.isNotEmpty() && musicPlaylist[0].ref.isEmpty()) {
            binding.noPlaylistText.visibility = android.view.View.VISIBLE
        } else {
            binding.noPlaylistText.visibility = android.view.View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateNoPlaylistTextVisibility()
        playlistAdapter.refreshList()
    }
}
