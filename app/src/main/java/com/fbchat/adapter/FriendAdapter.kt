package com.fbchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fbchat.R
import com.fbchat.api.FBFriend

class FriendAdapter(
    private val friends: MutableList<FBFriend>,
    private val onClick: (FBFriend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.VH>() {
    class VH(item: android.view.View) : RecyclerView.ViewHolder(item) {
        val name: TextView = item.findViewById(R.id.friend_name)
        val photo: ImageView = item.findViewById(R.id.friend_photo)
    }
    override fun onCreateViewHolder(p: ViewGroup, i: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_friend, p, false))
    override fun getItemCount() = friends.size
    override fun onBindViewHolder(h: VH, p: Int) {
        val f = friends[p]; h.name.text = f.name
        f.picture?.data?.url?.let {
            Glide.with(h.photo.context).load(it).circleCrop().into(h.photo)
        }
        h.itemView.setOnClickListener { onClick(f) }
    }
}
