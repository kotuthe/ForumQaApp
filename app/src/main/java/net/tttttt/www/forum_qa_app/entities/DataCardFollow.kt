package net.tttttt.www.forum_qa_app.entities

class DataCardFollow(
    imgUser: String,
    info: String,
    name: String,
    isFollow: Boolean = false
) {
    var imgUser: String = ""
    var info: String = ""
    var name: String = ""
    var isFollow: Boolean = false

    companion object {
        val TAG = "DataCardFollow"
    }

    init {
        this.imgUser = imgUser
        this.info = info
        this.name = name
        this.isFollow = isFollow
    }
}