package com.example.starplatinumchess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {
    private lateinit var btn9: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var userName = intent.getStringExtra("userName")
        var points = intent.getIntExtra("Points", 0)
        var totalMatches = intent.getIntExtra("totalMatches", 0)
        var MatchesWon = intent.getIntExtra("MatchesWon", 0)

        findViewById<TextView>(R.id.txt_V9).text = userName
        findViewById<TextView>(R.id.txt_V10).text = points.toString()
        findViewById<TextView>(R.id.txt_V12).text = totalMatches.toString()
        findViewById<TextView>(R.id.textView6).text = MatchesWon.toString()
        btn9 = findViewById(R.id.button4)
        btn9.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }

    }
}