package net.tttttt.www.forum_qa_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotReviewGalleryImageSearchBinding
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.HttpSpotInfo
import net.tttttt.www.forum_qa_app.value.Constants
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.RecyclerISReviewGalleryAdapter
import net.tttttt.www.forum_qa_app.view.RecyclerInfiniteScrollListener

class ActivitySpotReviewGallery_ImageSearch :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener  {

    companion object {
        val TAG = "ActivitySpotReviewGallery_ImageSearch"
        val TAG_SHORT = "SpotReviewGallery_IS"
    }

    private lateinit var binding: ActivitySpotReviewGalleryImageSearchBinding

    private val REQUEST_ALERT_NO_DATA: Int = 0x1

    // データ
    private lateinit var mContext: Context
    private var selectIndex = 0
    private var allNumber = 0
    private var condPage = 1
    private lateinit var dataSpot: DataSpotInfo
    private var imageListData: ArrayList<DataSpotReview> = arrayListOf()

    private lateinit var mAdapter: RecyclerISReviewGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_review_gallery_image_search)
        binding = ActivitySpotReviewGalleryImageSearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar!!.hide()

        mContext = applicationContext

        // データの取得
        selectIndex = intent.getIntExtra("selectIndex", 0)
        allNumber = intent.getIntExtra("allNumber", 0)
        condPage = intent.getIntExtra("condPage", 0)
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        imageListData = intent.getSerializableExtra("imageListData") as ArrayList<DataSpotReview>

        initLayout()
    }

    /**
     * アラート　ポジティブ
     */
    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_NO_DATA -> {
                finish()
            }
        }
    }

    /**
     * アラート　ネガティブ
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    /**
     * UIの初期設定
     */
    private fun initLayout() {
        binding.viewPageClose.setOnClickListener{
            // 戻った時にこちらで取得したクチコミデータをスポットページで更新するかを検討する
            finish()
        }

        binding.buttonReviewDetail.setOnClickListener {
            // クチコミ詳細へ
            val item = imageListData[selectIndex]
            val intent = Intent(this, ActivitySpotReviewDetail_ImageSearch::class.java)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("dataReview", item)
            startActivity(intent)
        }

        binding.textViewSpotName.text = dataSpot.name

        mAdapter = RecyclerISReviewGalleryAdapter(mContext, imageListData)

        // ページコントロール
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        // recyclerViewの設定
        binding.recyclerView.apply {
            // 表示計算の最適化（推奨）
            setHasFixedSize(true)

            val manager = GridLayoutManager(context, mAdapter.spanCount)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            manager.spanSizeLookup = mAdapter.spanSizeLookup
            layoutManager = manager
            addItemDecoration(mAdapter.mItemDecoration)
            adapter = mAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val manager = recyclerView.layoutManager as LinearLayoutManager?
                    val last = manager!!.findLastVisibleItemPosition()
                    updateItemView(last)
                }
            })

            addOnScrollListener(RecyclerInfiniteScrollListener(manager) {
                // 続きを検索
                getData()
            })

            scrollToPosition(selectIndex)
            updateItemView(selectIndex)
        }
    }

    /** アイテムの更新  */
    private fun updateItemView(index: Int) {
        selectIndex = index
        val item = imageListData[index]

        var title = ""
        if (allNumber >= 1) {
            // 全枚数 = 写真枚数 + 店舗写真
            title = "%d / %d".format(index + 1, allNumber + 1)
        }
        binding.textViewNumber.text = title

        if (index == 0) {
            binding.textViewDate.visibility = View.INVISIBLE
            binding.buttonReviewDetail.visibility = View.INVISIBLE
        } else {
            binding.textViewDate.visibility = View.VISIBLE
            binding.buttonReviewDetail.visibility = View.VISIBLE
            binding.textViewDate.text = item.reviewDate
        }
    }

    /**
     * クチコミ画像を取得
     */
    private fun getData() {
        HttpSpotInfo(mContext).get_review_images(
            dataSpot,
            condPage,
            { datas, all_number ->
                allNumber = all_number
                imageListData.addAll(datas)
                mAdapter.notifyDataSetChanged()
                condPage++
            },
            {
                if (condPage == 1) {
                    when (it) {
                        Constants.HTTP_STATUS.nodata, Constants.HTTP_STATUS.network -> {
                            val alert = AlertNormal.newInstance(
                                requestCode = REQUEST_ALERT_NO_DATA,
                                title = "クチコミを取得できません",
                                msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。",
                                positiveLabel = "OK",
                                negativeLabel = null
                            )
                            alert.show(supportFragmentManager, AlertNormal.TAG)
                        }
                    }
                }
            })
    }


}
