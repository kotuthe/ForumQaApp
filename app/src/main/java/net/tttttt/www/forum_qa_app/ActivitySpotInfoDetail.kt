package net.tttttt.www.forum_qa_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotInfoDetailBinding
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotInfoDetail
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.network.HttpSpotInfo
import net.tttttt.www.forum_qa_app.value.Constants
import net.tttttt.www.forum_qa_app.value.MyIntent
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.ListSpotInfoDetailAdapter


/// ここなんで表示されないか後で検証する
// データは取れていて一覧が出ない模様
class ActivitySpotInfoDetail :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotInfoDetail"
        val TAG_SHORT = "SpotInfoDetail"
    }

    private lateinit var binding: ActivitySpotInfoDetailBinding

    // リクエスト //
    private val REQUEST_ALERT_PHONE: Int = 0x1
    private val REQUEST_ALERT_NO_DATA: Int = 0x2

    private lateinit var firebase: FirebaseHelper

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var mAdapter: BaseAdapter
    private lateinit var dataSpot: DataSpotInfo
    private var listData: ArrayList<DataSpotInfoDetail> = ArrayList()
    private var request_phone_number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpotInfoDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo

        firebase.sendScreen(
            FirebaseHelper.screenName.Spot_Info_Detail, arrayListOf(Pair("id", dataSpot.id.toString())))

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mAdapter = ListSpotInfoDetailAdapter(mContext, listData, true)
        binding.listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
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
