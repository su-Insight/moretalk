package com.example.onepass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddContactActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AddContactActivity"
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_STORAGE_PERMISSION = 101
        private const val REQUEST_TAKE_PHOTO = 102
        private const val REQUEST_PICK_IMAGE = 103
    }
    

    private lateinit var contactImage: ImageView
    private lateinit var btnSave: android.widget.Button
    private lateinit var btnDelete: android.widget.Button
    private var contactId: Int? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AddContactActivity onCreate 开始")
        
        setContentView(R.layout.activity_add_contact)
        
        Log.d(TAG, "布局设置成功，开始初始化视图")
        
        contactImage = findViewById(R.id.contactImage)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        
        contactImage.setOnClickListener {
            Log.d(TAG, "联系人图片被点击")
            // 打开图片选择对话框
            showImagePickerDialog()
        }
        
        btnSave.setOnClickListener {
            Log.d(TAG, "保存按钮被点击")
            // 保存联系人功能
            saveContact()
        }
        
        btnDelete.setOnClickListener {
            Log.d(TAG, "删除按钮被点击")
            // 删除联系人功能
            deleteContact()
        }
        
        // 加载联系人信息
        loadContactInfo()
        
        Log.d(TAG, "AddContactActivity onCreate 完成")
    }
    
    private fun loadContactInfo() {
        Log.d(TAG, "加载联系人信息")
        
        // 从Intent获取联系人信息
        contactId = intent.getIntExtra("contact_id", -1)
        if (contactId != -1) {
            val name = intent.getStringExtra("contact_name")
            val wechatNote = intent.getStringExtra("contact_wechat")
            val phoneNumber = intent.getStringExtra("contact_phone")
            val imagePath = intent.getStringExtra("contact_image")
            val hasWechatVideo = intent.getBooleanExtra("contact_video", false)
            val hasWechatVoice = intent.getBooleanExtra("contact_voice", false)
            val hasPhoneCall = intent.getBooleanExtra("contact_call", false)
            
            Log.d(TAG, "加载联系人信息成功: name=$name, wechatNote=$wechatNote, phoneNumber=$phoneNumber, imagePath=$imagePath")
            Log.d(TAG, "选项: 微信视频=$hasWechatVideo, 微信语音=$hasWechatVoice, 拨打电话=$hasPhoneCall")
            
            // 填充联系人信息到输入字段
            findViewById<android.widget.EditText>(R.id.editName).setText(name)
            findViewById<android.widget.EditText>(R.id.editWechatNote).setText(wechatNote)
            findViewById<android.widget.EditText>(R.id.editPhoneNumber).setText(phoneNumber)
            findViewById<android.widget.CheckBox>(R.id.checkWechatVideo).isChecked = hasWechatVideo
            findViewById<android.widget.CheckBox>(R.id.checkWechatVoice).isChecked = hasWechatVoice
            findViewById<android.widget.CheckBox>(R.id.checkPhoneCall).isChecked = hasPhoneCall
            
            // 加载联系人头像
            currentPhotoPath = imagePath
            if (!imagePath.isNullOrEmpty()) {
                loadContactImage(imagePath)
            }
        } else {
            Log.d(TAG, "没有联系人信息，创建新联系人")
        }
    }
    
    private fun loadContactImage(imagePath: String) {
        try {
            val bitmap = android.graphics.BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                contactImage.setImageBitmap(bitmap)
                Log.d(TAG, "联系人头像加载成功")
            } else {
                Log.e(TAG, "联系人头像加载失败: bitmap为null")
                contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载联系人头像失败", e)
            contactImage.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
    }
    
    private fun showImagePickerDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("选择图片")
            .setItems(arrayOf("拍照", "从相册选择")) { dialog, which ->
                when (which) {
                    0 -> {
                        checkCameraPermissionAndTakePhoto()
                    }
                    1 -> {
                        checkStoragePermissionAndPickImage()
                    }
                }
            }
        builder.show()
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            dispatchTakePictureIntent()
        }
    }
    
    private fun checkStoragePermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        } else {
            dispatchPickImageIntent()
        }
    }
    
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                Log.e(TAG, "创建图片文件失败", ex)
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show()
            }
            
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.onepass.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }
    
    private fun dispatchPickImageIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, REQUEST_PICK_IMAGE)
    }
    
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchPickImageIntent()
                } else {
                    Toast.makeText(this, "存储权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (resultCode == RESULT_OK) {
                    currentPhotoPath?.let { path ->
                        val bitmap = android.graphics.BitmapFactory.decodeFile(path)
                        contactImage.setImageBitmap(bitmap)
                        Toast.makeText(this, "拍照成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            REQUEST_PICK_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImage: Uri = data.data!!
                    try {
                        val inputStream = contentResolver.openInputStream(selectedImage)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        contactImage.setImageBitmap(bitmap)
                        
                        // 保存选择的图片到应用私有目录
                        val savedPath = saveImageToPrivateStorage(bitmap)
                        currentPhotoPath = savedPath
                        Toast.makeText(this, "图片选择成功", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "加载图片失败", e)
                        Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun saveImageToPrivateStorage(bitmap: android.graphics.Bitmap): String? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            
            if (storageDir == null) {
                Log.e(TAG, "无法获取存储目录")
                return null
            }
            
            val imageFile = File(storageDir, "CONTACT_${timeStamp}.jpg")
            
            val outputStream = java.io.FileOutputStream(imageFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            Log.d(TAG, "图片保存成功: ${imageFile.absolutePath}")
            imageFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "保存图片失败", e)
            null
        }
    }
    
    private fun saveContact() {
        // 获取输入内容
        val name = findViewById<android.widget.EditText>(R.id.editName).text.toString()
        val wechatNote = findViewById<android.widget.EditText>(R.id.editWechatNote).text.toString()
        val phoneNumber = findViewById<android.widget.EditText>(R.id.editPhoneNumber).text.toString()
        
        // 获取选中的选项
        val checkWechatVideo = findViewById<android.widget.CheckBox>(R.id.checkWechatVideo).isChecked
        val checkWechatVoice = findViewById<android.widget.CheckBox>(R.id.checkWechatVoice).isChecked
        val checkPhoneCall = findViewById<android.widget.CheckBox>(R.id.checkPhoneCall).isChecked
        
        // 验证逻辑
        if (name.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        if ((checkWechatVideo || checkWechatVoice) && wechatNote.isEmpty()) {
            Toast.makeText(this, "微信备注不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (checkPhoneCall) {
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 手机号格式校验
            val phonePattern = "^1[3-9]\\d{9}$"
            if (!android.util.Patterns.PHONE.matcher(phoneNumber).matches() && !phoneNumber.matches(phonePattern.toRegex())) {
                Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // 使用传递过来的contactId或生成新的ID
        val finalContactId = if (contactId != null && contactId != -1) contactId else System.currentTimeMillis().toInt()
        
        // 创建联系人对象
        val contact = Contact(
            id = finalContactId!!,
            name = name,
            phoneNumber = phoneNumber,
            wechatNote = wechatNote,
            hasWechatVideo = checkWechatVideo,
            hasWechatVoice = checkWechatVoice,
            hasPhoneCall = checkPhoneCall,
            imagePath = currentPhotoPath
        )
        
        Log.d(TAG, "保存联系人: 姓名=$name, 微信备注=$wechatNote, 手机号=$phoneNumber, 图片路径=$currentPhotoPath")
        Log.d(TAG, "选项: 微信视频=$checkWechatVideo, 微信语音=$checkWechatVoice, 拨打电话=$checkPhoneCall")
        
        // 直接保存联系人到SharedPreferences
        saveContactToSharedPreferences(contact)
        
        Toast.makeText(this, "联系人保存成功", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun saveContactToSharedPreferences(contact: Contact) {
        val CONTACTS_PREFS = "contacts_prefs"
        val KEY_CONTACTS = "contacts"
        
        Log.d(TAG, "保存联系人到SharedPreferences: ${contact.wechatNote}")
        
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
    
    private fun deleteContact() {
        Log.d(TAG, "删除联系人")
        
        if (contactId == null || contactId == -1) {
            Toast.makeText(this, "没有可删除的联系人", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建确认对话框
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("删除联系人")
        builder.setMessage("确定要删除这个联系人吗？")
        
        builder.setPositiveButton("确定") { dialog, which ->
            // 执行删除操作
            val success = removeContactFromStorage(contactId!!)
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