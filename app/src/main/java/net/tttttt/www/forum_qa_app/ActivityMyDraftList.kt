package net.tttttt.www.forum_qa_app

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
import net.tttttt.www.forum_qa_app.databinding.ActivityMyDraftListBinding
import net.tttttt.www.forum_qa_app.databinding.ListviewEmptyBinding
import net.tttttt.www.forum_qa_app.entities.DataMyDraftReview
import net.tttttt.www.forum_qa_app.entities.DataReviewTag
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.MySharedPreferences
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.ListMyDraftReviewAdapter
import net.tttttt.www.forum_qa_app.view.LoadingNormal
import net.tttttt.www.forum_qa_app.view.TouchListenerSetSpeed
import org.json.JSONObject

class ActivityMyDraftList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "MyDraftList"
    }

    private lateinit var binding: ActivityMyDraftListBinding
    private lateinit var bindingListEmpty: ListviewEmptyBinding

    // リクエスト
    private val REQUEST_INPUT_REVIEW: Int = 0x1
    private val REQUEST_ALERT_NO_DATA: Int = 0x2

    private lateinit var firebase: FirebaseHelper

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
        // setContentView(R.layout.activity_my_draft_list)
        binding = ActivityMyDraftListBinding.inflate(layoutInflater)
        // bindingListEmpty = ListviewEmptyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        mySP = MySharedPreferences(mContext)

        firebase.sendScreen(FirebaseHelper.screenName.Mypage_Draft_List, null)

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
        binding.textViewParams.text = ""

        // listViewの初期化
        mAdapter = ListMyDraftReviewAdapter(mContext, listData)
        binding.listView.apply {
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
        binding.refreshLayout.apply {
            setColorSchemeResources(R.color.colorIosBlue)

            setOnRefreshListener {
                doRefresh()
                // 数秒後に消す
                Handler().postDelayed({ binding.refreshLayout.isRefreshing = false }, 1500)
            }
        }
    }



    private fun showListViewEmpty(message: String) {
        // binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutEmpty.textViewMsg.text = message
    }

    private fun hideListViewEmpty() {
        // binding.layoutEmpty.visibility = View.GONE
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

        val url = MyString().my_http_url_app() + "/mypage/get_draft_review_list.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                if (loading.isVisible) {
                    loading.onDismiss(300)
                }

                if (condPage == 1 && listData.count() > 0) {
                    binding.listView.setSelection(0)
                    listData.clear()
                    mAdapter!!.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val count = datas.getInt("count")
                    binding.textViewParams.apply {
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
