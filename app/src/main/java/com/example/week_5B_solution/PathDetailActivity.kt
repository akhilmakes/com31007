package com.example.week_5B_solution

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class PathDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_detail)
        var markerTest = findViewById<TextView>(R.id.markerTest)
        var title = intent.getStringExtra("title")
        var pathID = intent.getIntExtra("pathID",-1)

        markerTest.setText("$title, $pathID")

    }
}