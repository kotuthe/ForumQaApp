package net.tttttt.www.forum_qa_app.entities

import java.io.Serializable

class DataBadge(
    id: Int,
    name: String,
    imageUrl: String,
    detail: String,
    getFlag: Boolean
): Serializable {

    companion object {
        val TAG = "DataBadge"
    }

    var id: Int = 0
    var name: String = ""
    var imageUrl: String = ""
    var detail: String = ""
    var getFlag: Boolean = false

    init {
        this.id = id
        this.name = name
        this.imageUrl = imageUrl
        this.detail = detail
        this.getFlag = getFlag
    }

}