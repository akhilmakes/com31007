package com.example.week_5B_solution.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.week_5B_solution.R

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