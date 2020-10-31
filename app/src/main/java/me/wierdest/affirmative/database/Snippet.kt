package me.wierdest.affirmative.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippet_table")
data class Snippet(
    @PrimaryKey(autoGenerate = true)
    var snippetId: Long = 0L,
    @ColumnInfo(name = "name") // of file or inferred from first tokens
    var name: String = "",  //
    @ColumnInfo(name = "isDialogue")
    var isDialogue: Boolean = false,
    @ColumnInfo(name = "level") // learning level
    var level: String = "",
    @ColumnInfo(name = "icon") // id of the drawable icon to be used
    var icon: Int = 0,
    @ColumnInfo(name = "topic")
    var topic: String = "",
    @ColumnInfo(name = "note")
    var note: String = "",
    @ColumnInfo(name = "keywords") // description maybe pronunciation notes
    var keywords: String = "",
    @ColumnInfo(name = "raw")
    var raw: String = "",
    @ColumnInfo(name = "author")
    var author: String = "",
    @ColumnInfo(name = "creationTimeMilli")
    val creationTimeMilli: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "lastAccessTimeMilli")
    var lastAccessTimeMilli: Long = 0L,
    @ColumnInfo(name = "readings")
    var readings: Int = 0, // reading count
    @ColumnInfo(name = "snipCount")
    var snipCount: Int = 0,
    @ColumnInfo(name = "hideMode")
    var hideMode: Char = 'T',
    @ColumnInfo(name = "charDelay")
    var charDelay: Long = 100L,
)
