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
import net.tttttt.www.forum_qa_app.databinding.ActivityMyReviewListBinding
import net.tttttt.www.forum_qa_app.entities.DataMyReview
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.MyIntent
import net.tttttt.www.forum_qa_app.value.MySharedPreferences
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import net.tttttt.www.forum_qa_app.view.ListMyReviewAdapter
import net.tttttt.www.forum_qa_app.view.LoadingNormal
import net.tttttt.www.forum_qa_app.view.TouchListenerSetSpeed
import org.json.JSONObject

class ActivityMyReviewList : AppCompatActivity() {

    companion object {
        val TAG = "ActivityMyReviewList"
        val TAG_SHORT = "MyReviewList"
    }

    private lateinit var binding: ActivityMyReviewListBinding

    // リクエスト
    private val REQUEST_INPUT_REVIEW: Int = 0x1

    // UI //
    private lateinit var loading: LoadingNormal

    private lateinit var firebase: FirebaseHelper

    // データ
    private lateinit var mContext: Context
    private lateinit var mySP: MySharedPreferences
    private var listData: ArrayList<DataMyReview> = ArrayList()
    private var mAdapter: BaseAdapter? = null
    private var isEndScroll: Boolean = false

    // 条件
    private var condPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyReviewListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        mySP = MySharedPreferences(mContext)

        firebase.sendScreen(FirebaseHelper.screenName.Mypage_Kuchikomi_List, null)

        if (supportActionBar != null) {
            supportActionBar!!.title = "クチコミ一覧"
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
     * UI設定
     */
    private fun initLayout() {
        loading = LoadingNormal.newInstance( message = "", isProgress = true )
        hideListViewEmpty()
        binding.textViewParams.text = ""

        // listViewの初期化
        mAdapter = ListMyReviewAdapter(mContext, listData)
        binding.listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val item = listData[pos]
                if (item.type == 1) {
                    // クチコミ詳細
                    val dataSpot = DataSpotInfo(
                        item.spotId,
                        1,
                        item.spotImage,
                        false,
                        item.spotName,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        0.0,
                        0.0,
                        0,
                        false,
                        false,
                        false,
                        false,
                        "",
                        "",
                        0,
                        false
                    )
                    val dataReview = DataSpotReview(
                        item.id,
                        item.spotId,
                        item.spotName,
                        item.userId,
                        item.userName,
                        item.userImage,
                        item.userInfo,
                        item.reviewDate,
                        item.review,
                        item.reviewImageUrls,
                        item.reviewUrl,
                        item.goodNum,
                        item.enableGood
                    )
                    val intent = Intent(this@ActivityMyReviewList, ActivitySpotReviewDetail::class.java)
                    intent.putExtra("dataSpot", dataSpot)
                    intent.putExtra("dataReview", dataReview)
                    startActivityForResult(intent, 0)
                } else {
                    // イベントやエリア等はWebへ
                    startActivity(
                        MyIntent().web_browser(
                            MyString().my_http_url_spot_review_detail(item.userId, item.id)))
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

        val url = MyString().my_http_url_app() + "/mypage/v2/get_review_list.php"
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
                if (datas.get("result") as Boolean) {
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

                        listData.add(DataMyReview(
                            obj.getInt("id"),
                            obj.getInt("type"),
                            obj.getInt("spot_id"),
                            obj.getString("spot_name"),
                            obj.getString("spot_image"),
                            obj.getInt("user_id"),
                            obj.getString("user_name"),
                            obj.getString("user_image"),
                            obj.getString("user_info"),
                            obj.getString("review_date"),
                            obj.getString("review"),
                            review_image_array,
                            "",
                            obj.getInt("good_num"),
                            obj.getBoolean("enable_good")
                        ))
                    }

                    mAdapter!!.notifyDataSetChanged()
                    condPage++
                    isEndScroll = false // 成功以外は更新は止めるためにここだけにfalseを設定する
                    hideListViewEmpty()
                } else {
                    // 検索結果なし
                    // ページ1の時はエラー文の表示する
                    if (condPage == 1) {
                        showListViewEmpty("対象のクチコミが見つかりませんでした")
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
                }
            })
        }
    }
}
