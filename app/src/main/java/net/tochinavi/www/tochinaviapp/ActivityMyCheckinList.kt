package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_my_checkin_list.*
import net.tochinavi.www.tochinaviapp.entities.DataMySpotList
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


        if (supportActionBar != null) {
            supportActionBar!!.title = "チェックイン履歴"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mAdapter = ListMySpotAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // クチコミ詳細へ
                val item = listData[pos]
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

    /**
     * クチコミの取得
     */
    private fun getData() {
        Log.i(">> ${TAG_SHORT}", "onSearch")

        HttpMyPage(mContext).get_checkin_list(
            condPage,
            { datas, all_number ->
                // textViewNumber.text = "%d件".format(all_number)
                listData.addAll(datas)
                mAdapter.notifyDataSetChanged()
                condPage++
                isEndScroll = false
                // hideListViewEmpty()
            },
            {
                // アラートの表示
                var msg: String? = null
                if (condPage == 1) {
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