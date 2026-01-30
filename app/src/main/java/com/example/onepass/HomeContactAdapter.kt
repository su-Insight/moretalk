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
        val contactWechatNote: TextView = itemView.findViewById(R.id.contactWechatNote)
        val contactPhoneNumber: TextView = itemView.findViewById(R.id.contactPhoneNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_home, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        
        holder.contactWechatNote.text = contact.wechatNote
        
        if (contact.phoneNumber.isNotEmpty()) {
            holder.contactPhoneNumber.text = contact.phoneNumber
            holder.contactPhoneNumber.visibility = View.VISIBLE
        } else {
            holder.contactPhoneNumber.visibility = View.GONE
        }
        
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
        
        holder.itemView.setOnClickListener {
            listener.onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size
}