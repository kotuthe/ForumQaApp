package net.tttttt.www.forum_qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import net.tttttt.www.forum_qa_app.databinding.ActivityTopicsDetailBinding

class ActivityTopicsDetail : AppCompatActivity() {

    companion object {
        val TAG = "ActivityTopicsDetail"
        val TAG_SHORT = "TopicsDetail"
    }

    private lateinit var binding: ActivityTopicsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_topics_detail)

        binding = ActivityTopicsDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.title = "お知らせ"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.textDate.text = intent.getStringExtra("date")
        binding.textTitle.text = intent.getStringExtra("title")
        binding.textNote.text = intent.getStringExtra("note")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


}