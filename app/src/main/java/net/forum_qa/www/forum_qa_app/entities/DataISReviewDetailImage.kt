package net.tttttt.www.forum_qa_app.entities

class DataISReviewDetailImage(
    url: String,
    number: Int
) {

    companion object {
        val TAG = "DataISReviewDetailImage"
    }

    var url: String = ""
    var number: Int = 0

    init {
        this.url = url
        this.number = number
    }
}