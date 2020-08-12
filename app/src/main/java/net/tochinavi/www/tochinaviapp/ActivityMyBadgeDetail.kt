package net.tochinavi.www.tochinaviapp

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import coil.api.load
import kotlinx.android.synthetic.main.activity_my_badge_detail.*
import net.tochinavi.www.tochinaviapp.entities.DataBadge

class ActivityMyBadgeDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_badge_detail)

        val data = intent.getSerializableExtra("data") as DataBadge

        if (supportActionBar != null) {
            supportActionBar!!.title = data.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        imageViewIcon.load(data.imageUrl) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        textViewDetail.text = data.detail
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
