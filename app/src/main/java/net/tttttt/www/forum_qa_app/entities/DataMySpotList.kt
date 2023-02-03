package net.tttttt.www.forum_qa_app.entities

class DataMySpotList(
    id: Int,
    type: Int, // 1:スポット, それ以外:病院
    name: String,
    address: String,
    category: String,
    image_url: String,
    date: String
) {

    companion object {
        val TAG = "DataMySpotList"
    }

    var id: Int = 0
    var type: Int = 0
    var name: String = ""
    var address: String = ""
    var category: String = ""
    var image_url: String = ""
    var date: String = ""


    init {
        this.id = id
        this.type = type
        this.name  = name
        this.address  = address
        this.category  = category
        this.image_url  = image_url
        this.date = date
    }
}