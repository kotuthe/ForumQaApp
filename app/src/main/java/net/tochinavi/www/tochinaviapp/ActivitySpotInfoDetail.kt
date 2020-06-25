package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_spot_info_detail.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoDetail
import net.tochinavi.www.tochinaviapp.network.HttpSpotInfo
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.value.MyIntent
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.ListSpotInfoDetailAdapter


/// ここなんで表示されないか後で検証する
// データは取れていて一覧が出ない模様
class ActivitySpotInfoDetail :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotInfoDetail"
        val TAG_SHORT = "SpotInfoDetail"
    }

    // リクエスト //
    private val REQUEST_ALERT_PHONE: Int = 0x1
    private val REQUEST_ALERT_NO_DATA: Int = 0x2

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var mAdapter: BaseAdapter
    private lateinit var dataSpot: DataSpotInfo
    private var listData: ArrayList<DataSpotInfoDetail> = ArrayList()
    private var request_phone_number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_info_detail)

        mContext = applicationContext
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mAdapter = ListSpotInfoDetailAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                Log.i(">> basicAdapter", "position: $pos")
                val item = listData[pos]
                when (item.type) {
                    Constants.SPOT_BASIC_INFO_TYPE.address -> {
                        // 地図へ
                        val intent = Intent(this@ActivitySpotInfoDetail, ActivitySpotMap::class.java)
                        intent.putExtra("dataSpot", dataSpot)
                        startActivity(intent)
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                        // 電話
                        showPhoneAlert(item.value)
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.more_detail -> {
                        // 外部URLへ
                        startActivity(
                            MyIntent().web_browser(item.value))
                    }
                    else -> {

                    }
                }
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
            REQUEST_ALERT_PHONE -> {
                // 電話をする
                startActivity(MyIntent().phone(request_phone_number))
            }
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
     * 電話をしますかアラート
     */
    private fun showPhoneAlert(phone: String) {
        request_phone_number = phone
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PHONE,
            title = dataSpot.name,
            msg = "%sに電話をする".format(request_phone_number),
            positiveLabel = "電話をする",
            negativeLabel = "キャンセル"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    /**
     * スポットデータの取得
     */
    private fun getData() {
        HttpSpotInfo(mContext).get_spot_info_detail(
            dataSpot.id,
            {
                listData.addAll(it)
                mAdapter.notifyDataSetChanged()
            },
            {
                var msg: String? = null
                if (it == Constants.HTTP_STATUS.network) {
                    msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                }
                val alert = AlertNormal.newInstance(
                    requestCode = REQUEST_ALERT_NO_DATA,
                    title = "お店の情報を取得できません",
                    msg = msg,
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
    }
}
