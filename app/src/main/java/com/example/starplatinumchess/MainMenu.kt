package com.example.starplatinumchess

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
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
    private var Points: Int = 0;
    private lateinit var auth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference
    private lateinit var txtVwpts: TextView
    private lateinit var txtVwusr: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        database = Firebase.database.reference
        auth = Firebase.auth
        user = auth.currentUser!!
        userID = user.uid
        usrEmail = user.email


        txtVwusr = findViewById(R.id.text_User)
        txtVwpts = findViewById(R.id.text_Points)
        //auth = Firebase.auth

        //val intent = intent
//
        // usrName = intent.getStringExtra("User")
        //Points = intent.getIntExtra("Pts", 0)
        //txtVwusr.setText(usrName)
//
        //txtVwpts.setText(Points.toString())


        Log.d(TAG, "$Points")
    }

    override fun onStart() {
        super.onStart()
        retrieveData();
    }

    private fun retrieveData() {

        database.child("Users").child(userID).get().addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            val data:UserData? = it.result.getValue(UserData::class.java)
            //var index = data.indexOf(',')

            usrName = data?.userName
            Points = data?.points.toString().toInt()

            txtVwusr.text = usrName.toString()
            txtVwpts.text = Points.toString()
        }
    }

}



