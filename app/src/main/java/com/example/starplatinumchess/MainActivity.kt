package com.example.starplatinumchess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    val btn_click_me = findViewById(R.id.button) as Button

        btn_click_me.setOnClickListener{


        }


}
}