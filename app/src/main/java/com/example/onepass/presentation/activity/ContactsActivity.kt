package com.example.onepass.presentation.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.onepass.R
import com.example.onepass.domain.model.Contact
import com.example.onepass.presentation.adapter.ContactAdapter
import com.example.onepass.utils.CryptoUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    val gson = Gson()
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
            val gson = Gson()
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
    

    private lateinit var btnSearchContact: android.widget.Button
    private lateinit var btnAddContact: android.widget.Button
    private lateinit var btnImportContacts: android.widget.Button
    private lateinit var btnExportContacts: android.widget.Button
    private lateinit var recyclerViewContacts: androidx.recyclerview.widget.RecyclerView
    private lateinit var textNoContacts: android.widget.TextView
    
    private var contacts: MutableList<Contact> = mutableListOf()
    private lateinit var contactAdapter: ContactAdapter

    // 导入文件选择器
    private val importContactLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { importContactsFromFile(it) }
    }

    // 导出文件选择器 (用于保存到指定位置)
    private val exportContactLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { saveContactsToUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ContactsActivity onCreate 开始")
        
        setContentView(R.layout.activity_contacts)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
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
            // 打开文件选择器，选择JSON文件
            importContactLauncher.launch(arrayOf("application/json"))
        }

        btnExportContacts.setOnClickListener {
            Log.d(TAG, "导出联系人按钮被点击")
            exportContacts()
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
                val gson = Gson()
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
        
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_search_contact, null)
        val editSearchInput = dialogView.findViewById<android.widget.EditText>(R.id.editSearchInput)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSearch = dialogView.findViewById<android.widget.Button>(R.id.btnSearch)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSearch.setOnClickListener {
            val searchText = editSearchInput.text.toString().trim()
            if (searchText.isNotEmpty()) {
                dialog.dismiss()
                searchContacts(searchText)
            } else {
                android.widget.Toast.makeText(this, "请输入搜索关键词", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        editSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val searchText = editSearchInput.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    dialog.dismiss()
                    searchContacts(searchText)
                } else {
                    android.widget.Toast.makeText(this, "请输入搜索关键词", android.widget.Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
        
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    private fun searchContacts(keyword: String) {
        Log.d(TAG, "搜索联系人: $keyword")
        
        // 从SharedPreferences加载所有联系人
        val prefs = getSharedPreferences(CONTACTS_PREFS, android.content.Context.MODE_PRIVATE)
        val contactsJson = prefs.getString(KEY_CONTACTS, null)
        
        val allContacts = mutableListOf<Contact>()
        if (contactsJson != null) {
            try {
                val gson = Gson()
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
        
        val dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_search_results, null)
        val textTitle = dialogView.findViewById<android.widget.TextView>(R.id.textTitle)
        val textCount = dialogView.findViewById<android.widget.TextView>(R.id.textCount)
        val recyclerViewSearchResults = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewSearchResults)
        val btnClose = dialogView.findViewById<android.widget.Button>(R.id.btnClose)
        
        textTitle.text = "搜索结果"
        textCount.text = "${results.size} 个"
        
        val adapter = SearchResultsAdapter(results)
        
        recyclerViewSearchResults.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerViewSearchResults.adapter = adapter
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private inner class SearchResultsAdapter(private val contacts: List<Contact>) : 
        androidx.recyclerview.widget.RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val textName: android.widget.TextView = view.findViewById(R.id.textContactName)
            val textWechat: android.widget.TextView = view.findViewById(R.id.textContactWechat)
            val textPhone: android.widget.TextView = view.findViewById(R.id.textContactPhone)
            val imageAvatar: android.widget.ImageView = view.findViewById(R.id.imageContactAvatar)
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_result, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = contacts[position]
            holder.textName.text = "姓名: ${contact.name}"
            holder.textWechat.text = "微信: ${contact.wechatNote}"
            holder.textPhone.text = "手机: ${contact.phoneNumber}"
            
            if (!contact.imagePath.isNullOrEmpty()) {
                val imageFile = java.io.File(contact.imagePath)
                if (imageFile.exists()) {
                    try {
                        val bitmap = decodeSampledBitmapFromFile(contact.imagePath, 200, 200)
                        if (bitmap != null) {
                            holder.imageAvatar.setImageBitmap(bitmap)
                        } else {
                            holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "加载头像失败: ${e.message}")
                        holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
                    }
                } else {
                    holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } else {
                holder.imageAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
            
            holder.itemView.setOnClickListener {
                Log.d(TAG, "点击搜索结果: ${contact.name}")
                val intent = android.content.Intent(this@ContactsActivity, AddContactActivity::class.java)
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
        }
        
        override fun getItemCount(): Int = contacts.size
    }

    /**
     * 计算合适的采样率
     */
    private fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

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
        val options = android.graphics.BitmapFactory.Options()
        options.inJustDecodeBounds = true
        android.graphics.BitmapFactory.decodeFile(path, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false
        return android.graphics.BitmapFactory.decodeFile(path, options)
    }

    /**
     * 导出联系人列表到文件
     */
    private fun exportContacts() {
        if (contacts.isEmpty()) {
            Toast.makeText(this, "没有联系人可导出", Toast.LENGTH_SHORT).show()
            return
        }

        // 生成默认文件名
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "onepass_contacts_$timestamp.json"

        // 打开文件保存对话框
        exportContactLauncher.launch(fileName)
    }

    /**
     * 保存联系人到指定URI
     */
    private fun saveContactsToUri(uri: Uri) {
        try {
            // 加密导出
            val encryptedContacts = contacts.map { c ->
                val encryptedPhone = CryptoUtil.encrypt(c.phoneNumber)
                Contact(
                    id = c.id,
                    name = c.name,
                    phoneNumber = encryptedPhone ?: c.phoneNumber,
                    wechatNote = c.wechatNote,
                    hasWechatVideo = c.hasWechatVideo,
                    hasWechatVoice = c.hasWechatVoice,
                    hasPhoneCall = c.hasPhoneCall,
                    imagePath = c.imagePath
                )
            }
            val gson = Gson()
            val contactsJson = gson.toJson(encryptedContacts)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contactsJson.toByteArray(Charsets.UTF_8))
            }

            Toast.makeText(this, "文件已保存至: $uri", Toast.LENGTH_LONG).show()
            Log.d(TAG, "联系人导出成功: $uri")
        } catch (e: IOException) {
            Log.e(TAG, "导出联系人失败: ${e.message}", e)
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 从文件导入联系人
     */
    private fun importContactsFromFile(uri: Uri) {
        try {
            val jsonString = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            }

            if (jsonString.isNullOrEmpty()) {
                Toast.makeText(this, "文件内容为空", Toast.LENGTH_SHORT).show()
                return
            }

            val gson = Gson()
            val importedContacts: List<Contact> = gson.fromJson(
                jsonString,
                object : TypeToken<List<Contact>>() {}.type
            )

            if (importedContacts.isEmpty()) {
                Toast.makeText(this, "文件中没有联系人", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取当前联系人列表
            val prefs = getSharedPreferences(CONTACTS_PREFS, MODE_PRIVATE)
            val currentJson = prefs.getString(KEY_CONTACTS, null)
            val currentContacts = mutableListOf<Contact>()

            currentJson?.let {
                try {
                    val currentArray = gson.fromJson(it, Array<Contact>::class.java)
                    currentContacts.addAll(currentArray)
                } catch (e: Exception) {
                    Log.e(TAG, "解析现有联系人失败: ${e.message}", e)
                }
            }

            // 合并联系人（避免ID冲突）
            val existingIds = currentContacts.map { c -> c.id }.toSet()
            var importCount = 0

            for (imported in importedContacts) {
                if (imported.id !in existingIds) {
                    // 解密手机号后再保存
                    val decryptedPhone = CryptoUtil.decrypt(imported.phoneNumber)
                    val decryptedContact = Contact(
                        id = imported.id,
                        name = imported.name,
                        phoneNumber = decryptedPhone ?: imported.phoneNumber,
                        wechatNote = imported.wechatNote,
                        hasWechatVideo = imported.hasWechatVideo,
                        hasWechatVoice = imported.hasWechatVoice,
                        hasPhoneCall = imported.hasPhoneCall,
                        imagePath = imported.imagePath
                    )
                    currentContacts.add(decryptedContact)
                    importCount++
                }
            }

            // 保存合并后的联系人列表
            val updatedJson = gson.toJson(currentContacts)
            prefs.edit().putString(KEY_CONTACTS, updatedJson).apply()

            Toast.makeText(this, "成功导入 $importCount 个联系人", Toast.LENGTH_SHORT).show()

            // 刷新列表
            loadContacts()

            Log.d(TAG, "导入联系人成功: $importCount 个")
        } catch (e: Exception) {
            Log.e(TAG, "导入联系人失败: ${e.message}", e)
            Toast.makeText(this, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}