package com.fbchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fbchat.R
import com.fbchat.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.VH>() {
    class VH(item: android.view.View) : RecyclerView.ViewHolder(item) {
        val container: FrameLayout = item.findViewById(R.id.message_container)
        val sender: TextView = item.findViewById(R.id.sender_name)
        val text: TextView = item.findViewById(R.id.message_text)
        val time: TextView = item.findViewById(R.id.message_time)
    }
    override fun onCreateViewHolder(p: ViewGroup, i: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_message, p, false))
    override fun getItemCount() = messages.size
    override fun onBindViewHolder(h: VH, p: Int) {
        val m = messages[p]
        h.text.text = m.text; h.sender.text = m.senderName
        h.time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(m.timestamp))
        val lp = h.container.layoutParams as FrameLayout.LayoutParams
        if (m.isSent) {
            lp.gravity = android.view.Gravity.END
            h.container.setBackgroundResource(R.drawable.bg_sent)
            h.sender.visibility = android.view.View.GONE
        } else {
            lp.gravity = android.view.Gravity.START
            h.container.setBackgroundResource(R.drawable.bg_received)
            h.sender.visibility = android.view.View.VISIBLE
        }
        h.container.layoutParams = lp
    }
    fun add(m: ChatMessage) { messages.add(m); notifyItemInserted(messages.size - 1) }
}
