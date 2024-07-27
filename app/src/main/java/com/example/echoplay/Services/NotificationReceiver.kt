package com.example.echoplay.Services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.Fragments.NowPlayingFragment
import com.example.echoplay.POJO.checkFav
import com.example.echoplay.POJO.setSongPosition
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver(){

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(p0: Context?, p1: Intent?) {
        when(p1?.action){
            Constants.PREVIOUS -> {
                // play previous song
                nextPrevSong(increment = false, p0!!)
            }
            Constants.PLAY -> {
                // play or pause song
                playMusic()
            }
            Constants.NEXT -> {
                // play next song
                nextPrevSong(increment = true, p0!!)
            }

            Constants.SEEK_FORWARD -> {
                seekMusic(forward = true)
            }
            Constants.SEEK_BACKWARD -> {
                seekMusic( forward = false)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun seekMusic(forward: Boolean) {
        PlayerActivity.musicService?.mediaPlayer?.let {
            val currentPosition = it.currentPosition
            val seekPosition = if (forward) {
                currentPosition + Constants.SEEK_INTERVAL
            } else {
                currentPosition - Constants.SEEK_INTERVAL
            }
            it.seekTo(seekPosition.coerceIn(0, it.duration))
            PlayerActivity.binding.seekBar.progress = it.currentPosition
        }
    }







    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playMusic() {
        PlayerActivity.musicService?.mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                PlayerActivity.musicService?.showNotification(R.drawable.play,0F)
                PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play)
                NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)
            } else {
                it.start()
                PlayerActivity.musicService?.showNotification(R.drawable.pause,1F)
                PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause)
                NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause)
            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun nextPrevSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService?.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].imageUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .into(PlayerActivity.binding.musicImage)

        PlayerActivity.binding.songName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].imageUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .into(NowPlayingFragment.binding.nowPlayingImage)
        NowPlayingFragment.binding.nowPlayingSongName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
        playMusic()
        PlayerActivity.favIndex = checkFav(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id)
        if (PlayerActivity.isFavorite) PlayerActivity.binding.favouriteBtn.setImageResource(R.drawable.fav_fill)
        else PlayerActivity.binding.favouriteBtn.setImageResource(R.drawable.favourite)

    }


}