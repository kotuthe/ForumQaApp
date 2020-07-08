package net.tochinavi.www.tochinaviapp.entities

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import net.tochinavi.www.tochinaviapp.value.convertString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import java.util.*

class DataNarrowMulti(
    id: Int,
    name: String,
    parent_id: Int,
    checked: Boolean
) {
    var id: Int = 0
    var name: String = ""
    var parent_id: Int = 0
    var checked: Boolean = false

    companion object {
        val TAG = "DataNarrowMulti"
    }

    init {
        this.id = id
        this.name = name
        this.parent_id = parent_id
        this.checked = checked
    }
}