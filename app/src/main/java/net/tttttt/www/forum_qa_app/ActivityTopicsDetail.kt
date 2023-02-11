package net.tttttt.www.forum_qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import net.tttttt.www.forum_qa_app.R
import kotlinx.android.synthetic.main.activity_topics_detail.*

class ActivityTopicsDetail : AppCompatActivity() {

    companion object {
        val TAG = "ActivityTopicsDetail"
        val TAG_SHORT = "TopicsDetail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics_detail)

        if (supportActionBar != null) {
            supportActionBar!!.title = "お知らせ"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        textDate.text = intent.getStringExtra("date")
        textTitle.text = intent.getStringExtra("title")
        textNote.text = intent.getStringExtra("note")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


}