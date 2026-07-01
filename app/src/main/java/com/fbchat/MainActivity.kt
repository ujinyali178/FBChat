package com.fbchat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
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
        val token = AccessToken.getCurrentAccessToken() ?: run { goLogin(); return }
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu); return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            LoginManager.getInstance().logOut(); goLogin(); return true
        }
        return super.onOptionsItemSelected(item)
    }

        adapter = FriendAdapter(friends) { f ->
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra("friend_id", f.id); putExtra("friend_name", f.name)
            })
        }
        b.friendsRecycler.layoutManager = LinearLayoutManager(this)
        b.friendsRecycler.adapter = adapter
        loadFriends(token.token)
    }

    private fun loadFriends(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val r = Api.service.getFriends(token = token)
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

    private fun goLogin() { startActivity(Intent(this, LoginActivity::class.java)); finish() }
}
