package net.tttttt.www.forum_qa_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import net.tttttt.www.forum_qa_app.databinding.ActivityMyPageSettingBinding
import net.tttttt.www.forum_qa_app.entities.DataListSimple
import net.tttttt.www.forum_qa_app.entities.ServiceNearWishSpot
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.value.MyIntent
import net.tttttt.www.forum_qa_app.value.MySharedPreferences
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import net.tttttt.www.forum_qa_app.view.AlertActionSheet
import net.tttttt.www.forum_qa_app.view.ListSimpleAdapter


class ActivityMyPageSetting : AppCompatActivity(), AlertActionSheet.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivityMyPageSetting"
    }

    private lateinit var binding: ActivityMyPageSettingBinding

    private val REQUEST_LOGIN: Int = 0x1
    private val REQUEST_DEVICE_SETTING: Int = 0x2
    private val REQUEST_ALERT_LOGOUT: Int = 0x3

    private var mContext: Context? = null
    private lateinit var firebase: FirebaseHelper
    private lateinit var mySP: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_my_page_setting)
        binding = ActivityMyPageSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext!!)
        mySP = MySharedPreferences(mContext!!)

        firebase.sendScreen(FirebaseHelper.screenName.Mypage_Setting, null)

        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.my_page_setting_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // MyPage?????????????????????
            val intent = Intent()
            setResult(RESULT_OK, intent);
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            // ????????????
            REQUEST_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateStatusLogin()
                }
            }
            // ??????
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
        // Log.i(">> Permission", "req: $requestCode, per: $permissions, gra: $grantResults")
    }

    override fun onSimpleDialogNegativeClick(requestCode: Int) {
        // Log.i(">> alert", "Negative: " + requestCode)
    }

    override fun onSimpleDialogActionClick(requestCode: Int, index: Int) {
        when (requestCode) {
            // ?????????????????????
            REQUEST_ALERT_LOGOUT -> {

                firebase.sendEvent(
                    FirebaseHelper.screenName.Mypage_Setting,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "???????????????"
                )

                mySP.set_status_login(false)
                updateStatusLogin()

                // ?????????????????????????????????

                // ?????????????????????????????????
                val intent = Intent(applicationContext, ServiceNearWishSpot::class.java)
                stopService(intent)
            }
        }
    }

    /**
     * ???????????????????????????
     */
    private fun initLayout() {
        // ?????????????????????
        updateStatusLogin()
        updateStatusNotice()

        // WEB?????????
        val adapter = ListSimpleAdapter(this, 0)
        adapter.add(setListData("????????????WEB?????????"))
        adapter.add(setListData("??????????????????"))
        adapter.add(setListData("????????????"))
        adapter.add(setListData("??????????????????????????????"))
        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { parent, view, position, id ->

            ifNotNull(adapter.getItem(position), {
                firebase.sendEvent(
                    FirebaseHelper.screenName.Mypage_Setting,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    it.title
                )
            })

            var url = ""
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

        // ???????????????????????????
        binding.layoutNotice.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            /* // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intent) */
            startActivityForResult(intent, REQUEST_DEVICE_SETTING)
        }

        // ???????????? or ???????????????
        binding.layoutLogin.setOnClickListener {
            if (mySP.get_status_login()) {
                // ???????????????
                val alert = AlertActionSheet.newInstance(
                    requestCode = REQUEST_ALERT_LOGOUT,
                    title = null,
                    msg = "??????????????????????????????",
                    actionLabels = arrayOf("???????????????"),
                    negativeLabel = "???????????????"
                )
                alert.setActionDestructive(0)
                alert.show(supportFragmentManager, AlertActionSheet.TAG)
            } else {
                // ????????????
                firebase.sendEvent(
                    FirebaseHelper.screenName.Mypage_Setting,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "????????????"
                )
                val intent = Intent(this, ActivityLogin::class.java)
                intent.putExtra("tag", TAG)
                startActivityForResult(intent, REQUEST_LOGIN)
            }
        }
    }
    /**
     * WEB????????????????????????
     */
    private fun setListData(title: String) : DataListSimple {
        val data = DataListSimple(title, true)
        data.titleColor = ContextCompat.getColor(mContext!!, R.color.colorLinkBlue)
        data.titleSize = 14f
        return data
    }

    /**
     * ???????????????????????????
     */
    private fun updateStatusLogin() {
        var title: String = ""
        var color: Int = 0
        if (mySP.get_status_login()) {
            title = "???????????????"
            color = R.color.colorIosPink
        } else {
            title = "????????????"
            color = R.color.colorLinkBlue
        }
        binding.textViewStatusLogin.text = title
        binding.textViewStatusLogin.setTextColor(ContextCompat.getColor(mContext!!, color))
    }

    /**
     * ?????????????????????
     */
    private fun updateStatusNotice() {
        val manager = NotificationManagerCompat.from(mContext!!)
        val enabled = manager.areNotificationsEnabled()
        binding.textViewStatusNotice.text = if (enabled) "??????" else "??????"
    }
}
