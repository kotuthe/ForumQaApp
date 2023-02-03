package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_my_badge_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tttttt.www.forum_qa_app.entities.DataBadge
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.MySharedPreferences
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.RecyclerMyBadgeAdapter
import org.json.JSONObject

class ActivityMyBadgeList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener  {

    companion object {
        val TAG = "ActivityMyBadgeList"
        val TAG_SHORT = "MyBadgeList"
    }

    // リクエスト //
    private val REQUEST_ALERT_NO_DATA: Int = 0x1

    // データ //
    private lateinit var firebase: FirebaseHelper
    private lateinit var mySP: MySharedPreferences
    private lateinit var mAdapter: RecyclerMyBadgeAdapter
    private var listData: ArrayList<DataBadge> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_badge_list)

        if (supportActionBar != null) {
            supportActionBar!!.title = "称号一覧"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        firebase = FirebaseHelper(this)
        mySP = MySharedPreferences(this)
        mAdapter = RecyclerMyBadgeAdapter(this, listData)

        firebase.sendScreen(FirebaseHelper.screenName.Mypage_Badge_List, null)

        hideListViewEmpty()

        recyclerView.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 画像の表示がかぶるため以下の対策
            // https://teratail.com/questions/54616
            recycledViewPool.setMaxRecycledViews(1, 0)

            // 列
            val lmg = GridLayoutManager(context, mAdapter.spanCount)
            lmg.spanSizeLookup = mAdapter.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(mAdapter.mItemDecoration)
            adapter = mAdapter
        }
        mAdapter.setOnItemClickListener(View.OnClickListener { view ->
            // 詳細へ
            val index = view.id
            val item = listData[index]
            if (item.getFlag) {
                val intent = Intent(this, ActivityMyBadgeDetail::class.java)
                intent.putExtra("data", item)
                startActivity(intent)
            }
        })

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

    //get_badge_list
    private fun getData() {
        // ※Firebase
        val params: ArrayList<Pair<String, Any>> = ArrayList()

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(this)
            try {
                val tableUsers = DBTableUsers(this)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }
        val url = MyString().my_http_url_app() + "/mypage/v2/get_badge_list.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val number = datas.getInt("number")
                    val badge_array = datas.getJSONArray("badge_array")
                    for (i in 0..badge_array.length() - 1) {
                        val obj = badge_array.getJSONObject(i)
                        listData.add(
                            DataBadge(
                                obj.getInt("id"),
                                obj.getString("name"),
                                obj.getString("image"),
                                obj.getString("description"),
                                obj.getBoolean("get_flag")
                            ))
                    }
                    mAdapter.notifyDataSetChanged()
                    textViewParams.apply {
                        if (number > 0) {
                            text = "%d件".format(number)
                        } else {
                            text = "チェックインをして称号をゲットしよう！"
                        }
                    }
                } else {
                    textViewParams.text = "称号を取得できませんでした"
                    showAlertNoData("称号を取得できませんでした")
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG_SHORT, error.toString())
                showAlertNoData("通信エラー", "時間を置いて再度表示してください")
            })
        }
    }

    private fun showAlertNoData(title: String, message: String? = null) {
        showListViewEmpty(title)
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_NO_DATA,
            title = title,
            msg = message,
            positiveLabel = "OK",
            negativeLabel = null
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }




}
