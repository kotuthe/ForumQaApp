package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_spot_review_gallery_image_search.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.network.HttpSpotInfo
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.RecyclerISReviewGalleryAdapter
import net.tochinavi.www.tochinaviapp.view.RecyclerInfiniteScrollListener

class ActivitySpotReviewGallery_ImageSearch :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener  {

    companion object {
        val TAG = "ActivitySpotReviewGallery_ImageSearch"
        val TAG_SHORT = "SpotReviewGallery_IS"
    }

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
        setContentView(R.layout.activity_spot_review_gallery_image_search)

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
        viewPageClose.setOnClickListener{
            // 戻った時にこちらで取得したクチコミデータをスポットページで更新するかを検討する
            finish()
        }

        buttonReviewDetail.setOnClickListener {
            // クチコミ詳細へ
            val item = imageListData[selectIndex]
            val intent = Intent(this, ActivitySpotReviewDetail_ImageSearch::class.java)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("dataReview", item)
            startActivity(intent)
        }

        textViewSpotName.text = dataSpot.name

        mAdapter = RecyclerISReviewGalleryAdapter(mContext, imageListData)

        // ページコントロール
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // recyclerViewの設定
        recyclerView.apply {
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
        textViewNumber.text = title

        if (index == 0) {
            textViewDate.visibility = View.INVISIBLE
            buttonReviewDetail.visibility = View.INVISIBLE
        } else {
            textViewDate.visibility = View.VISIBLE
            buttonReviewDetail.visibility = View.VISIBLE
            textViewDate.text = item.reviewDate
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
