package me.wierdest.affirmative.database

import androidx.room.Embedded
import androidx.room.Relation

data class SnippetWithSnips(
    @Embedded
    val snippet: Snippet,
    @Relation(
        parentColumn = "snippetId",
        entityColumn = "snippetCreatorId"
    )
    var snips: List<Snip>
)