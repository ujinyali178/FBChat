package com.fbchat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.fbchat.adapter.FriendAdapter
import com.fbchat.api.Api
import com.fbchat.api.FBFriend
import com.fbchat.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val friends = mutableListOf<FBFriend>()
    private lateinit var adapter: FriendAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater); setContentView(b.root)
        val t = AccessToken.getCurrentAccessToken() ?: run { go(); return }
        setSupportActionBar(b.toolbar)
        adapter = FriendAdapter(friends) { f ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("friend_id", f.id); putExtra("friend_name", f.name)
            })
        }
        b.friendsRecycler.layoutManager = LinearLayoutManager(this)
        b.friendsRecycler.adapter = adapter
        load(t.token)
        b.toolbar.setOnMenuItemClickListener {
            if (it.itemId == 0) { LoginManager.getInstance().logOut(); go() }; false
        }
    }
    private fun load(t: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val r = Api.service.getFriends(token = t)
                if (r.isSuccessful) {
                    val list = r.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        friends.clear(); friends.addAll(list); adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Gagal load", 0).show() }
            }
        }
    }
    private fun go() { startActivity(Intent(this, LoginActivity::class.java)); finish() }
}
