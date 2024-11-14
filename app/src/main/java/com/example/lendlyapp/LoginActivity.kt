package com.example.lendlyapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.lendlyapp.databinding.ActivityLoginBinding
import com.example.lendlyapp.utils.ThemeHelper

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateThemeIcon()
        
        binding.themeToggleButton.setOnClickListener {
            ThemeHelper.toggleTheme(this)
            updateThemeIcon()
        }

        // Add animations
        binding.logoImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.appNameText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.welcomeText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.subtitleText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        binding.formCard.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        
        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateThemeIcon() {
        binding.themeToggleButton.setImageResource(
            if (ThemeHelper.isDarkMode(this)) 
                R.drawable.ic_light_mode 
            else 
                R.drawable.ic_dark_mode
        )
    }
}
