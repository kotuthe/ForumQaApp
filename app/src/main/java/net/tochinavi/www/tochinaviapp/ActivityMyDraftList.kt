package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_my_draft_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tochinavi.www.tochinaviapp.entities.DataMyDraftReview
import net.tochinavi.www.tochinaviapp.entities.DataReviewTag
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.ListMyDraftReviewAdapter
import net.tochinavi.www.tochinaviapp.view.LoadingNormal
import net.tochinavi.www.tochinaviapp.view.TouchListenerSetSpeed
import org.json.JSONObject

class ActivityMyDraftList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "MyDraftList"
    }

    // リクエスト
    private val REQUEST_INPUT_REVIEW: Int = 0x1
    private val REQUEST_ALERT_NO_DATA: Int = 0x2

    // UI //
    private lateinit var loading: LoadingNormal

    // データ
    private lateinit var mContext: Context
    private lateinit var mySP: MySharedPreferences
    private var listData: ArrayList<DataMyDraftReview> = ArrayList()
    private var mAdapter: BaseAdapter? = null
    private var isEndScroll: Boolean = false

    // 条件
    private var condPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_draft_list)

        mContext = applicationContext
        mySP = MySharedPreferences(mContext)

        if (supportActionBar != null) {
            supportActionBar!!.title = "クチコミ下書き"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
        getData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_INPUT_REVIEW -> {
                // 下書き更新後
                if (resultCode == Activity.RESULT_OK) {
                    doRefresh()
                }
            }
        }
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
     * UI設定
     */
    private fun initLayout() {
        loading = LoadingNormal.newInstance( message = "", isProgress = true )
        hideListViewEmpty()
        textViewParams.text = ""

        // listViewの初期化
        mAdapter = ListMyDraftReviewAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // 下書き入力へ
                val intent = Intent(this@ActivityMyDraftList, ActivityInputReview::class.java)
                intent.putExtra("isDraft", true)
                intent.putExtra("dataReview", listData[pos])
                startActivityForResult(intent, REQUEST_INPUT_REVIEW)
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
     * お店の検索
     */
    private fun getData() {
        Log.i(">> ${FragmentTop.TAG}", "onSearch")

        if (condPage == 1) {
            loading.show(supportFragmentManager, LoadingNormal.TAG)
            loading.updateLayout(getString(R.string.loading_normal_message), true)
        }

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("page" to condPage)
        params.add("get_count" to true)

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        println(params)

        val url = MyString().my_http_url_app() + "/mypage/get_draft_review_list.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                if (loading.isVisible) {
                    loading.onDismiss(300)
                }

                if (condPage == 1 && listData.count() > 0) {
                    listView.setSelection(0)
                    listData.clear()
                    mAdapter!!.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val count = datas.getInt("count")
                    textViewParams.apply {
                        if (count > 0) {
                            text = "%d件".format(count)
                        } else {
                            visibility = View.GONE
                        }
                    }
                    val review_array = datas.getJSONArray("review")
                    for (i in 0..review_array.length() - 1) {
                        val obj = review_array.getJSONObject(i)

                        // 写真
                        val js_review_images = obj.getJSONArray("review_images")
                        val review_image_array: ArrayList<String> = arrayListOf()
                        if (js_review_images.length() > 0) {
                            for (j in 0..js_review_images.length() - 1) {
                                review_image_array.add(js_review_images.getString(j))
                            }
                        }

                        // 日付
                        val js_action_date = obj.getJSONArray("action_date")
                        val action_date: Array<Int> = arrayOf(
                            js_action_date[0] as Int,
                            js_action_date[1] as Int,
                            js_action_date[2] as Int)

                        // タグ
                        val js_tag_ids = obj.getJSONArray("tag_ids")
                        val tag_ids: ArrayList<DataReviewTag> = arrayListOf()
                        for (j in 0..js_tag_ids.length() - 1) {
                            val obj_item = js_tag_ids.getJSONObject(j)
                            tag_ids.add(DataReviewTag(
                                obj_item.getInt("id"), obj_item.getString("name"), true))
                        }

                        listData.add(DataMyDraftReview(
                            obj.getInt("review_id"),
                            true,
                            obj.getInt("spot_id"),
                            obj.getString("spot_name"),
                            obj.getInt("type"),
                            obj.getString("review_date"),
                            obj.getString("review"),
                            review_image_array,
                            0,
                            obj.getBoolean("review_photo_flag"),
                            action_date,
                            tag_ids
                        ))
                    }

                    mAdapter!!.notifyDataSetChanged()
                    condPage++
                    isEndScroll = false // 成功以外は更新は止めるためにここだけにfalseを設定する
                    hideListViewEmpty()
                } else {
                    // 検索結果なし
                    if (condPage == 1) {
                        showListViewEmpty("下書きを取得できませんでした")
                        showAlertNoData("下書きを取得できませんでした")
                    }
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
                if (loading.isVisible) {
                    loading.onDismiss(300)
                }
                if (condPage == 1) {
                    showListViewEmpty("通信エラー")
                    showAlertNoData("通信エラー", "時間を置いて再度表示してください")
                }
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
