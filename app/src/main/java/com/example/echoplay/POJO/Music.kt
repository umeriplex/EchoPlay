package com.example.echoplay.POJO

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.echoplay.Activities.FavoriteActivity
import com.example.echoplay.Activities.PlayerActivity
import com.example.echoplay.Activities.PlaylistActivity
import com.example.echoplay.Utils.Constants
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

data class Music(
    var id : String,
    val musicName: String,
    val album: String,
    val duration: Long,
    val artist : String,
    val path : String,
    val imageUri: String
)

fun getImageArt(path:String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {

    if (!PlayerActivity.repeat){
        if (increment) {
            if (PlayerActivity.musicPosition == PlayerActivity.musicListPlayerActivity.size - 1) {
                PlayerActivity.musicPosition = 0
            } else {
                ++PlayerActivity.musicPosition
            }
        } else {
            if (PlayerActivity.musicPosition == 0) {
                PlayerActivity.musicPosition = PlayerActivity.musicListPlayerActivity.size - 1
            } else {
                --PlayerActivity.musicPosition
            }
        }
    }
}

fun getFormattedDuration(duration: Long): String {
    val minutes = duration / 1000 / 60
    val seconds = duration / 1000 % 60
    val hours = duration / 1000 / 3600
    return  if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun closeApplication(){
    val musicService = PlayerActivity.musicService
    val mediaPlayer = musicService?.mediaPlayer
    musicService?.stopForeground(true)
    mediaPlayer?.release()
    PlayerActivity.musicService = null
    exitProcess(1)
//    PlayerActivity.musicService!!.audioManager.abandonAudioFocus (PlayerActivity.musicService)

}

fun checkFav(id:String):Int{
    PlayerActivity.isFavorite = false
    FavoriteActivity.favMusicList.forEachIndexed { index, music ->
         if (music.id == id){
             PlayerActivity.isFavorite = true
             return index
          }

    }
    return -1

}
fun checkSongExistence(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists()){
            playlist.removeAt(index)
        }
    }
    return playlist

}



fun updateSharedPreferences(context: Context) {
    val editor = context.getSharedPreferences(Constants.SharedPreferences_name, Context.MODE_PRIVATE).edit()
    val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
    editor.putString(Constants.PLAYLIST_MUSIC, jsonStringPlaylist)
    editor.apply()
}




