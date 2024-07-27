package com.example.echoplay.Fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.POJO.setSongPosition
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.FragmentNowPlayingBinding


class NowPlayingFragment : Fragment() {

    companion object{
        lateinit var binding : FragmentNowPlayingBinding

        fun updateUI() {
            if (::binding.isInitialized) {
                binding.nowPlayingSongName.isSelected = true
                binding.nowPlayingSongName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
                Glide.with(binding.root.context)
                    .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].imageUri)
                    .apply(RequestOptions().placeholder(R.drawable.logo))
                    .into(binding.nowPlayingImage)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.nowPlayingPlayPauseBtn.setOnClickListener {
            if (PlayerActivity.musicService?.mediaPlayer!!.isPlaying){
                pauseMusic()
            }else{
                playMusic()
            }
        }
        binding.nowPlayingNextBtn.setOnClickListener {
            setSongPosition(increment = true)
            playNextPrev()

        }
        binding.nowPlayingPrevBtn.setOnClickListener {
            setSongPosition(increment = false)
            playNextPrev()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra(Constants.INTENT_POSITION,  PlayerActivity.musicPosition)
            intent.putExtra(Constants.INTENT_CLASS, "NowPlayingFragment")
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()

        if(PlayerActivity.musicService?.mediaPlayer != null){
            binding.root.visibility = View.VISIBLE
            binding.nowPlayingSongName.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].imageUri)
                .apply(RequestOptions().placeholder(R.drawable.logo))
                .into(binding.nowPlayingImage)
            binding.nowPlayingSongName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
            if (PlayerActivity.musicService?.mediaPlayer!!.isPlaying) {
                binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause)
            }else{
                binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playMusic(){
        PlayerActivity.musicService?.mediaPlayer?.start()
        binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause)
        PlayerActivity.musicService?.showNotification(R.drawable.pause,1F)
        PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun pauseMusic(){
        PlayerActivity.musicService?.mediaPlayer?.pause()
        binding.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play)
        PlayerActivity.musicService?.showNotification(R.drawable.play,0F)
        PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playNextPrev(){
        PlayerActivity.musicService?.createMediaPlayer()

        PlayerActivity.binding.songName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName

        Glide.with(this)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].imageUri)
            .apply(RequestOptions().placeholder(R.drawable.logo))
            .into(binding.nowPlayingImage)
        binding.nowPlayingSongName.text = PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
        PlayerActivity.musicService?.showNotification(R.drawable.pause,1F)
        playMusic()
    }


}