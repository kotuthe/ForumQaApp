package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import coil.api.load
import kotlinx.android.synthetic.main.activity_spot_review_detail.*
import kotlinx.android.synthetic.main.view_spot_review_detail_spot.view.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.view.RecyclerReviewDetailImageAdapter

// タイプをintentに入れて、スポット以外も表示できるように調整する
//　スポット以外のクチコミはMyクチコミから

class ActivitySpotReviewDetail : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotReviewDetail"
        val TAG_SHORT = "SpotReviewDetail"
    }

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var dataSpot: DataSpotInfo // 多分　null対応にしないといけない
    private lateinit var dataReview: DataSpotReview

    private lateinit var mAdapter: RecyclerReviewDetailImageAdapter
    private var imageData: ArrayList<String> = ArrayList()
    private var isClearFinish: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_review_detail)

        mContext = applicationContext
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        // 遷移元がギャラリー・クチコミ一覧の場合はクリアしてSpotInfoを表示する
        // ※これを使用するにはstartActivityForResultを使わないと動かない
        if (callingActivity != null) {
            val className = callingActivity!!.className
            if (Regex(ActivitySpotReviewImageList.TAG).containsMatchIn(className)) {
                isClearFinish = true
            }
            if (Regex(ActivitySpotReviewList.TAG).containsMatchIn(className)) {
                isClearFinish = true
            }
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        updateLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateLayout() {
        // スポット情報
        layoutSpot.apply {
            imageViewSpot.load(dataSpot.imageUrl) {
                placeholder(R.drawable.ic_image_placeholder)
            }
            textViewName.text = dataSpot.name
            textViewDetail.text = dataSpot.simple_detail
            setOnClickListener {
                // firebase
                if (isClearFinish) {
                    val intent =
                        Intent(this@ActivitySpotReviewDetail, ActivitySpotInfo::class.java)
                    // 展開されているActivityをクリア
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } else {
                    finish()
                }
            }
        }
        layoutOther.visibility = View.GONE

        // ユーザー情報
        textViewDate.text = "投稿日: %s".format(dataReview.reviewDate)
        imageViewUserIcon.load(dataReview.userImage) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        textViewUserName.text = dataReview.userName
        textViewUserDetail.text = dataReview.userInfo
        textViewReview.apply {
            if (dataReview.review.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = dataReview.review
            }
        }
        textViewReviewInfo.apply {
            if (dataReview.goodNum > 0) {
                visibility = View.VISIBLE
                text = "グッときた　%d件".format(dataReview.goodNum)
            } else {
                visibility = View.GONE
            }
        }

        // クチコミ画像
        mAdapter = RecyclerReviewDetailImageAdapter(this, imageData)
        recyclerView.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 列
            val lmg = GridLayoutManager(context, mAdapter.spanCount)
            lmg.spanSizeLookup = mAdapter.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(mAdapter.mItemDecoration)
            adapter = mAdapter

            // クチコミ写真をタップしている時はScrollviewはスクロールさせない
            setOnTouchListener { view, motionEvent ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
        mAdapter.setOnItemClickListener(View.OnClickListener { view ->
            // 画像プレビュー
            val index = view.id
            val intent = Intent(this@ActivitySpotReviewDetail, ActivitySpotReviewGalleryPreview_ImageSearch::class.java)
            intent.putExtra("selectIndex", index)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("dataReview", dataReview)
            startActivity(intent)
        })

        if (dataReview.reviewImageUrls.size > 0) {
            imageData.addAll(dataReview.reviewImageUrls)
        } else {
            recyclerView.visibility = View.GONE
        }

    }
}
