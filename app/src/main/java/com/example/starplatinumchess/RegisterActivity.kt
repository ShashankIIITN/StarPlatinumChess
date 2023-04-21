package com.example.starplatinumchess

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var etEmail: EditText
    private lateinit var etUsrName: EditText
    private lateinit var etPass: EditText
    private lateinit var etCnfPass: EditText
    private lateinit var aldLoginbtn: TextView
    private lateinit var signUpBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var ProgressBar: ProgressBar


    fun validate(): Boolean
    {
        val username= findViewById<EditText>(R.id.editTextUsrName)
        val Email =findViewById<EditText>(R.id.editTextEmail)
        val passw =findViewById<EditText>(R.id.editTextPass)
        val conpass =findViewById<EditText>(R.id.editTextCnfPass)
        val pHone =findViewById<EditText>(R.id.editTextPhone)

        val uname = username.text.toString()
        val email= Email.text.toString()
        val pass = passw.text.toString()
        val cpass = conpass.text.toString()
        val phone = pHone.text.toString()

        var user=false
        var mail=false
        var pas =false
        var conpas =false
        var pho= false


        //gender.error=if (gender.checkedRadioButtonId == -1)"Please select your gender" else null


//        if(uname.isEmpty())
//        {
//            umane.getError("Username can not be empty")
//        }
//        username.validate("Username can not be empty") {uname->uname.length>0}


//        else if(uname.length<4)
//        {
//            uname.setError("Username has to be of at least 4 characters")
//        }

        username.error=if (uname.isEmpty())"Username cannot be empty" else if (uname.length<4)"Username has to be of at lest 4 characters" else {user = true; null}

        //username.error=if (uname.length<4)"Username has to be of at lest 4 characters" else null

//        if(email.isEmpty())
//        {
//            email.setError("Email can not be empty")
//        }

        ////////////else if()

        Email.error=if (email.isEmpty())"Email cannot be empty" else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())"Please enter a valid email address" else {mail=true;null}


        //@ ka checkingggg!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!



//
//        if(pass.isEmpty())
//        {
//            pass.setError("Password cannot be empty")
//        }
//
//        else if(pass.length<8)
//        {
//            pass.setError("Password must be of at least 8 characters")
//        }

//        else
//        {
//            val cap=false
//            val num=false
//            val chat=false
//            for(i in 0 .. pass.length)
//            {
//                if(pass[i])
//            }
//        }

        var cap=false
        var num=false
        var schar=false
        var small=false

        for(i in pass.indices)
        {
            if(pass[i] in 'A'..'Z')
                cap=true

            else if(pass[i] in '0'..'9')
                num=true

            else if(pass[i] in '!'..'/' || pass[i] in ':'..'@' || pass[i] in '['..'`' || pass[i] in '{'..'~')
                schar=true

            else
                small=true

            if(cap && num && schar && small)
                break
        }

        passw.error=if (pass.isEmpty())"Password cannot be empty" else if (pass.length<8)"Password must be of at least 8 characters"
        else if(!cap || !num || !schar || !small)"Password must contain a capital letter,one small letter, one number and one special character" else {pas=true ; null}

        //passw.error=if (pass.length<8)"Password must be of at least 8 characters" else null

        //char up,down,num,sp


        //passw.error= if(cap && num && schar && small) null else "Password must contain a capital letter,one small letter, one number and one special character"


//        if(cpass!=pass)
//        {
//            cpass.setError("Passwords do not match")
//        }

        conpass.error=if (cpass!=pass)"Passwords do not match" else {conpas=true ; null}

        pHone.error=if(phone.length!=10)"Phone number should be of 10 digits" else {pho=true ; null}

        return (user && pas && conpas && pho && mail)


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
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
            intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        signUpBtn.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val CnfPass = etCnfPass.text.toString()
            val usrName = etUsrName.text.toString()
            //Example of functions that can be used for validation no need for lengthy way

            var valiDate : Boolean = validate()
            if(valiDate) SignUp(email, pass, usrName)

        }


    }

    public override fun onStart() {
        super.onStart()
        ProgressBar.visibility = View.INVISIBLE
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@RegisterActivity, "Already Logged in $currentUser", Toast.LENGTH_SHORT)
                .show()
            intent = Intent(this, MainMenu::class.java)
            intent.putExtra("User", "Myname");
            intent.putExtra("Pts", 150);
            startActivity(intent)
        }
    }


    private fun SignUp(Email: String, Pass: String, UsrName: String) {
        //auth.setTenantId(UsrName);
        ProgressBar.visibility = View.VISIBLE

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
                        this@RegisterActivity, "Email Already in Use..", Toast.LENGTH_SHORT
                    ).show()
                } else if (task.exception is FirebaseNetworkException) {

                    Log.w(
                        ContentValues.TAG,
                        "Connection Error: Please Check your Internet connection and try Again ",
                        task.exception
                    )
                    Toast.makeText(
                        this@RegisterActivity,
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
                        this@RegisterActivity,
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
                        this@RegisterActivity,
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

            Toast.makeText(this@RegisterActivity, "Successfully Registered !!", Toast.LENGTH_SHORT).show()

        }

    }
}
