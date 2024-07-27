package com.example.echoplay.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.POJO.Music
import com.example.echoplay.POJO.getFormattedDuration
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.MusicViewBinding

class FavAdapter(private val context: Context, val FavMusicList: ArrayList<Music>) : RecyclerView.Adapter<FavAdapter.FavViewHolder>() {

    class FavViewHolder(binding:MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val musicName = binding.songTitle
        val album = binding.album
        val duration = binding.songDuraion
        val image = binding.musicImage
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = MusicViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return FavViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return FavMusicList.size
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val music = FavMusicList[position]
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
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .into(holder.image)

        holder.root.setOnClickListener {
            // send intent]
            when {
                FavMusicList[position].id == PlayerActivity.nowPlayingId -> {
                    sendIntent("NowPlayingFragment", PlayerActivity.musicPosition)
                }

                else -> {
                    sendIntent("FavoriteAdapter", position)
                }
            }
            }
    }

    private fun sendIntent(ref: String, position: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra(Constants.INTENT_POSITION, position)
        intent.putExtra(Constants.INTENT_CLASS,ref)
        context.startActivity(intent)
    }

}