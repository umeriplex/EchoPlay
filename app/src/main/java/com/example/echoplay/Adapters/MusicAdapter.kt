package com.example.echoplay.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoplay.Activities.MainActivity
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.Activities.PlaylistActivity
import com.example.echoplay.Activities.PlaylistDetailsActivity
import com.example.echoplay.POJO.Music
import com.example.echoplay.POJO.getFormattedDuration
import com.example.echoplay.POJO.updateSharedPreferences
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.MusicViewBinding

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
    private val playlistDetails: Boolean = false,
    private val selectionActivity: Boolean = false

) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {


    class MusicViewHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val musicName = binding.songTitle
        val album = binding.album
        val duration = binding.songDuraion
        val image = binding.musicImage
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        if (music.musicName.length > 20) {
            holder.musicName.text = music.musicName.substring(0, 20) + "..."
        } else {
            holder.musicName.text = music.musicName
        }
        if (music.album == "<unknown>") {
            holder.album.text = "Unknown Album"
        } else {
            holder.album.text = music.album
        }
        holder.duration.text = getFormattedDuration(music.duration)

        Glide.with(context)
            .load(music.imageUri)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.image)

        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    when{
                        musicList[position].id == PlayerActivity.nowPlayingId -> {
                            sendIntent("NowPlayingFragment", PlayerActivity.musicPosition)
                        }

                        else -> {
                            sendIntent("PlaylistDetailsAdapter", position)
                        }

                    }
                }
                holder.root.setOnLongClickListener {
                    val alertDialog = android.app.AlertDialog.Builder(context)
                    alertDialog.setTitle("Remove")
                    alertDialog.setMessage("Are you sure you want to remove this song from the playlist?")
                    alertDialog.setPositiveButton("Yes") { dialog, which ->
                        PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist.removeAt(position)
                        notifyDataSetChanged()
                        refreshPlaylist()
                        updateSharedPreferences(context)
                        (context as PlaylistDetailsActivity).updateUi()
                        Toast.makeText(context, "Song Removed", Toast.LENGTH_SHORT).show()
                    }
                    alertDialog.setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    alertDialog.show()
                    true
                }
            }
            selectionActivity -> {
                holder.root.setOnClickListener {
                    if (addSong(musicList[position])) {
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.secondary_color))
                    }
                }
            }

            else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent("MusicAdapterSearch", position)
                        musicList[position].id == PlayerActivity.nowPlayingId -> {
                            sendIntent("NowPlayingFragment", PlayerActivity.musicPosition)
                        }

                        else -> {
                            sendIntent("MusicAdapter", position)
                        }
                    }

                }
            }
        }


    }

    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                Toast.makeText(context, "Song already added in your playlist", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist.add(song)
        return true
    }

    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    fun updateSortMusicList(newMusicList: ArrayList<Music>) {
        musicList = newMusicList
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, position: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra(Constants.INTENT_POSITION, position)
        intent.putExtra(Constants.INTENT_CLASS, ref)
        context.startActivity(intent)
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }


}