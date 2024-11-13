package com.example.lendlyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.lendlyapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.lendlyapp.utils.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        
        Log.d("MainActivity", "Current user: ${currentUser?.email}")
        
        if (currentUser == null) {
            Log.d("MainActivity", "No user found, redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_you, R.id.navigation_ongoing
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        updateThemeIcon(menu.findItem(R.id.action_toggle_theme))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                ThemeHelper.toggleTheme(this)
                updateThemeIcon(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateThemeIcon(menuItem: MenuItem) {
        menuItem.setIcon(
            if (ThemeHelper.isDarkMode(this)) 
                R.drawable.ic_light_mode 
            else 
                R.drawable.ic_dark_mode
        )
    }
}