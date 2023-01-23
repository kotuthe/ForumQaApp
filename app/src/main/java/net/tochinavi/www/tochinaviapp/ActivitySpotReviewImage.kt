package net.tochinavi.www.tochinaviapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import kotlinx.android.synthetic.main.activity_spot_review_image.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper

class ActivitySpotReviewImage : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotReviewImage"
        val TAG_SHORT = "SpotReviewImage"
    }

    private lateinit var firebase: FirebaseHelper

    // データ
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var dataReview: DataSpotReview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_review_image)

        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        firebase = FirebaseHelper(applicationContext)
        firebase.sendScreen(
            FirebaseHelper.screenName.Kuchikomi_Image,
            arrayListOf(Pair("id", dataSpot.id.toString()), Pair("kuchikomi_id", dataReview.id)))

        if (supportActionBar != null) {
            supportActionBar!!.title = dataReview.spotName
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        if (dataReview.userId > 0 || !dataReview.userName.isEmpty()) {
            imageViewUserIcon.load(dataReview.userImage) {
                placeholder(R.drawable.ic_image_placeholder)
            }
            textViewUserName.text = dataReview.userName
            textViewUserDetail.text = dataReview.userInfo
        } else {
            layoutUser.visibility = View.GONE
        }

        imageView.load(dataReview.reviewImageUrls[0])
        textViewOtherNumber.apply {
            if (dataReview.reviewImageUrls.size > 1) {
                visibility = View.VISIBLE
                text = "他%d件".format(dataReview.reviewImageUrls.size - 1)
            } else {
                visibility = View.GONE
            }
        }

        if (!dataReview.review.isEmpty()) {
            textViewReview.text = dataReview.review
            layoutReview.setOnClickListener {
                // クチコミ詳細へ
                val intent = Intent(this@ActivitySpotReviewImage, ActivitySpotReviewDetail::class.java)
                intent.putExtra("dataSpot", dataSpot)
                intent.putExtra("dataReview", dataReview)
                startActivityForResult(intent, 0)
            }
        } else {
            layoutReview.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
