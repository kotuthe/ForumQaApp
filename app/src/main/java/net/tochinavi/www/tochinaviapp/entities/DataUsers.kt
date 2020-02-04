package net.tochinavi.www.tochinaviapp.entities

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import net.tochinavi.www.tochinaviapp.value.convertString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import java.util.*

class DataUsers(
    id: Int,
    user_id: Int,
    email: String,
    password: String,
    name: String,
    image: Bitmap?,
    auto_save: Boolean
) {
    var id: Int = 0
    var user_id: Int = 0
    var email: String = ""
    var password: String = ""
    var name: String = ""
    var image: Bitmap? = null
    var auto_save: Boolean = false

    companion object {
        val TAG = "DataUsers"
        val TABLE_NAME = "users"
        val TABLE_NAME_OMISSION = "users"
        val COL = arrayOf(
            "id",
            "user_id",
            "email",
            "password",
            "name",
            "image",
            "auto_save"
        )
    }

    init {
        this.id = id
        this.user_id = user_id
        this.email = email
        this.password = password
        this.name = name
        this.image = image
        this.auto_save = auto_save
    }

    fun _debug_log() {
        /*
        var _modified = ""
        ifNotNull(modified, {
            _modified = it.convertString()
        })
        Log.i(TAG, "id: $id, modified: $_modified")
        */
    }
}