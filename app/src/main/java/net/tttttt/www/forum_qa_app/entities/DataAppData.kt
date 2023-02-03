package net.tttttt.www.forum_qa_app.entities

import android.util.Log
import net.tttttt.www.forum_qa_app.value.convertString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import java.util.*

class DataAppData(
    id: Int,
    modified: Date?
) {
    var id: Int = 0
    var modified: Date? = null

    companion object {
        val TAG = "DataAppData"
        val TABLE_NAME = "app_data"
        val TABLE_NAME_OMISSION = "app_data"
        val COL = arrayOf("id", "modified")
    }

    init {
        this.id = id
        this.modified = modified
    }

    fun _debug_log() {
        var _modified = ""
        ifNotNull(modified, {
            _modified = it.convertString()
        })
        Log.i(TAG, "id: $id, modified: $_modified")
    }
}