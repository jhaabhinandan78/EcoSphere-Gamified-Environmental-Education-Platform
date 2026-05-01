package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Show splash for minimum time, then check user authentication
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAuthentication()
        }, 2000)
    }
    
    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            navigateToRoleSelection()
            return
        }

        // Add timeout for Firestore query
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            // Timeout occurred - navigate to login
            navigateToLogin()
        }
        
        // Set 10-second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 10000)

        firestore.collection("Users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                // Cancel timeout since we got a response
                timeoutHandler.removeCallbacks(timeoutRunnable)
                
                val role = document.getString("role")

                when (role) {
                    "student" -> {
                        startActivity(Intent(this, StudentMainActivity::class.java))
                        finish()
                    }
                    "admin" -> {
                        startActivity(Intent(this, AdminMainActivity::class.java))
                        finish()
                    }
                    else -> {
                        // Invalid role - sign out and go to role selection
                        FirebaseAuth.getInstance().signOut()
                        navigateToRoleSelection()
                    }
                }
            }
            .addOnFailureListener {
                // Cancel timeout and handle failure
                timeoutHandler.removeCallbacks(timeoutRunnable)
                navigateToLogin()
            }
    }
    
    private fun navigateToRoleSelection() {
        startActivity(Intent(this, RoleSelectionActivity::class.java))
        finish()
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
