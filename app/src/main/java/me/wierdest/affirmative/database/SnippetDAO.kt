package me.wierdest.affirmative.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SnippetDAO {
    // Insert, Update, Clear Snippets
    @Insert
    fun insert(item: Snippet)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg items: Snippet?)
    @Update
    fun update(item: Snippet)
    @Query("DELETE FROM snippet_table")
    fun clear()
    @Query("DELETE FROM snippet_table WHERE snippetId = :key")
    fun clearSnippet(key: Long)

    // Insert, Update, Clear Snips
    @Insert
    fun insertSnip(item: Snip)
    @Update
    fun updateSnip(item: Snip)
    @Update
    fun updateAll(vararg items: Snippet?)
    @Query("DELETE FROM snip_table")
    fun clearAllSnips()
    @Query("DELETE FROM snip_table WHERE snipId = :key")
    fun clearSnip(key: Long)

    // Get Snippet
    @Query("SELECT * FROM snippet_table ORDER BY snippetId")
    fun getAllSnippetsLive() : LiveData<List<Snippet>>
    @Query("SELECT * FROM snippet_table ORDER BY snippetId")
    fun getAllSnippets(): Array<out Snippet?>
    @Query("SELECT * FROM snippet_table ORDER BY snippetId DESC LIMIT 1")
    fun getLastSnippetLive() : LiveData<Snippet>
    @Query("SELECT * FROM snippet_table ORDER BY snippetId DESC LIMIT 1")
    fun getLastSnippet() : Snippet
    @Query("SELECT * FROM snippet_table WHERE snippetId = :key")
    fun getSnippet(key: Long) : Snippet?
    @Query("SELECT * FROM snippet_table WHERE raw = :source")
    fun getSnippetByRaw(source: String) : Snippet?

    // Get SnippetsWithSnips
    @Transaction
    @Query("SELECT * FROM snippet_table WHERE level =:key")
    fun getLastSnippetWithSnipsByLevel(key: String) : SnippetWithSnips?
    @Transaction
    @Query("SELECT * FROM snippet_table WHERE snippetId =:key")
    fun getSnippetWithSnipsById(key: Long) : SnippetWithSnips?
    @Transaction
    @Query("SELECT * FROM snippet_table")
    fun getAllSnippetWithSnipsLive() : LiveData<List<SnippetWithSnips>>
    @Transaction
    @Query("SELECT * FROM snippet_table ORDER BY snippetId DESC LIMIT 1")
    fun getLastSnippetWithSnips() : SnippetWithSnips?

}