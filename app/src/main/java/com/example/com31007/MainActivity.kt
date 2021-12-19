package com.example.com31007

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent


import android.view.View
import android.widget.Button


class MainActivity : AppCompatActivity() {

    private var mButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mButton = findViewById<View>(R.id.map_button) as Button
        mButton!!.setOnClickListener() {
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        }
    }
}
