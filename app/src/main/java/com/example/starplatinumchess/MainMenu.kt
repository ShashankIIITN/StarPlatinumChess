package com.example.starplatinumchess

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainMenu : AppCompatActivity() {

    private var usrName: String? = null
    private var usrEmail: String? = null
    private var points: Int = 0;
    private lateinit var auth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private lateinit var txtVwpts: TextView
    private lateinit var txtVwusr: TextView
    private lateinit var Progressbar :ProgressBar;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        Progressbar = findViewById(R.id.FetchPBar)

        Progressbar.visibility = View.INVISIBLE

        database = Firebase.database.reference
        auth = Firebase.auth
        user = auth.currentUser!!
        userID = user.uid
        usrEmail = user.email


        txtVwusr = findViewById(R.id.text_User)
        txtVwpts = findViewById(R.id.text_Points)

        findViewById<Button>(R.id.with_frnds_btn).setOnClickListener{
            val  intent = Intent(this@MainMenu, MultMainActivity::class.java)
            intent.putExtra("UserName", usrName)
            intent.putExtra("UserPoints", points)

            Toast.makeText(this , "usrname $usrName points  $points", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }


        //auth = Firebase.auth

        //val intent = intent
//
        // usrName = intent.getStringExtra("User")
        //Points = intent.getIntExtra("Pts", 0)
        //txtVwusr.setText(usrName)
//
        //txtVwpts.setText(Points.toString())

    }

    override fun onStart() {
        super.onStart()
        retrieveData();

    }

    private fun retrieveData() {
        Progressbar.visibility = View.VISIBLE


        database.child("Users").child(userID).get().addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            val data:UserData? = it.result.getValue(UserData::class.java)
            //var index = data.indexOf(',')

            usrName = data?.userName
            points = data?.points.toString().toInt()

            txtVwusr.text = usrName.toString()
            txtVwpts.text = points.toString()
            Progressbar.visibility = View.INVISIBLE
        }
    }

}



