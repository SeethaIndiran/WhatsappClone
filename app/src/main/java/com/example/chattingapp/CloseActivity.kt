package com.example.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class CloseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        finishAffinity()
    }
}