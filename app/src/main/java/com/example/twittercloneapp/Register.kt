package com.example.twittercloneapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.*
import java.io.IOException
import java.util.*

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

//        auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage

        signInAnonymously()

        imageView_reg.setOnClickListener {
            // Load image from android file
            loadImage()
        }
        register.setOnClickListener {
            performRegister()

        }

    }


    fun register_btn(view: View) {
        signInAnonymously()
    }

    fun signInAnonymously() {

        auth.signInAnonymously().addOnCompleteListener(this) { task ->

            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("LoginInfo: ", task.isSuccessful.toString())

            } else {
                // If sign in fails, display a message to the user.
                Log.d("signInAnonymously: ", task.isSuccessful.toString())

                Log.d("signInAnonymously", task.exception.toString())
            }
        }
    }


    val PICK_IMAGE_CODE = 123
    fun loadImage() {
        // this should be call on setOnclickListener
        // open the file to view images
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_CODE)
        // after startActivityForResult(), onActivityResult override fun automatically call
    }

    // check the Build.VERSION.SDK then set the uri to bitmap and encode it, finally return bitmap
    @RequiresApi(Build.VERSION_CODES.P)
    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }

    // after startActivityForResult(), onActivityResult override fun automatically call
    // this always call outside of onCreate because of override fun
    // allow to select the image

    var selectedPhotoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK) {
            // proceed & check what image was selected!

            // uri represents the location where that image is stored
            selectedPhotoUri = data.data
            // receive the bitmap form getCapturedImage()
            val bitmap = getCapturedImage(selectedPhotoUri!!)
            // change it into drawable
            val bitmapDrawable = BitmapDrawable(bitmap)
            // set it to imageView
            imageView_reg.setImageDrawable(bitmapDrawable)

        }
    }


    fun performRegister() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener

            // else if successfull
            Log.d("Main", "Successfully created user")

            uploadImageToFirebaseStorage()
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        // creating a img filename which should be unique or random
        // here filename is consider as a unique id of img
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Successfully image is uploaded: ${it.metadata?.path}")
                Toast.makeText(this, "Image is successfully uploaded", Toast.LENGTH_LONG).show()
                //if download URL needed
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "File location :$it")
                    // Save img to some database
                    // make http
                    var url = "http://10.0.2.2/Twitter%20Android%20Clone/TwitterAndroidServer/register.php?first_name=" + username_reg.text + "&email=" + email_reg.text + "&password=" + password_reg.text + "&picture_path=" +it
//                    var url = "http://10.0.2.2/Twitter%20Android%20Clone/TwitterAndroidServer/register.php?first_name=Faheem&email=h@yaho.com&password=123&picture_path=home"
                    //        for background process
                    httpCall(url)
                }
            }
            .addOnFailureListener {
                Log.d("Register", "Cant upload the image")
            }
    }

    private fun httpCall(url:String) {
        println("attempt to call http")

        val request=Request.Builder().url(url).build();

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("fail to call url: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println(body)

            }

        })

    }

    // Call HTTP


}
