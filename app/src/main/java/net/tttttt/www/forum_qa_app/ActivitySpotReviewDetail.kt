package net.tttttt.www.forum_qa_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotReviewDetailBinding
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.view.RecyclerReviewDetailImageAdapter

// タイプをintentに入れて、スポット以外も表示できるように調整する
//　スポット以外のクチコミはMyクチコミから

class ActivitySpotReviewDetail : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotReviewDetail"
        val TAG_SHORT = "SpotReviewDetail"
    }

    private lateinit var binding: ActivitySpotReviewDetailBinding
    private lateinit var firebase: FirebaseHelper

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var dataSpot: DataSpotInfo // 多分　null対応にしないといけない
    private lateinit var dataReview: DataSpotReview

    private lateinit var mAdapter: RecyclerReviewDetailImageAdapter
    private var imageData: ArrayList<String> = ArrayList()
    private var isClearFinish: Boolean = false
    private var isNextSpotInfo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_review_detail)
        binding = ActivitySpotReviewDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        firebase.sendScreen(
            FirebaseHelper.screenName.Spot_Info_Kuchikomi_Detail,
            arrayListOf(Pair("id", dataSpot.id.toString()), Pair("kuchikomi_id", dataReview.id)))

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
            if (Regex(ActivitySpotReviewImage.TAG).containsMatchIn(className)) {
                isClearFinish = true
            }
            if (Regex(ActivityMyReviewList.TAG).containsMatchIn(className)) {
                isNextSpotInfo = true
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
        binding.layoutSpot.apply {
            imageViewSpot.load(dataSpot.imageUrl) {
                placeholder(R.drawable.ic_image_placeholder)
            }
            textViewName.text = dataSpot.name
            textViewDetail.text = dataSpot.simple_detail
            /*setOnClickListener {
                firebase.sendSpotInfo(
                    FirebaseHelper.screenName.Spot_Info_Kuchikomi_Detail, 1, dataSpot.id)
                if (isClearFinish) {
                    // 展開されているActivityをクリア
                    val intent =
                        Intent(this@ActivitySpotReviewDetail, ActivitySpotInfo::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } else if (isNextSpotInfo) {
                    // スポットページへ
                    val intent = Intent(this@ActivitySpotReviewDetail, ActivitySpotInfo::class.java)
                    intent.putExtra("id", dataSpot.id)
                    intent.putExtra("name", dataSpot.name)
                    startActivity(intent)
                } else {
                    // 戻る
                    finish()
                }
            }*/
        }
        // error
        // binding.layoutOther.visibility = View.GONE

        // ユーザー情報
        binding.textViewDate.text = "投稿日: %s".format(dataReview.reviewDate)
        binding.imageViewUserIcon.load(dataReview.userImage) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        binding.textViewUserName.text = dataReview.userName
        binding.textViewUserDetail.text = dataReview.userInfo
        binding.textViewReview.apply {
            if (dataReview.review.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = dataReview.review
            }
        }
        binding.textViewReviewInfo.apply {
            if (dataReview.goodNum > 0) {
                visibility = View.VISIBLE
                text = getString(R.string.spot_review_detail_good_kita).format(dataReview.goodNum)
            } else {
                visibility = View.GONE
            }
        }

        // クチコミ画像
        mAdapter = RecyclerReviewDetailImageAdapter(this, imageData)
        binding.recyclerView.apply {
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
            binding.recyclerView.visibility = View.GONE
        }

    }
}
