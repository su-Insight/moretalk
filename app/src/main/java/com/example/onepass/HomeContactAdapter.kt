package com.example.onepass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeContactAdapter(private val contacts: List<Contact>, private val listener: OnContactClickListener) : RecyclerView.Adapter<HomeContactAdapter.ContactViewHolder>() {

    interface OnContactClickListener {
        fun onContactClick(contact: Contact)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: ImageView = itemView.findViewById(R.id.contactImage)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_home, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        
        holder.contactName.text = contact.name
        
        if (!contact.imagePath.isNullOrEmpty()) {
            android.util.Log.d("HomeContactAdapter", "加载图片: ${contact.imagePath}")
            
            // 检查文件是否存在
            val imageFile = java.io.File(contact.imagePath)
            if (imageFile.exists()) {
                android.util.Log.d("HomeContactAdapter", "文件存在，大小: ${imageFile.length()} bytes")
                
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(contact.imagePath)
                    if (bitmap != null) {
                        holder.contactImage.setImageBitmap(bitmap)
                        android.util.Log.d("HomeContactAdapter", "图片加载成功")
                    } else {
                        android.util.Log.e("HomeContactAdapter", "图片解码失败，bitmap为null")
                        holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeContactAdapter", "图片加载异常: ${e.message}", e)
                    holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } else {
                android.util.Log.e("HomeContactAdapter", "文件不存在: ${contact.imagePath}")
                holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        } else {
            android.util.Log.d("HomeContactAdapter", "图片路径为空")
            holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
        
        holder.itemView.setOnClickListener {
            listener.onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size
}