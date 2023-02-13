package net.tttttt.www.forum_qa_app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import coil.load
import net.tttttt.www.forum_qa_app.databinding.ActivityMyBadgeDetailBinding
import net.tttttt.www.forum_qa_app.entities.DataBadge

class ActivityMyBadgeDetail : AppCompatActivity() {

    private lateinit var binding: ActivityMyBadgeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_my_badge_detail)
        binding = ActivityMyBadgeDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val data = intent.getSerializableExtra("data") as DataBadge

        if (supportActionBar != null) {
            supportActionBar!!.title = data.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.imageViewIcon.load(data.imageUrl) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        binding.textViewDetail.text = data.detail
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
