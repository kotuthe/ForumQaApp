package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_my_review_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tochinavi.www.tochinaviapp.entities.DataMyReview
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.MyIntent
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.ListMyReviewAdapter
import net.tochinavi.www.tochinaviapp.view.LoadingNormal
import net.tochinavi.www.tochinaviapp.view.TouchListenerSetSpeed
import org.json.JSONObject

class ActivityMyReviewList : AppCompatActivity() {

    companion object {
        val TAG = "ActivityMyReviewList"
        val TAG_SHORT = "MyReviewList"
    }

    // リクエスト
    private val REQUEST_INPUT_REVIEW: Int = 0x1

    // UI //
    private lateinit var loading: LoadingNormal

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
        setContentView(R.layout.activity_my_review_list)

        mContext = applicationContext
        mySP = MySharedPreferences(mContext)

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
        textViewParams.text = ""

        // listViewの初期化
        mAdapter = ListMyReviewAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val item = listData[pos]
                if (item.type == 1) {
                    // クチコミ詳細
                    val dataSpot = DataSpotInfo(
                        item.spotId,
                        1,
                        "",
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

        val url = MyString().my_http_url_app() + "/mypage/v2/get_review_list.php"
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
                if (datas.get("result") as Boolean) {
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

                        listData.add(DataMyReview(
                            obj.getInt("id"),
                            obj.getInt("type"),
                            obj.getInt("spot_id"),
                            obj.getString("spot_name"),
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
