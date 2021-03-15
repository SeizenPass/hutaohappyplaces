package com.example.hutaohappyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.hutaohappyplaces.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        mAuth = FirebaseAuth.getInstance()
        btn_create.setOnClickListener {
            var email = et_email.text.toString().trim()
            var password = et_password.text.toString().trim()
            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if (it.isSuccessful) {
                        var user: FirebaseUser = mAuth!!.currentUser!!
                        Toast.makeText(this, "Account was created.", Toast.LENGTH_LONG)
                            .show()
                        val data = Intent()
                        data.putExtra("email", email)
                        data.putExtra("password", password)
                        setResult(RESULT_OK, data)
                        finish()
                    } else {
                        Log.d("Error: ", it.toString())
                    }

                }
        }
    }
}
