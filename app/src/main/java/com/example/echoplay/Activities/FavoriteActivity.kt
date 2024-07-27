package com.example.echoplay.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echoplay.Adapters.FavAdapter
import com.example.echoplay.POJO.Music
import com.example.echoplay.POJO.checkSongExistence
import com.example.echoplay.R
import com.example.echoplay.Utils.AdHelper.loadBannerAd
import com.example.echoplay.Utils.Constants
import com.example.echoplay.databinding.ActivityFavoriteBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class FavoriteActivity : AppCompatActivity() {
    companion object{
        lateinit var binding : ActivityFavoriteBinding
        var favMusicList: ArrayList<Music> = ArrayList()
        lateinit var favAdapter: FavAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       try {
            favMusicList = checkSongExistence(favMusicList)
        }catch (e: Exception){
            Log.d("FavoriteActivity", "Error: ${e.message.toString()}")
       }
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtn.setOnClickListener {
           finish()
        }
        setUpAdapter()

        binding.favShuffle.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("position", 0)
            intent.putExtra(Constants.INTENT_CLASS, "FavoriteActivity")
            startActivity(intent)
        }
        loadBannerAd(this, binding.favBannerAd)
    }


    override fun onResume() {
        super.onResume()
        setUpAdapter()
        try {
            favMusicList = checkSongExistence(favMusicList)
        }catch (e: Exception){
            Log.d("FavoriteActivity", "Error: ${e.message.toString()}")
        }
        if (favMusicList.isEmpty()){
            binding.favShuffle.visibility = android.view.View.GONE
            binding.noFavSongs.visibility = android.view.View.VISIBLE
        }else{
            binding.favShuffle.visibility = android.view.View.VISIBLE
            binding.noFavSongs.visibility = android.view.View.GONE
        }
    }

    private fun setUpAdapter() {
        favMusicList.reverse()
        favAdapter = FavAdapter(this, favMusicList)
        binding.favoriteRecycler.layoutManager = LinearLayoutManager(this)
        binding.favoriteRecycler.adapter = favAdapter
    }

}