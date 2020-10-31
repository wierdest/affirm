package me.wierdest.affirmative

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import me.wierdest.affirmative.Configuration.Companion.MY_AUDIO_PERMISSION
import me.wierdest.affirmative.database.MyDatabase
import me.wierdest.affirmative.database.MyViewModel
import me.wierdest.affirmative.database.MyViewModelFactory
import me.wierdest.affirmative.databinding.ActivityMainBinding
import me.wierdest.affirmative.settings.SettingsFragment
import me.wierdest.affirmative.title.TitleFragment
import me.wierdest.affirmative.utilities.currentNavigationFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = javaClass.name
    private lateinit var b: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var viewModel: MyViewModel
    private lateinit var navController: NavController
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        /**
         * Binding and drawer layout
         */
        b = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = b.drawerLayout
        /**
         * ViewModel
         */
        val libraryDAO = MyDatabase.getInstance(application).snippetDao
        val viewModelFactory = MyViewModelFactory(libraryDAO, application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MyViewModel::class.java)
        /**
         * App shared preferences
         */
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        /**
         * Navigation & Toolbar
         */
        navView = findViewById(R.id.navView)
        navView.setNavigationItemSelectedListener(this)
        navController = findNavController(R.id.myNavHostFragment)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, b.toolbar, R.string.nav_drawer_open, R.string.nav_drawer_closed)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        NavigationUI.setupWithNavController(b.toolbar, navController, drawerLayout)
        setSupportActionBar(b.toolbar)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.groupId == R.id.basicNav || item.groupId == R.id.extraNav) {
            // todo create the privacy fragment
            if (supportFragmentManager.currentNavigationFragment !is TitleFragment) {
                navController.popBackStack()
            }
            navController.navigate(item.itemId)
        } else {
            // not a navigation click, but an action like rate share or send
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        navView.setCheckedItem(R.id.menu_none)
        return NavigationUI.navigateUp(navController, drawerLayout)

    }

    private fun askRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), MY_AUDIO_PERMISSION)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop!")
        preferences.unregisterOnSharedPreferenceChangeListener(SettingsFragment().listener)
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume!")
        preferences.registerOnSharedPreferenceChangeListener(SettingsFragment().listener)
    }

}