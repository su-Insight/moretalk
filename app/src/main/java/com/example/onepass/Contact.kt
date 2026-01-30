package com.example.onepass

import android.os.Parcel
import android.os.Parcelable

class Contact(
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val wechatNote: String,
    val hasWechatVideo: Boolean,
    val hasWechatVoice: Boolean,
    val hasPhoneCall: Boolean,
    val imagePath: String? = null
) : Parcelable {
    companion object {
        const val CONTACTS_PREFS = "contacts_prefs"
        const val KEY_CONTACTS = "contacts"
        
        @JvmField
        val CREATOR = object : Parcelable.Creator<Contact> {
            override fun createFromParcel(parcel: Parcel): Contact {
                return Contact(
                    parcel.readInt(),
                    parcel.readString() ?: "",
                    parcel.readString() ?: "",
                    parcel.readString() ?: "",
                    parcel.readByte() != 0.toByte(),
                    parcel.readByte() != 0.toByte(),
                    parcel.readByte() != 0.toByte(),
                    parcel.readString()
                )
            }
            
            override fun newArray(size: Int): Array<Contact?> {
                return arrayOfNulls(size)
            }
        }
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(phoneNumber)
        parcel.writeString(wechatNote)
        parcel.writeByte(if (hasWechatVideo) 1 else 0)
        parcel.writeByte(if (hasWechatVoice) 1 else 0)
        parcel.writeByte(if (hasPhoneCall) 1 else 0)
        parcel.writeString(imagePath)
    }
}