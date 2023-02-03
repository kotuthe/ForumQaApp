package net.tttttt.www.forum_qa_app.entities


import java.util.*

class DataCardTopics(
    date: Date,
    title: String,
    note: String,
    isCaution: Boolean = false
) {
    var date: Date = Date()
    var title: String = ""
    var note: String = ""
    var isCaution: Boolean = false

    companion object {
        val TAG = "DataCardTopics"
    }

    init {
        this.date = date
        this.title = title
        this.note = note
        this.isCaution = isCaution
    }
}