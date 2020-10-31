package me.wierdest.affirmative.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MyViewModel(private val snippetData: SnippetDAO, application: Application) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val snippetHandler = SnippetHandler(snippetData, scope)

}