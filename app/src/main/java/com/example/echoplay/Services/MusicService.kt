package com.example.echoplay.Services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.echoplay.Activities.MainActivity
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.Fragments.NowPlayingFragment
import com.example.echoplay.POJO.getFormattedDuration
import com.example.echoplay.POJO.getImageArt
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener{
    private lateinit var runnable: Runnable
    private lateinit var mediaSession: MediaSessionCompat
    private val myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    lateinit var audioManager: AudioManager
    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun getCurrentService(): MusicService {
            return this@MusicService
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {


        val forwardIntent = Intent(applicationContext, NotificationReceiver::class.java).setAction(Constants.SEEK_FORWARD)
        val forwardPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, forwardIntent, PendingIntent.FLAG_IMMUTABLE)

        val backwardIntent = Intent(applicationContext, NotificationReceiver::class.java).setAction(Constants.SEEK_BACKWARD)
        val backwardPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, backwardIntent, PendingIntent.FLAG_IMMUTABLE)


        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.PLAY)
        val playPausePendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val imageArt =
            getImageArt(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].path)
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.logo)
        }

        val pendingIntforOpenApp = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            baseContext,
            0,
            pendingIntforOpenApp,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(baseContext, Constants.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName)
            .setContentText(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].album)
            .setSmallIcon(R.drawable.icon_transparent)
            .setLargeIcon(image)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)

            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.previous, "Previous", prevPendingIntent)
            .addAction(R.drawable.back_arrow, "Backward", backwardPendingIntent)
            .addAction(playPauseBtn, "Pause", playPausePendingIntent)
            .addAction(R.drawable.forward_arrow, "Forward", forwardPendingIntent)
            .addAction(R.drawable.next, "Next", nextPendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check notification permission
            val notificationManager = NotificationManagerCompat.from(this)
            if (notificationManager.areNotificationsEnabled()) {
                startForeground(Constants.FORE_GROUND_SERVICE_ID, notification)
            } else {
                // Guide user to enable notifications in Settings
                Toast.makeText(this, "Notifications are disabled. Please enable them in Settings.", Toast.LENGTH_LONG).show();
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun createMediaPlayer() {

        if (PlayerActivity.musicService?.mediaPlayer == null) {
            PlayerActivity.musicService?.mediaPlayer = MediaPlayer()
        } else {
            PlayerActivity.musicService?.mediaPlayer?.reset()
        }
        PlayerActivity.musicService?.mediaPlayer?.setDataSource(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].path)
        PlayerActivity.musicService?.mediaPlayer?.prepare()
        PlayerActivity.musicService?.showNotification(R.drawable.pause,0f)
        PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause)
        PlayerActivity.binding.durationPlayed.text = getFormattedDuration( PlayerActivity.musicService?.mediaPlayer?.currentPosition!!.toLong())
        PlayerActivity.binding.totalDuration.text = getFormattedDuration( PlayerActivity.musicService?.mediaPlayer?.duration!!.toLong())
        PlayerActivity.binding.seekBar.progress  = 0
        PlayerActivity.binding.seekBar.max = PlayerActivity.musicService?.mediaPlayer?.duration!!
        PlayerActivity.nowPlayingId = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id


    }

    fun seekbarSetter(){
        runnable = Runnable {
            PlayerActivity.binding.durationPlayed.text = getFormattedDuration( mediaPlayer?.currentPosition!!.toLong())
            PlayerActivity.binding.totalDuration.text = getFormattedDuration( mediaPlayer?.duration!!.toLong())
            PlayerActivity.binding.seekBar.progress = mediaPlayer?.currentPosition!!
            PlayerActivity.binding.seekBar.max = mediaPlayer?.duration!!
            PlayerActivity.binding.seekBar.postDelayed(runnable, 1000)
        }
        PlayerActivity.binding.seekBar.postDelayed(runnable, 0)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onAudioFocusChange(p0: Int) {
        when (p0) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play)
                    showNotification(R.drawable.play,0F)
                    NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play)
                    showNotification(R.drawable.play,0F)
                    NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)

                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.setVolume(0.1f, 0.1f)
                }
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause)
                    showNotification(R.drawable.pause,1F)
                    NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause)
                    showNotification(R.drawable.pause,1F)
                    NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.setVolume(1f, 1f)
                }
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play)
                    showNotification(R.drawable.play,0F)
                    NowPlayingFragment.binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)
                }
            }


        }

    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.abandonAudioFocus(this)
    }
}