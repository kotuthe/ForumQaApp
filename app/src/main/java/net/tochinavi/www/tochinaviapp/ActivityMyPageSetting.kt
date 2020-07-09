package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_page_setting.*
import net.tochinavi.www.tochinaviapp.entities.DataListSimple
import net.tochinavi.www.tochinaviapp.entities.ServiceNearWishSpot
import net.tochinavi.www.tochinaviapp.value.MyIntent
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.view.AlertActionSheet
import net.tochinavi.www.tochinaviapp.view.ListSimpleAdapter


class ActivityMyPageSetting : AppCompatActivity(), AlertActionSheet.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivityMyPageSetting"
    }

    private val REQUEST_LOGIN: Int = 0x1
    private val REQUEST_DEVICE_SETTING: Int = 0x2
    private val REQUEST_ALERT_LOGOUT: Int = 0x3

    private var mContext: Context? = null
    private var mySP: MySharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page_setting)

        mContext = applicationContext
        mySP = MySharedPreferences(mContext!!)

        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.my_page_setting_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // MyPageで更新するため
            val intent = Intent()
            setResult(RESULT_OK, intent);
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(">> Setting", "req: $requestCode, res: $resultCode")
        when(requestCode) {
            // ログイン
            REQUEST_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateStatusLogin()
                }
            }
            // 設定
            REQUEST_DEVICE_SETTING -> {
                updateStatusNotice()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(">> Permission", "req: $requestCode, per: $permissions, gra: $grantResults")
    }

    override fun onSimpleDialogNegativeClick(requestCode: Int) {
        // Log.i(">> alert", "Negative: " + requestCode)
    }

    override fun onSimpleDialogActionClick(requestCode: Int, index: Int) {
        when (requestCode) {
            // ログアウト処理
            REQUEST_ALERT_LOGOUT -> {
                // ※　Firebase
                mySP!!.set_status_login(false)
                updateStatusLogin()

                // 周辺スポット通知の停止

                // 周辺スポット通知の停止
                val intent = Intent(applicationContext, ServiceNearWishSpot::class.java)
                stopService(intent)
            }
        }
    }

    /**
     * 初回レイアウト設定
     */
    private fun initLayout() {
        // ステータス更新
        updateStatusLogin()
        updateStatusNotice()

        // WEBリンク
        val adapter = ListSimpleAdapter(this, 0)
        adapter.add(setListData("栃ナビ！WEBサイト"))
        adapter.add(setListData("使い方の説明"))
        adapter.add(setListData("運営会社"))
        adapter.add(setListData("プライバシーポリシー"))
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
            var url: String = ""
            when(position) {
                0 -> {
                    url = MyString().my_http_url_partner_top_web()
                }
                1 -> {
                    url = MyString().my_http_url_how_to_app()
                }
                2 -> {
                    url = MyString().my_http_url_company_top()
                }
                3 -> {
                    url = MyString().my_http_url_privacy_policy()
                }
            }

            startActivity(
                MyIntent().web_browser(url))
        }

        // アプリ通知の設定へ
        layoutNotice.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            /* // この設定をするとアプリとは別タスクになるため、戻った時のイベントを受け取れない
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intent) */
            startActivityForResult(intent, REQUEST_DEVICE_SETTING)
        }

        // ログイン or ログアウト
        layoutLogin.setOnClickListener {
            if (mySP!!.get_status_login()) {
                // ログアウト
                val alert = AlertActionSheet.newInstance(
                    requestCode = REQUEST_ALERT_LOGOUT,
                    title = null,
                    msg = "ログアウトしますか？",
                    actionLabels = arrayOf("ログアウト"),
                    negativeLabel = "キャンセル"
                )
                alert.setActionDestructive(0)
                alert.show(supportFragmentManager, AlertActionSheet.TAG)
            } else {
                // ログイン
                val intent = Intent(this, ActivityLogin::class.java)
                intent.putExtra("tag", TAG)
                startActivityForResult(intent, REQUEST_LOGIN)
            }
        }
    }
    /**
     * WEBリンクのデザイン
     */
    private fun setListData(title: String) : DataListSimple {
        val data = DataListSimple(title, true)
        data.titleColor = ContextCompat.getColor(mContext!!, R.color.colorLinkBlue)
        data.titleSize = 14f
        return data
    }

    /**
     * ログインステータス
     */
    private fun updateStatusLogin() {
        var title: String = ""
        var color: Int = 0
        if (mySP!!.get_status_login()) {
            title = "ログアウト"
            color = R.color.colorIosPink
        } else {
            title = "ログイン"
            color = R.color.colorLinkBlue
        }
        textViewStatusLogin.text = title
        textViewStatusLogin.setTextColor(ContextCompat.getColor(mContext!!, color))
    }

    /**
     * 通知ステータス
     */
    private fun updateStatusNotice() {
        val manager = NotificationManagerCompat.from(mContext!!)
        val enabled = manager.areNotificationsEnabled()
        textViewStatusNotice.text = if (enabled) "オン" else "オフ"
    }
}
