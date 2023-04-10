package com.example.starplatinumchess

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var etEmail: EditText
    private lateinit var etUsrName: EditText
    private lateinit var etPass: EditText
    private lateinit var etCnfPass: EditText
    private lateinit var aldLoginbtn: TextView
    private lateinit var signUpBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var ProgressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        etEmail = findViewById(R.id.editTextEmail)
        etPass = findViewById(R.id.editTextPass)
        etCnfPass = findViewById(R.id.editTextCnfPass)
        etUsrName = findViewById(R.id.editTextUsrName)
        aldLoginbtn = findViewById(R.id.Ald_Usr_Txt)
        signUpBtn = findViewById(R.id.button)
        auth = Firebase.auth
        database = Firebase.database.reference
        ProgressBar = findViewById(R.id.progressBar)



        aldLoginbtn.setOnClickListener {
            aldLoginbtn.setTextColor(Color.parseColor("#000075"))
            intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        signUpBtn.setOnClickListener {
            ProgressBar.visibility = View.VISIBLE
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val CnfPass = etCnfPass.text.toString()
            val usrName = etUsrName.text.toString()
            //Example of functions that can be used for validation no need for lengthy way
            if (TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please Enter a Valid Email Address", Toast.LENGTH_SHORT)
                    .show()
            } else if (TextUtils.isEmpty(usrName)) {
                Toast.makeText(this, "Please Enter a Valid User name", Toast.LENGTH_SHORT).show()
            } else {
                SignUp(email, pass, usrName)
            }
        }


    }

    public override fun onStart() {
        super.onStart()
        ProgressBar.visibility = View.INVISIBLE
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@MainActivity, "Already Logged in $currentUser", Toast.LENGTH_SHORT)
                .show()
            intent = Intent(this, MainMenu::class.java)
            intent.putExtra("User", "Myname");
            intent.putExtra("Pts", 150);
            startActivity(intent)
        }
    }


    private fun SignUp(Email: String, Pass: String, UsrName: String) {
        //auth.setTenantId(UsrName);

        auth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = auth.currentUser
                Log.d(ContentValues.TAG, "createUserWithEmail:success + $user")

                val pts = 1000;
                WriteUser(user!!.uid, UsrName, pts)


            } else {
                // If sign in fails, display a message to the user.
                if (task.exception is FirebaseAuthUserCollisionException) {

                    Log.w(ContentValues.TAG, "Email Already in Use ", task.exception)
                    Toast.makeText(
                        this@MainActivity, "Email Already in Use..", Toast.LENGTH_SHORT
                    ).show()
                } else if (task.exception is FirebaseNetworkException) {

                    Log.w(
                        ContentValues.TAG,
                        "Connection Error: Please Check your Internet connection and try Again ",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Connection Error: Please Check your Internet connection and try Again..",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (task.exception is FirebaseAuthWeakPasswordException) {

                    Log.w(
                        ContentValues.TAG,
                        "Connection Error: Please Check your Internet connection and try Again ",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Your Password is Too Weak Please Change it and try again ..",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (task.exception is FirebaseAuthEmailException) {

                    Log.w(
                        ContentValues.TAG,
                        "Connection Error: Please Check your Internet connection and try Again ",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Incorrect Email : Please Use another Email to Signup  ..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.w(ContentValues.TAG, "Email Already in Use ", task.exception)
            }
            ProgressBar.visibility = View.INVISIBLE
        }

    }

    private fun WriteUser(userId: String, userName: String, points: Int) {
        val usr = UserData(userName, points)

        database.child("Users").child(userId).setValue(usr).addOnCompleteListener {

            ProgressBar.visibility = View.INVISIBLE
            val intent = Intent(this, MainMenu::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

            Toast.makeText(this@MainActivity, "Successfully Registered !!", Toast.LENGTH_SHORT).show()

        }

    }
}
