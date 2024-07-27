package com.example.echoplay.POJO

data class Playlist(
    val playlistName: String,
    var playlist: ArrayList<Music>,
    val createdBy: String,
    val dateCreated: String
)
data class MusicPlaylist(
    val ref : ArrayList<Playlist>,
)
