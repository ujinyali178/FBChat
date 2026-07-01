package com.fbchat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.AccessToken
import com.fbchat.adapter.MessageAdapter
import com.fbchat.api.Api
import com.fbchat.databinding.ActivityChatBinding
import com.fbchat.model.ChatMessage
import kotlinx.coroutines.*

private const val PAGE_ID = "YOUR_PAGE_ID"
private const val PAGE_TOKEN = "YOUR_PAGE_ACCESS_TOKEN"

class ChatActivity : AppCompatActivity() {
    private lateinit var b: ActivityChatBinding
    private val msgs = mutableListOf<ChatMessage>()
    private lateinit var adapter: MessageAdapter
    private var friendId = ""; private var friendName = ""
    private var postId: String? = null
    private var userId = ""; private var userName = ""
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var polling: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityChatBinding.inflate(layoutInflater); setContentView(b.root)
        friendId = intent.getStringExtra("friend_id") ?: ""
        friendName = intent.getStringExtra("friend_name") ?: "?"
        b.toolbar.title = friendName
        b.toolbar.setNavigationOnClickListener { finish() }
        adapter = MessageAdapter(msgs, userId)
        b.messagesRecycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        b.messagesRecycler.adapter = adapter
        b.sendButton.setOnClickListener { send() }
        init()
    }

    private fun init() {
        val t = AccessToken.getCurrentAccessToken() ?: return
        userId = t.userId
        scope.launch {
            try {
                val r = Api.service.getProfile(token = t.token)
                if (r.isSuccessful) {
                    userName = r.body()?.name ?: "Me"
                    adapter = MessageAdapter(msgs, userId)
                    b.messagesRecycler.adapter = adapter
                    withContext(Dispatchers.Main) { findRoom() }
                }
            } catch (_: Exception) {}
        }
    }

    private fun findRoom() {
        val t = AccessToken.getCurrentAccessToken() ?: return
        val key = "room_${minOf(userId, friendId)}_${maxOf(userId, friendId)}"
        val prefs = getSharedPreferences("rooms", MODE_PRIVATE)
        postId = prefs.getString(key, null)
        if (postId == null) {
            scope.launch {
                try {
                    val r = Api.service.createPost(PAGE_ID, "chat_$key", PAGE_TOKEN)
                    if (r.isSuccessful) {
                        postId = r.body()?.id
                        prefs.edit().putString(key, postId).apply()
                        poll()
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ChatActivity, "Gagal buat room", 0).show()
                    }
                }
            }
        } else poll()
    }

    private fun poll() {
        polling?.cancel()
        polling = scope.launch {
            while (isActive) {
                val pid = postId ?: break
                try {
                    val r = Api.service.getMsgs(pid, token = PAGE_TOKEN)
                    if (r.isSuccessful) {
                        val list = r.body()?.data ?: emptyList()
                        withContext(Dispatchers.Main) {
                            msgs.clear()
                            list.forEach { c ->
                                val sent = c.from?.id == userId
                                msgs.add(ChatMessage(c.id, c.from?.id ?: "",
                                    c.from?.name ?: "?", c.message ?: "",
                                    parse(c.created_time), sent))
                            }
                            adapter.notifyDataSetChanged()
                            if (msgs.isNotEmpty())
                                b.messagesRecycler.smoothScrollToPosition(msgs.size - 1)
                        }
                    }
                } catch (_: Exception) {}
                delay(3000)
            }
        }
    }

    private fun send() {
        val text = b.messageInput.text?.toString()?.trim() ?: return
        if (text.isEmpty() || postId == null) return
        b.messageInput.text?.clear()
        scope.launch {
            try {
                Api.service.sendMsg(postId!!, text, PAGE_TOKEN)
                adapter.add(ChatMessage("s", userId, userName, text,
                    System.currentTimeMillis(), true))
                b.messagesRecycler.smoothScrollToPosition(msgs.size - 1)
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Gagal kirim", 0).show()
                }
            }
        }
    }

    private fun parse(t: String?): Long = try {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault()).parse(t ?: "")?.time
            ?: System.currentTimeMillis()
    } catch (_: Exception) { System.currentTimeMillis() }

    override fun onDestroy() {
        super.onDestroy(); polling?.cancel(); scope.cancel()
    }
}
