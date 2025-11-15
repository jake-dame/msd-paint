package com.example.msdpaint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {

        var SHOW_SPLASH = true
        lateinit var dir: File
        var GLOBAL_USER = Firebase.auth.currentUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        dir = getExternalFilesDir(null)!!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
