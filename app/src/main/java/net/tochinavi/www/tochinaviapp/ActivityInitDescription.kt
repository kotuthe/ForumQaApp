package net.tochinavi.www.tochinaviapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_init_description.*
import net.tochinavi.www.tochinaviapp.view.RecyclerInitDescriptionAdapter
import java.util.*

class ActivityInitDescription : AppCompatActivity() {
    companion object {
        val TAG = "ActivityInitDescription"
    }
    // 定数 //
    private val imageDatas = Arrays.asList(
        R.drawable.img_top_init_01,
        R.drawable.img_top_init_02,
        R.drawable.img_top_init_03,
        R.drawable.img_top_init_04,
        R.drawable.img_top_init_05,
        R.drawable.img_top_init_06
    )

    // 変数 //
    private var mAdapter: RecyclerInitDescriptionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_description)

        buttonClose.isEnabled = false
        buttonClose.setOnClickListener {
            // データベースのアップデート
            val intent = Intent(this, ActivityAppDataUpdate::class.java)
            intent.putExtra("tag", TAG)
            startActivity(intent)
            finish()
        }

        initLayout()
    }

    private fun initLayout() {

        mAdapter = RecyclerInitDescriptionAdapter(this, imageDatas)

        // ページコントロール
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // recyclerViewの設定
        recyclerView.apply {
            // 表示計算の最適化（推奨）
            setHasFixedSize(true)

            var manager = GridLayoutManager(context, mAdapter!!.spanCount)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            manager.spanSizeLookup = mAdapter!!.spanSizeLookup
            layoutManager = manager
            addItemDecoration(mAdapter!!.mItemDecoration)
            // ページャーver
            // addItemDecoration(RecyclerItemLinePager(context))
            adapter = mAdapter

        }

        // スクロール
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recyclerView.layoutManager as LinearLayoutManager?
                if (manager != null) {
                    val position = manager!!.findLastVisibleItemPosition()
                    // 閉じるボタンを表示する
                    if (position == mAdapter!!.itemCount - 1) {
                        buttonClose.isEnabled = true
                    }
                }
            }
        })
    }

}
