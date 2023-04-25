package com.example.starplatinumchess

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var Usrname: String;
    private var Points: Int = 0;
    private lateinit var Email: String;
    private lateinit var Pass: String;
    private lateinit var auth: FirebaseAuth
    private lateinit var ProgressBar: ProgressBar
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var lginBtn: Button

    fun validate(): Boolean
    {
        val username= findViewById<EditText>(R.id.editTextUsrName)
        val passw =findViewById<EditText>(R.id.editTextPass)

        val uname = username.text.toString()
        val pass = passw.text.toString()

        var user=false
        var pas =false

        username.error= if (uname.isEmpty())"Username cannot be empty"
        else {user = true; null}

        passw.error=if (pass.isEmpty())"Password cannot be empty" else{pas=true ; null}

        return (user && pas)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activity)
        auth = Firebase.auth

        etEmail = findViewById<EditText>(R.id.etext_email)
        etPass = findViewById<EditText>(R.id.etext_pass)

        lginBtn = findViewById<Button>(R.id.lginbtn)
        ProgressBar = findViewById(R.id.progressBar2)

        lginBtn.setOnClickListener {
            Email = etEmail.text.toString()
            Pass = etPass.text.toString()
            etEmail.focusable = View.NOT_FOCUSABLE
            etPass.focusable = View.NOT_FOCUSABLE
            lginBtn.isEnabled = false
            if (TextUtils.isEmpty(Email)) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                Log.e("Email", Email)
            } else if (TextUtils.isEmpty(Pass)) {
                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
            } else {
                Login()
            }
        }
        findViewById<TextView>(R.id.Reg_New).setOnClickListener{
            var intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        ProgressBar.visibility = View.INVISIBLE
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@LoginActivity, "Already Logged in $currentUser", Toast.LENGTH_SHORT)
                .show()
            intent = Intent(this, MainMenu::class.java)
            intent.putExtra("User", "Myname");
            intent.putExtra("Pts", 150);
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Login() {
        ProgressBar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(Email, Pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    ProgressBar.visibility = View.INVISIBLE
                    etEmail.focusable = View.FOCUSABLE
                    etPass.focusable = View.FOCUSABLE
                    lginBtn.isEnabled = true
                    val intent = Intent(this, MainMenu::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    Toast.makeText(this, "Successfully Logged in !!", Toast.LENGTH_SHORT).show()

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed : Please Enter valid Credentials!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (task.exception is FirebaseAuthEmailException) {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed : Please Enter valid Email!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed : User Doesn't Exists Please Sign Up or Try Again!! ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                ProgressBar.visibility = View.INVISIBLE
                etEmail.focusable = View.FOCUSABLE
                etPass.focusable = View.FOCUSABLE
                lginBtn.isEnabled = true
            }
    }
}