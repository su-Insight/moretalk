package com.example.onepass.presentation.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onepass.R
import com.example.onepass.domain.model.Contact

class ContactDetailActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ContactDetailActivity"
        const val EXTRA_CONTACT_ID = "contact_id"
    }
    

    private lateinit var contactImage: ImageView
    private lateinit var textContactName: android.widget.TextView
    private lateinit var textPhoneNumber: android.widget.TextView
    private lateinit var textWechatNote: android.widget.TextView
    private lateinit var textFeatures: android.widget.TextView
    private lateinit var btnDeleteContact: android.widget.Button
    
    private var contact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ContactDetailActivity onCreate 开始")
        
        setContentView(R.layout.activity_contact_detail)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
        contactImage = findViewById(R.id.contactImage)
        textContactName = findViewById(R.id.textContactName)
        textPhoneNumber = findViewById(R.id.textPhoneNumber)
        textWechatNote = findViewById(R.id.textWechatNote)
        textFeatures = findViewById(R.id.textFeatures)
        btnDeleteContact = findViewById(R.id.btnDeleteContact)
        
        btnDeleteContact.setOnClickListener {
            Log.d(TAG, "删除联系人按钮被点击")
            deleteContact()
        }
        
        // 加载联系人详情
        loadContactDetail()
        
        Log.d(TAG, "ContactDetailActivity onCreate 完成")
    }
    
    private fun loadContactDetail() {
        Log.d(TAG, "加载联系人详情")
        
        // 获取从Intent传递的联系人ID
        val contactId = intent.getIntExtra(EXTRA_CONTACT_ID, -1)
        if (contactId == -1) {
            Log.e(TAG, "未获取到联系人ID")
            Toast.makeText(this, "未找到联系人信息", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 从SharedPreferences加载所有联系人
        val CONTACTS_PREFS = "contacts_prefs"
        val KEY_CONTACTS = "contacts"
        val prefs = getSharedPreferences(CONTACTS_PREFS, MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        if (contactsJson != null) {
            try {
                val gson = com.google.gson.Gson()
                val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                contact = contactArray.find { it.id == contactId }
                
                if (contact != null) {
                    // 显示联系人详情
                    displayContactDetail(contact!!)
                } else {
                    Log.e(TAG, "未找到ID为 $contactId 的联系人")
                    Toast.makeText(this, "未找到联系人信息", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
                Toast.makeText(this, "加载联系人信息失败", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e(TAG, "没有找到联系人数据")
            Toast.makeText(this, "未找到联系人信息", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun displayContactDetail(contact: Contact) {
        Log.d(TAG, "显示联系人详情: ${contact.wechatNote}")
        
        // 设置联系人图片
        if (contact.imagePath != null) {
            contactImage.setImageResource(android.R.drawable.ic_menu_gallery)
        } else {
            contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
        
        // 设置联系人名称
        val contactName = contact.wechatNote.ifEmpty { contact.name }
        textContactName.text = contactName
        
        // 设置手机号
        textPhoneNumber.text = if (contact.phoneNumber.isNotEmpty()) {
            "手机号: ${contact.phoneNumber}"
        } else {
            "手机号: 无"
        }
        
        // 设置微信备注
        textWechatNote.text = if (contact.wechatNote.isNotEmpty()) {
            "微信备注: ${contact.wechatNote}"
        } else {
            "微信备注: 无"
        }
        
        // 设置功能特性
        val features = mutableListOf<String>()
        if (contact.hasWechatVideo) features.add("微信视频")
        if (contact.hasWechatVoice) features.add("微信语音")
        if (contact.hasPhoneCall) features.add("拨打电话")
        
        textFeatures.text = if (features.isNotEmpty()) {
            "功能: ${features.joinToString(", ")}"
        } else {
            "功能: 无"
        }
    }
    
    private fun deleteContact() {
        Log.d(TAG, "删除联系人")
        
        if (contact == null) {
            Toast.makeText(this, "联系人信息不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建确认对话框
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("删除联系人")
        builder.setMessage("确定要删除 ${contact!!.wechatNote.ifEmpty { contact!!.name }} 吗？")
        
        builder.setPositiveButton("确定") { dialog, which ->
            // 执行删除操作
            val success = removeContactFromStorage(contact!!.id)
            if (success) {
                Toast.makeText(this, "联系人删除成功", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "联系人删除失败", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("取消") { dialog, which ->
            dialog.cancel()
        }
        
        builder.show()
    }
    
    private fun removeContactFromStorage(contactId: Int): Boolean {
        Log.d(TAG, "从存储中删除联系人: $contactId")
        
        val CONTACTS_PREFS = "contacts_prefs"
        val KEY_CONTACTS = "contacts"
        
        // 从SharedPreferences加载现有联系人
        val prefs = getSharedPreferences(CONTACTS_PREFS, MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        val contacts = mutableListOf<Contact>()
        if (contactsJson != null) {
            try {
                val gson = com.google.gson.Gson()
                val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                contacts.addAll(contactArray)
            } catch (e: Exception) {
                Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
                return false
            }
        }
        
        // 移除指定ID的联系人
        val initialSize = contacts.size
        contacts.removeAll { it.id == contactId }
        
        if (contacts.size == initialSize) {
            Log.e(TAG, "未找到要删除的联系人")
            return false
        }
        
        // 保存更新后的联系人列表
        try {
            val gson = com.google.gson.Gson()
            val updatedContactsJson = gson.toJson(contacts)
            prefs.edit().putString(KEY_CONTACTS, updatedContactsJson).apply()
            Log.d(TAG, "联系人删除成功，剩余 ${contacts.size} 个联系人")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "保存联系人数据失败: ${e.message}", e)
            return false
        }
    }
}