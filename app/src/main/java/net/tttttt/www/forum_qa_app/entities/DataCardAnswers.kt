package net.tttttt.www.forum_qa_app.entities


import java.util.*

class DataCardAnswers(
    date: Date,
    title: String,
    note: String,
    categories: Array<Int>
) {
    var date: Date = Date()
    var title: String = ""
    var note: String = ""
    var categories: Array<Int> = arrayOf()

    companion object {
        val TAG = "DataCardAnswers"
    }

    init {
        this.date = date
        this.title = title
        this.note = note
        this.categories = categories
    }
}