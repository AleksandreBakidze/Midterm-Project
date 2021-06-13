package com.example.ownmassanger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.ownmassanger.R
import com.example.ownmassanger.messages.LatestMassageActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Login", "Attempt login with email/password: $email/***")

            //for login
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    val intent = Intent(this, LatestMassageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                    //if login or pass is wrong
                .addOnFailureListener{
                    Log.d("LoginActivity", "Failed to login user: ${it.message}")
                    Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
        //change to register
        sing_in_text_view.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
