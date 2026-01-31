package com.example.onepass

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(private val contacts: List<Contact>, private val listener: OnContactClickListener) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    interface OnContactClickListener {
        fun onContactClick(contact: Contact)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: ImageView = itemView.findViewById(R.id.contactImage)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactInfo: TextView = itemView.findViewById(R.id.contactInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        
        // 设置联系人图片
        if (!contact.imagePath.isNullOrEmpty()) {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeFile(contact.imagePath)
                if (bitmap != null) {
                    holder.contactImage.setImageBitmap(bitmap)
                } else {
                    holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } catch (e: Exception) {
                holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        } else {
            holder.contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
        
        // 设置联系人名称
        holder.contactName.text = contact.name
        
        // 设置联系人信息
        val infoBuilder = StringBuilder()
        if (contact.wechatNote.isNotEmpty()) {
            infoBuilder.append("微信备注: ${contact.wechatNote}")
        }
        if (contact.phoneNumber.isNotEmpty()) {
            if (infoBuilder.isNotEmpty()) infoBuilder.append(" | ")
            infoBuilder.append("手机号: ${contact.phoneNumber}")
        }
        
        val features = mutableListOf<String>()
        if (contact.hasWechatVideo) features.add("微信视频")
        if (contact.hasWechatVoice) features.add("微信语音")
        if (contact.hasPhoneCall) features.add("拨打电话")
        
        if (features.isNotEmpty()) {
            if (infoBuilder.isNotEmpty()) infoBuilder.append(" | ")
            infoBuilder.append(features.joinToString(", "))
        }
        
        holder.contactInfo.text = infoBuilder.toString()
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            listener.onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size
}