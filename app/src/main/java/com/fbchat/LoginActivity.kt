package com.fbchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.fbchat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding
    private lateinit var cm: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater); setContentView(b.root)
        if (AccessToken.getCurrentAccessToken() != null) { go(); return }
        cm = CallbackManager.Factory.create()
        b.facebookLoginButton.setPermissions(listOf("public_profile", "email", "user_friends"))
        b.facebookLoginButton.registerCallback(cm, object : FacebookCallback<LoginResult> {
            override fun onSuccess(r: LoginResult) { go() }
            override fun onCancel() { Toast.makeText(this@LoginActivity, "Batal", 0).show() }
            override fun onError(e: FacebookException) { Toast.makeText(this@LoginActivity, "Error: ${e.message}", 1).show() }
        })
    }
    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data); cm.onActivityResult(req, res, data)
    }
    private fun go() { startActivity(Intent(this, MainActivity::class.java)); finish() }
}
