package net.tttttt.www.forum_qa_app.entities


import java.util.*

class DataCardQuestions(
    date: Date,
    title: String,
    note: String,
    categories: Array<Int>,
    postNumber: Int
) {
    var date: Date = Date()
    var title: String = ""
    var note: String = ""
    var categories: Array<Int> = arrayOf()
    var postNumber: Int = 0

    companion object {
        val TAG = "DataCardQuestions"
    }

    init {
        this.date = date
        this.title = title
        this.note = note
        this.categories = categories
        this.postNumber = postNumber
    }
}