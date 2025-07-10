package com.example.bookboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.bookboard.databinding.ActivityMainBinding
import com.example.bookboard.viewmodel.AuthViewModel
import com.example.bookboard.viewmodel.BookPostViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val bookPostViewModel: BookPostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up user change listener to refresh posts
        authViewModel.setOnUserChangedListener {
            bookPostViewModel.refreshPosts()
        }

        // Check if user is logged in
        authViewModel.currentUser.observe(this) { user ->
            if (user == null) {
                navController.navigate(R.id.loginFragment)
            }
        }
    }

}