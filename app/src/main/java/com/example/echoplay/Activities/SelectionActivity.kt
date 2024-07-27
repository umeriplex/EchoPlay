package com.example.echoplay.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.echoplay.Adapters.MusicAdapter
import com.example.echoplay.R
import com.example.echoplay.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    companion object{
        lateinit var binding: ActivitySelectionBinding
        lateinit var selectionAdapter: MusicAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        setupAdapter()
        initializeSearchView()
    }

    private fun setupAdapter() {
        binding.selectionRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        // Use MainActivity's music list directly for the adapter
        selectionAdapter = MusicAdapter(this, MainActivity.musicListMainActivity, false, true)
        binding.selectionRecyclerView.adapter = selectionAdapter
    }

    private fun initializeSearchView(){
        binding.selectionSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicListFilterMA = ArrayList()
                if (newText!!.isNotEmpty()){
                    val userInput = newText.toLowerCase()
                    for (music in MainActivity.musicListMainActivity){
                        if (music.musicName.toLowerCase().contains(userInput)){
                            MainActivity.musicListFilterMA.add(music)
                        }
                    }
                    MainActivity.search = true
                    selectionAdapter.updateMusicList(MainActivity.musicListFilterMA)
                } else {
                    // Reset the list when search query is empty
                    selectionAdapter.updateMusicList(MainActivity.musicListMainActivity)
                }
                return true
            }
        })
    }
}
