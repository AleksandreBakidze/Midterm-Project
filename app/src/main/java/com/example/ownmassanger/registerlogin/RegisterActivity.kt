package com.example.ownmassanger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.ownmassanger.R
import com.example.ownmassanger.messages.LatestMassageActivity
import com.example.ownmassanger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_reg.setOnClickListener {
            val email = email_editText_reg.text.toString()
            val password = password_editText_reg.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all of the blanks", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("RegisterActivity", "Email is: " + email)
            Log.d("RegisterActivity", "Password: $password")

            //Firebase Authentication to create a user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    //else if successful
                    Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener{
                    Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }




        already_have_accaunt_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login accaunt")

            //launch login activity somehow
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        select_photo_button_reg.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null) {
            // proceed and check what the select image was ...
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_reg.setImageBitmap(bitmap)

            select_photo_button_reg.alpha = 0f

           // val bitmapDrawable = BitmapDrawable(bitmap)
           // select_photo_button_reg.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                //do some login here
            }
    }

    //save in firebase database
    private fun saveUserToFirebaseDatabase(profileImageUri: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_editText_reg.text.toString(), profileImageUri)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally i saved the user to Firebase Database :D")

                //After registration open new tab
                val intent = Intent (this, LatestMassageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}
