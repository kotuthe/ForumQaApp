package net.tttttt.www.forum_qa_app

import android.content.Context
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
import net.tttttt.www.forum_qa_app.view.RecyclerISReviewGalleryPreviewAdapter
import net.tttttt.www.forum_qa_app.view.RecyclerInfiniteScrollListener

class ActivitySpotReviewGalleryPreview_ImageSearch :
    AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotReviewGalleryPreview_ImageSearch"
        val TAG_SHORT = "ReviewGalleryPre_IS"
    }

    private lateinit var binding: ActivitySpotReviewGalleryImageSearchBinding

    // データ
    private lateinit var mContext: Context
    private var selectIndex = 0
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var dataReview: DataSpotReview

    private lateinit var mAdapter: RecyclerISReviewGalleryPreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_review_gallery_image_search)
        binding = ActivitySpotReviewGalleryImageSearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext

        // データの取得
        selectIndex = intent.getIntExtra("selectIndex", 0)
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        initLayout()
    }

    /**
     * UIの初期設定
     */
    private fun initLayout() {
        binding.viewPageClose.setOnClickListener{
            finish()
        }

        binding.buttonReviewDetail.visibility = View.GONE
        binding.textViewSpotName.text = dataSpot.name
        binding.textViewDate.text = dataReview.reviewDate

        mAdapter = RecyclerISReviewGalleryPreviewAdapter(mContext, dataReview.reviewImageUrls)

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
                // 最後尾のスクロール
                // Log.i(">> $TAG_SHORT", "続きから：$condPage")
            })

            scrollToPosition(selectIndex)
            updateItemView(selectIndex)
        }
    }

    /** アイテムの更新  */
    private fun updateItemView(index: Int) {
        selectIndex = index

        var title = ""
        val all = dataReview.reviewImageUrls.size
        if (all >= 2) {
            // 2以上
            title = "%d / %d".format(index + 1, all)
        }
        binding.textViewNumber.text = title
    }



}
