package com.example.onepass

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ContactsActivity : AppCompatActivity(), ContactAdapter.OnContactClickListener {
    companion object {
        private const val TAG = "ContactsActivity"
        const val CONTACTS_PREFS = "contacts_prefs"
        const val KEY_CONTACTS = "contacts"
        
        @JvmStatic
        fun saveContact(context: android.content.Context, contact: Contact) {
            Log.d(TAG, "保存联系人: ${contact.wechatNote}")
            
            // 从SharedPreferences加载现有联系人
            val prefs = context.getSharedPreferences(CONTACTS_PREFS, android.content.Context.MODE_PRIVATE)
            val contactsJson = prefs.getString(KEY_CONTACTS, null)
            
            val contacts = mutableListOf<Contact>()
            if (contactsJson != null) {
                try {
                    val gson = com.google.gson.Gson()
                    val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                    contacts.addAll(contactArray)
                } catch (e: Exception) {
                    Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
                }
            }
            
            // 检查是否已存在相同ID的联系人
            val existingIndex = contacts.indexOfFirst { it.id == contact.id }
            if (existingIndex >= 0) {
                // 更新现有联系人
                contacts[existingIndex] = contact
            } else {
                // 添加新联系人
                contacts.add(contact)
            }
            
            // 保存更新后的联系人列表
            val gson = com.google.gson.Gson()
            val updatedContactsJson = gson.toJson(contacts)
            prefs.edit().putString(KEY_CONTACTS, updatedContactsJson).apply()
            
            Log.d(TAG, "联系人保存成功，共 ${contacts.size} 个联系人")
        }
        
        // 生成唯一ID的方法
        @JvmStatic
        fun generateContactId(): Int {
            return System.currentTimeMillis().toInt()
        }
    }
    
    private lateinit var backButton: ImageView
    private lateinit var btnSearchContact: android.widget.Button
    private lateinit var btnAddContact: android.widget.Button
    private lateinit var btnImportContacts: android.widget.Button
    private lateinit var btnExportContacts: android.widget.Button
    private lateinit var recyclerViewContacts: androidx.recyclerview.widget.RecyclerView
    private lateinit var textNoContacts: android.widget.TextView
    
    private var contacts: MutableList<Contact> = mutableListOf()
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ContactsActivity onCreate 开始")
        
        setContentView(R.layout.activity_contacts)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
        backButton = findViewById(R.id.backButton)
        btnSearchContact = findViewById(R.id.btnSearchContact)
        btnAddContact = findViewById(R.id.btnAddContact)
        btnImportContacts = findViewById(R.id.btnImportContacts)
        btnExportContacts = findViewById(R.id.btnExportContacts)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)
        textNoContacts = findViewById(R.id.textNoContacts)
        
        // 初始化RecyclerView
        recyclerViewContacts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        contactAdapter = ContactAdapter(contacts, this)
        recyclerViewContacts.adapter = contactAdapter
        
        backButton.setOnClickListener {
            Log.d(TAG, "返回按钮被点击")
            finish()
        }
        
        btnSearchContact.setOnClickListener {
            Log.d(TAG, "搜索联系人按钮被点击")
            // 打开搜索对话框
            showSearchDialog()
        }
        
        btnAddContact.setOnClickListener {
            Log.d(TAG, "添加联系人按钮被点击")
            // 启动添加联系人页面
            val intent = android.content.Intent(this, AddContactActivity::class.java)
            startActivity(intent)
        }
        
        btnImportContacts.setOnClickListener {
            Log.d(TAG, "导入联系人按钮被点击")
            // 导入联系人功能开发中...
            android.widget.Toast.makeText(this, "导入联系人功能开发中...", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        btnExportContacts.setOnClickListener {
            Log.d(TAG, "导出联系人按钮被点击")
            // 导出联系人功能开发中...
            android.widget.Toast.makeText(this, "导出联系人功能开发中...", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        Log.d(TAG, "ContactsActivity onCreate 完成")
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到这个页面时加载联系人列表
        loadContacts()
    }
    
    private fun loadContacts() {
        Log.d(TAG, "加载联系人列表")
        
        // 从SharedPreferences加载联系人数据
        val prefs = getSharedPreferences(CONTACTS_PREFS, android.content.Context.MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        if (contactsJson != null) {
            try {
                // 使用Gson解析JSON数据
                val gson = com.google.gson.Gson()
                val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                contacts.clear()
                contacts.addAll(contactArray)
                Log.d(TAG, "成功加载 ${contacts.size} 个联系人")
            } catch (e: Exception) {
                Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
                contacts.clear()
            }
        } else {
            Log.d(TAG, "没有找到联系人数据")
            contacts.clear()
        }
        
        // 更新UI
        updateContactsUI()
    }
    
    private fun updateContactsUI() {
        if (contacts.isEmpty()) {
            // 显示暂无联系人提示
            recyclerViewContacts.visibility = android.view.View.GONE
            textNoContacts.visibility = android.view.View.VISIBLE
        } else {
            // 显示联系人列表
            recyclerViewContacts.visibility = android.view.View.VISIBLE
            textNoContacts.visibility = android.view.View.GONE
            contactAdapter.notifyDataSetChanged()
        }
    }
    
    override fun onContactClick(contact: Contact) {
        Log.d(TAG, "联系人被点击: ${contact.wechatNote}")
        // 启动添加联系人页面并传递联系人信息
        val intent = android.content.Intent(this, AddContactActivity::class.java)
        intent.putExtra("contact_id", contact.id)
        intent.putExtra("contact_name", contact.name)
        intent.putExtra("contact_phone", contact.phoneNumber)
        intent.putExtra("contact_wechat", contact.wechatNote)
        intent.putExtra("contact_image", contact.imagePath)
        intent.putExtra("contact_video", contact.hasWechatVideo)
        intent.putExtra("contact_voice", contact.hasWechatVoice)
        intent.putExtra("contact_call", contact.hasPhoneCall)
        startActivity(intent)
    }
    
    private fun showSearchDialog() {
        Log.d(TAG, "显示搜索联系人对话框")
        
        // 创建搜索对话框
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("搜索联系人")
        
        // 创建搜索输入框
        val input = android.widget.EditText(this)
        input.hint = "请输入姓名、微信备注或手机号"
        builder.setView(input)
        
        // 设置搜索按钮
        builder.setPositiveButton("搜索") { dialog, which ->
            val searchText = input.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchContacts(searchText)
            } else {
                android.widget.Toast.makeText(this, "请输入搜索关键词", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // 设置取消按钮
        builder.setNegativeButton("取消") { dialog, which ->
            dialog.cancel()
        }
        
        // 显示对话框
        builder.show()
    }
    
    private fun searchContacts(keyword: String) {
        Log.d(TAG, "搜索联系人: $keyword")
        
        // 从SharedPreferences加载所有联系人
        val prefs = getSharedPreferences(CONTACTS_PREFS, android.content.Context.MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        val allContacts = mutableListOf<Contact>()
        if (contactsJson != null) {
            try {
                val gson = com.google.gson.Gson()
                val contactArray = gson.fromJson(contactsJson, Array<Contact>::class.java)
                allContacts.addAll(contactArray)
            } catch (e: Exception) {
                Log.e(TAG, "解析联系人数据失败: ${e.message}", e)
            }
        }
        
        // 搜索联系人
        val searchResults = allContacts.filter {
            it.name.contains(keyword, ignoreCase = true) ||
            it.wechatNote.contains(keyword, ignoreCase = true) ||
            it.phoneNumber.contains(keyword, ignoreCase = true)
        }
        
        Log.d(TAG, "搜索结果: ${searchResults.size} 个联系人")
        
        // 显示搜索结果
        if (searchResults.isEmpty()) {
            android.widget.Toast.makeText(this, "未找到匹配的联系人", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            // 显示搜索结果对话框
            showSearchResultsDialog(searchResults)
        }
    }
    
    private fun showSearchResultsDialog(results: List<Contact>) {
        Log.d(TAG, "显示搜索结果对话框")
        
        // 创建搜索结果对话框
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("搜索结果 (${results.size} 个)")
        
        // 创建结果列表
        val resultItems = results.map { contact ->
            val name = contact.wechatNote.ifEmpty { contact.name }
            val info = if (contact.phoneNumber.isNotEmpty()) " | ${contact.phoneNumber}" else ""
            "$name$info"
        }.toTypedArray()
        
        builder.setItems(resultItems) { dialog, which ->
            val selectedContact = results[which]
            Log.d(TAG, "选择了搜索结果: ${selectedContact.wechatNote}")
            // 启动添加联系人页面并传递联系人信息
            val intent = android.content.Intent(this, AddContactActivity::class.java)
            intent.putExtra("contact_id", selectedContact.id)
            intent.putExtra("contact_name", selectedContact.name)
            intent.putExtra("contact_phone", selectedContact.phoneNumber)
            intent.putExtra("contact_wechat", selectedContact.wechatNote)
            intent.putExtra("contact_image", selectedContact.imagePath)
            intent.putExtra("contact_video", selectedContact.hasWechatVideo)
            intent.putExtra("contact_voice", selectedContact.hasWechatVoice)
            intent.putExtra("contact_call", selectedContact.hasPhoneCall)
            startActivity(intent)
        }
        
        builder.setNegativeButton("关闭") { dialog, which ->
            dialog.cancel()
        }
        
        builder.show()
    }
}