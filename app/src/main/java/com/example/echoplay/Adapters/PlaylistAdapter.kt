package com.example.echoplay.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.echoplay.Activities.PlaylistActivity
import com.example.echoplay.Activities.PlaylistDetailsActivity
import com.example.echoplay.POJO.Playlist
import com.example.echoplay.POJO.updateSharedPreferences
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.PlaylistViewBinding
import com.google.gson.GsonBuilder

class PlaylistAdapter(private val context: Context, private var playlist: ArrayList<Playlist>): RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {
        val playlistName = binding.playlistName
        val delete = binding.deletePlaylistBtn
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return playlist.size
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.playlistName.text = playlist[position].playlistName
        holder.playlistName.isSelected = true
        holder.delete.setOnClickListener {
            val alert = AlertDialog.Builder(context)
            alert.setTitle("Delete Playlist")
            alert.setMessage("Are you sure you want to delete '${ playlist[position].playlistName}' playlist?")
            alert.setPositiveButton("Yes") { dialog, which ->
                PlaylistActivity.musicPlaylist[0].ref.removeAt(position)
                refreshList()
                updateSharedPreferences(context)
                (context as PlaylistActivity).updateNoPlaylistTextVisibility()
            }
            alert.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            alert.show()
        }
        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetailsActivity::class.java)
            intent.putExtra("index", position)
            context.startActivity(intent)
        }
    }

    fun refreshList() {
        playlist = ArrayList()
        if (PlaylistActivity.musicPlaylist.isNotEmpty()) {
            playlist.addAll(PlaylistActivity.musicPlaylist[0].ref)
        }
        notifyDataSetChanged()
    }

}