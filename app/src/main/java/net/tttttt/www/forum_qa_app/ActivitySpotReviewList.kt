package net.tttttt.www.forum_qa_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotReviewListBinding
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.network.HttpSpotInfo
import net.tttttt.www.forum_qa_app.value.Constants
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.ListSpotReviewAdapter
import net.tttttt.www.forum_qa_app.view.TouchListenerSetSpeed

class ActivitySpotReviewList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotReviewList"
        val TAG_SHORT = "SpotReviewList"
    }

    private lateinit var binding: ActivitySpotReviewListBinding

    private val REQUEST_ALERT_NO_DATA: Int = 0x1

    private lateinit var firebase: FirebaseHelper

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var dataSpot: DataSpotInfo
    private var mAdapter: BaseAdapter? = null
    private var listData: ArrayList<DataSpotReview> = ArrayList()
    private var isEndScroll: Boolean = false

    // 条件
    private var condPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_review_list)
        binding = ActivitySpotReviewListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo

        firebase.sendScreen(
            FirebaseHelper.screenName.Spot_Info_Kuchikomi_List, arrayListOf(Pair("id", dataSpot.id.toString())))

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        hideListViewEmpty()

        mAdapter = ListSpotReviewAdapter(mContext, listData)
        binding.listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // クチコミ詳細へ
                val intent = Intent(this@ActivitySpotReviewList, ActivitySpotReviewDetail::class.java)
                intent.putExtra("dataSpot", dataSpot)
                intent.putExtra("dataReview", listData[pos])
                startActivityForResult(intent, 0)
            }

            // スクロール
            setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
                    // TODO("Not yet implemented")
                }

                override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {
                    if (!listData.isEmpty() && !isEndScroll) {
                        if ((p1 + p2 + 2) >= p3) {
                            isEndScroll = true
                            getData()
                        }
                    }
                }
            })

            // タッチ速度設定
            val changer = TouchListenerSetSpeed()
            setOnTouchListener { view, motionEvent ->
                changer.setOnTouch(motionEvent)
                false
            }
        }
        getData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
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

    private fun showListViewEmpty(message: String) {
        // error
        // binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutEmpty.textViewMsg.text = message
    }

    private fun hideListViewEmpty() {
    //error
    // binding.layoutEmpty.visibility = View.GONE
    }

    /**
     * クチコミの取得
     */
    private fun getData() {
        HttpSpotInfo(mContext).get_review_list(
            dataSpot,
            condPage,
            { datas, all_number ->
                binding.textViewNumber.text = "%d件".format(all_number)
                listData.addAll(datas)
                mAdapter!!.notifyDataSetChanged()
                condPage++
                isEndScroll = false // 成功以外は更新は止めるためにここだけにfalseを設定する
                hideListViewEmpty()
            },
            {
                // アラートの表示
                var msg: String? = null
                if (condPage == 1) {
                    showListViewEmpty("クチコミが見つかりませんでした")
                    if (it == Constants.HTTP_STATUS.network) {
                        msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                    }
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_NO_DATA,
                        title = "クチコミを取得できません",
                        msg = msg,
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
            })
    }
}
