package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_my_checkin_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tochinavi.www.tochinaviapp.entities.DataMySpotList
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.network.HttpMyPage
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.ListMySpotAdapter
import net.tochinavi.www.tochinaviapp.view.TouchListenerSetSpeed

/*
  未実装なこと
  ・リフレッシュ
  ・パラメーター
  ・EmptyView
 */

class ActivityMyCheckinList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener  {

    companion object {
        val TAG = "ActivityMyCheckinList"
        val TAG_SHORT = "MyCheckinList"
    }

    private val REQUEST_ALERT_NO_DATA: Int = 0x1

    private lateinit var firebase: FirebaseHelper

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var mAdapter: BaseAdapter
    private var listData: ArrayList<DataMySpotList> = ArrayList()
    private var isEndScroll: Boolean = false

    // 条件
    private var condPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_checkin_list)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)

        firebase.sendScreen(FirebaseHelper.screenName.Mypage_Checkin_List, null)

        if (supportActionBar != null) {
            supportActionBar!!.title = "チェックイン履歴"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        textViewParams.text = ""
        hideListViewEmpty()

        mAdapter = ListMySpotAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // クチコミ詳細へ
                val item = listData[pos]
                firebase.sendSpotInfo(FirebaseHelper.screenName.Mypage_Checkin_List, item.type, item.id)
                if (item.type == 1) {
                    val intent = Intent(this@ActivityMyCheckinList, ActivitySpotInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@ActivityMyCheckinList, ActivityHospitalInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                }
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

        // リフレッシュ
        refreshLayout.apply {
            setColorSchemeResources(R.color.colorIosBlue)

            setOnRefreshListener {
                doRefresh()
                // 数秒後に消す
                Handler().postDelayed({ refreshLayout.isRefreshing = false }, 1500)
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
        layoutEmpty.visibility = View.VISIBLE
        layoutEmpty.textViewMsg.text = message
    }

    private fun hideListViewEmpty() {
        layoutEmpty.visibility = View.GONE
    }

    /**
     * リフレッシュ
     */
    private fun doRefresh() {
        isEndScroll = false
        condPage = 1

        getData()
    }

    /**
     * クチコミの取得
     */
    private fun getData() {
        HttpMyPage(mContext).get_checkin_list(
            condPage,
            { datas, all_number ->
                if (condPage == 1 && listData.count() > 0) {
                    listView.setSelection(0)
                    listData.clear()
                    mAdapter.notifyDataSetChanged()
                }
                textViewParams.text = "%d件".format(all_number)
                listData.addAll(datas)
                mAdapter.notifyDataSetChanged()
                condPage++
                isEndScroll = false
                hideListViewEmpty()
            },
            {
                // アラートの表示
                var msg: String? = null
                if (condPage == 1) {
                    showListViewEmpty("チェックイン履歴が見つかりませんでした")
                    if (it == Constants.HTTP_STATUS.network) {
                        msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                    }
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_NO_DATA,
                        title = "チェックイン履歴を取得できません",
                        msg = msg,
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
            })
    }
}
