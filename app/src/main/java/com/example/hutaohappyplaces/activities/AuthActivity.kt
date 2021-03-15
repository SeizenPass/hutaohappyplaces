package com.example.hutaohappyplaces.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hutaohappyplaces.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        mAuth = FirebaseAuth.getInstance()
        btn_register.setOnClickListener {
            val regIntent = Intent(this@AuthActivity, RegistrationActivity::class.java)
            startActivityForResult(regIntent, REGISTER)
        }
        btn_login.setOnClickListener {
            if (et_email.text.toString().trim() != "" && et_password.text.toString().trim() != "") {
                login(et_email.text.toString().trim(), et_password.text.toString().trim())
            }
        }
    }

    private fun login(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                    task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val pf = getSharedPreferences("auth",Context.MODE_PRIVATE)
                    val ed = pf.edit()
                    ed.putString("email", email)
                    ed.putString("password", password)
                    ed.apply()
                    Toast.makeText(this, "Signed in.", Toast.LENGTH_LONG).show()
                    val regUser = Intent()
                    regUser.putExtra("user", mAuth!!.currentUser)
                    setResult(RESULT_OK, regUser)
                    finish()
                } else {
                    Toast.makeText(this, "Couldn't sign in.", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REGISTER) {
                login(data!!.getStringExtra("email").toString(),
                    data.getStringExtra("password").toString())
            }
        }
    }

    companion object {
        const val REGISTER = 0
    }
}
