package com.example.onepass

import android.view.ViewOutlineProvider
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onepass.GlobalScaleManager

class HomeContactAdapter(private val contacts: List<Contact>, private val listener: OnContactClickListener) : RecyclerView.Adapter<HomeContactAdapter.ContactViewHolder>() {

    interface OnContactClickListener {
        fun onContactClick(contact: Contact)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: ImageView = itemView.findViewById(R.id.contactImage)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        
        init {
            contactImage.clipToOutline = true
            contactImage.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, 16f)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact_home, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val context = holder.itemView.context
        
        holder.contactName.text = contact.name
        
        // 根据缩放比例调整头像大小
        val originalImageSize = 400
        val scaledImageSize = GlobalScaleManager.getScaledValue(context, originalImageSize)
        val layoutParams = holder.contactImage.layoutParams
        layoutParams.width = scaledImageSize
        layoutParams.height = scaledImageSize
        holder.contactImage.layoutParams = layoutParams
        
        // 根据缩放比例调整字体大小
        val originalTextSize = 26f
        val scaledTextSize = GlobalScaleManager.getScaledValue(context, originalTextSize)
        holder.contactName.textSize = scaledTextSize
        
        if (!contact.imagePath.isNullOrEmpty()) {
            android.util.Log.d("HomeContactAdapter", "加载图片: ${contact.imagePath}")

            // 检查文件是否存在
            val imageFile = java.io.File(contact.imagePath)
            if (imageFile.exists()) {
                android.util.Log.d("HomeContactAdapter", "文件存在，大小: ${imageFile.length()} bytes")

                try {
                    val bitmap = decodeSampledBitmapFromFile(contact.imagePath, scaledImageSize, scaledImageSize)
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

    /**
     * 计算合适的采样率
     */
    private fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // 图片原始宽高
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // 计算最大的采样率，使宽高都不超过目标值
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 使用采样率解码图片，减少内存占用
     */
    private fun decodeSampledBitmapFromFile(
        path: String,
        reqWidth: Int,
        reqHeight: Int
    ): android.graphics.Bitmap? {
        // 首先只解码图片的尺寸信息
        val options = android.graphics.BitmapFactory.Options()
        options.inJustDecodeBounds = true
        android.graphics.BitmapFactory.decodeFile(path, options)

        // 计算采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // 使用采样率解码图片
        options.inJustDecodeBounds = false
        return android.graphics.BitmapFactory.decodeFile(path, options)
    }
}