package net.tttttt.www.forum_qa_app.entities

import java.io.Serializable

class DataReviewTag(
    id: Int,
    name: String,
    enable: Boolean
): Serializable {

    companion object {
        val TAG = "DataReviewTag"
    }

    var id: Int = 0
    var name: String = ""
    var enable: Boolean = false

    init {
        this.id = id
        this.name = name
        this.enable = enable
    }

}