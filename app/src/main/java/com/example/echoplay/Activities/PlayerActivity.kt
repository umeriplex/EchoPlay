package com.example.echoplay.Activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoplay.Fragments.NowPlayingFragment
import com.example.echoplay.POJO.Music
import com.example.echoplay.POJO.checkFav
import com.example.echoplay.POJO.closeApplication
import com.example.echoplay.POJO.getFormattedDuration
import com.example.echoplay.POJO.setSongPosition
import com.example.echoplay.R
import com.example.echoplay.Services.MusicService
import com.example.echoplay.Utils.AdHelper
import com.example.echoplay.Utils.AdHelper.showRandomTimeAds
import com.example.echoplay.Utils.Constants
import com.example.echoplay.Utils.Constants.SELECTED_THEME
import com.example.echoplay.databinding.ActivityPlayerBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    private var interstitialAd: InterstitialAd? = null

    companion object {
        lateinit var binding: ActivityPlayerBinding
        lateinit var musicListPlayerActivity: ArrayList<Music>
        var musicPosition = 0
        var musicService: MusicService? = null
        var repeat = false
        var min_15 = false
        var min_30 = false
        var min_60 = false
        var nowPlayingId = ""
        var isFavorite = false
        var favIndex = -1
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intentWorks()
        loadInterstitialAd()

        binding.themeBtn.setOnClickListener {
            val intent = Intent(this, CustomThemeActivity::class.java)
            startActivity(intent)
        }
        binding.playPauseBtn.setOnClickListener {
            if (musicService?.mediaPlayer?.isPlaying == true) startSong() else pauseSong()
        }

        binding.playNextBtn.setOnClickListener {
            playPrevNextSong(true)
        }

        binding.playPreviousBtn.setOnClickListener {
            playPrevNextSong(false)
        }

        binding.seekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit


            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit

        })

        binding.repeatMusicBtn.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatMusicBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
                Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show()
            }else{
                repeat = false
                binding.repeatMusicBtn.setColorFilter(ContextCompat.getColor(this, R.color.grey))
                Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
            if (showRandomTimeAds()) showInterstitialAd()
        }


        binding.songEqualizerBtn.setOnClickListener {
            try {
                val intentEq = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                intentEq.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService?.mediaPlayer?.audioSessionId)
                intentEq.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                intentEq.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(intentEq, Constants.EQUALIZER_INTENT_ID)
            }catch (e: Exception){
                Toast.makeText(this, "No equalizer found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.timeBtn.setOnClickListener {
            val timer =  min_15 || min_30 || min_60
            if (!timer) showTimeBottomSheetDialog()
            else{
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Timer")
                alert.setMessage("Do you want to stop the timer?")
                alert.setPositiveButton("Yes"){ _, _ ->
                    min_15 = false
                    min_30 = false
                    min_60 = false
                    binding.timeBtn.setColorFilter(ContextCompat.getColor(this, R.color.grey))
                    Toast.makeText(this, "Timer Off", Toast.LENGTH_SHORT).show()
                }
                alert.setNegativeButton("No"){ _, _ ->

                }
                alert.create().show()
            }
        }

        binding.shareBtn.setOnClickListener {
            shareAudio()
        }

        binding.favouriteBtn.setOnClickListener {
            if (isFavorite) {
                // Ensure `favIndex` is valid before removing
                if (favIndex != -1) {
                    binding.favouriteBtn.setImageResource(R.drawable.favourite)
                    isFavorite = false
                    FavoriteActivity.favMusicList.removeAt(favIndex)
                    favIndex = -1 // Reset `favIndex` after removal
                }
            } else {
                binding.favouriteBtn.setImageResource(R.drawable.fav_fill)
                isFavorite = true
                FavoriteActivity.favMusicList.add(musicListPlayerActivity[musicPosition])
                // Update `favIndex` to the new item's index
                favIndex = FavoriteActivity.favMusicList.size - 1
            }
        }

        AdHelper.loadBannerAd(this, binding.playerBannerAd)

    }


    private fun shareAudio() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPlayerActivity[musicPosition].path))
        startActivity(Intent.createChooser(shareIntent, "Share Audio"))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playPrevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(true)
            playMusic()
        } else {
            setSongPosition(false)
            playMusic()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startSong() {
        musicService?.mediaPlayer?.pause()
        musicService?.showNotification(R.drawable.play,0F)
        binding.playPauseBtn.setImageResource(R.drawable.play)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun pauseSong() {
        musicService?.mediaPlayer?.start()
        musicService?.showNotification(R.drawable.pause,1F)
        binding.playPauseBtn.setImageResource(R.drawable.pause)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playMusic() {
        try {
            if (musicService?.mediaPlayer == null) {
                musicService?.mediaPlayer = MediaPlayer()
            } else {
                musicService?.mediaPlayer?.reset()
            }
            manageLayout() // Ensure this method updates the UI correctly
            musicService?.mediaPlayer?.setDataSource(musicListPlayerActivity[musicPosition].path)
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()
            musicService?.showNotification(R.drawable.pause,1F)
            binding.playPauseBtn.setImageResource(R.drawable.pause)

            binding.durationPlayed.text = getFormattedDuration(musicService?.mediaPlayer?.currentPosition!!.toLong())
            binding.totalDuration.text = getFormattedDuration(musicService?.mediaPlayer?.duration!!.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService?.mediaPlayer?.duration!!
            musicService?.mediaPlayer?.setOnCompletionListener(this)
            nowPlayingId = musicListPlayerActivity[musicPosition].id
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing music: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun intentWorks() {
        musicPosition = intent.getIntExtra(Constants.INTENT_POSITION, 0)
        when (intent.getStringExtra(Constants.INTENT_CLASS)) {


            "PlaylistDetailsActivity" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist)
                musicListPlayerActivity.shuffle()
            }

            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(PlaylistActivity.musicPlaylist[0].ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist)
            }

            "FavoriteAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(FavoriteActivity.favMusicList)

            }  "FavoriteActivity" -> {

            val intent = Intent(this, MusicService::class.java)
            bindService(intent, this, BIND_AUTO_CREATE)
            startService(intent)
            musicListPlayerActivity = ArrayList()
            musicListPlayerActivity.addAll(FavoriteActivity.favMusicList)
            musicListPlayerActivity.shuffle()
        }

            "NowPlayingFragment" -> {
                manageLayout()
                musicService?.seekbarSetter()

                if (musicService?.mediaPlayer?.isPlaying == true) {
                    binding.playPauseBtn.setImageResource(R.drawable.pause)
                } else {
                    binding.playPauseBtn.setImageResource(R.drawable.play)
                }
            }

            "MusicAdapterSearch" -> {

                // for start service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.musicListFilterMA)

            }
            "MusicAdapter" -> {

                // for start service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.musicListMainActivity)

            }

            "MainActivity" -> {
                // for start service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPlayerActivity = ArrayList()
                musicListPlayerActivity.addAll(MainActivity.musicListMainActivity)
                musicListPlayerActivity.shuffle()

            }
        }
    }

    private fun manageLayout() {
        try {
            favIndex = checkFav(musicListPlayerActivity[musicPosition].id)


                Glide.with(applicationContext)
                    .load(musicListPlayerActivity[musicPosition].imageUri)
                    .apply(RequestOptions().placeholder(R.drawable.logo))
                    .into(binding.musicImage)


            binding.songName.text = musicListPlayerActivity[musicPosition].musicName
            if (repeat) {
                binding.repeatMusicBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
            }
            if (min_15 || min_30 || min_60) {
                binding.timeBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
            }
            if (isFavorite) binding.favouriteBtn.setImageResource(R.drawable.fav_fill)
            else binding.favouriteBtn.setImageResource(R.drawable.favourite)
        } catch (e: Exception) {
            Toast.makeText(this, "Error managing layout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showTimeBottomSheetDialog(){
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.timer_bottom_sheet_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
        bottomSheetDialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timeBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
            min_15 = true
            Thread{
                Thread.sleep(15 * 60000)
                if (min_15) closeApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timeBtn.setColorFilter(ContextCompat.getColor(this, R.color.secondary_color))
            min_30 = true
            Thread{
                Thread.sleep(30 * 60000)
                if (min_30) closeApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timeBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
            min_60 = true
            Thread{
                Thread.sleep(60 * 60000)
                if (min_60) closeApplication()
            }.start()
            bottomSheetDialog.dismiss()
        }
    }

    // service override functions
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.getCurrentService()
        musicService?.let {
            playMusic()
            it.seekbarSetter()
            binding.seekBar.max = it.mediaPlayer?.duration ?: 0
            handler.post(updateSeekBar)
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCompletion(p0: MediaPlayer?) {

        try {
            setSongPosition(true)
            playMusic()
            NowPlayingFragment.updateUI()
        }catch (e: Exception){
            Log.d("Error Ui", "Error: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Constants.EQUALIZER_INTENT_ID && requestCode == RESULT_OK){
            return
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            musicService?.mediaPlayer?.let {
                binding.seekBar.progress = it.currentPosition
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        applySelectedTheme()
    }

    private fun applySelectedTheme() {
        val sharedPreferences = getSharedPreferences(Constants.SharedPreferences_name, Context.MODE_PRIVATE)
        val selectedTheme = sharedPreferences.getInt(SELECTED_THEME, R.drawable.theme_default)

        // Set the background of the player activity
        binding.root.setBackgroundResource(selectedTheme)
    }



    // ads

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
                    setupAdCallbacks()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    // Handle the error
                    interstitialAd = null
                }
            })
    }

    private fun showInterstitialAd() {
        interstitialAd?.show(this) ?: run {
            // If the ad isn't loaded yet, you can either retry or skip showing the ad
            // For example, you can load a new ad here
            loadInterstitialAd()
        }
    }


    private fun setupAdCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // The ad was dismissed, load a new ad
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Handle the error

            }

            override fun onAdShowedFullScreenContent() {
                // The ad was shown, perform any actions needed
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBar)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (showRandomTimeAds()) showInterstitialAd()
    }


}
