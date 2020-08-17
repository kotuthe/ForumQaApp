package net.tochinavi.www.tochinaviapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_spot_review_image_list.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.RecyclerInfiniteScrollListener
import net.tochinavi.www.tochinaviapp.view.RecyclerReviewImagesAdapter
import org.json.JSONObject

class ActivitySpotReviewImageList :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener  {

    companion object {
        val TAG = "ActivitySpotReviewImageList"
        val TAG_SHORT = "ReviewImageList"
    }

    // リクエスト //
    private val REQUEST_ALERT_NO_DATA: Int = 0x1

    // データ //
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var mAdapter: RecyclerReviewImagesAdapter
    private var listData: ArrayList<DataSpotReview> = ArrayList()

    // 検索条件
    private var condPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_review_image_list)

        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mAdapter = RecyclerReviewImagesAdapter(this, listData)
        recyclerView.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 列
            val lmg = GridLayoutManager(context, mAdapter.spanCount)
            lmg.spanSizeLookup = mAdapter.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(mAdapter.mItemDecoration)
            adapter = mAdapter

            // イベント //
            clearOnScrollListeners()
            addOnScrollListener(RecyclerInfiniteScrollListener(lmg) {
                // 続きを検索
                getData()
            })
        }
        mAdapter.setOnItemClickListener(View.OnClickListener { view ->
            // ※ダブルクリック禁止を実装(後で考える)
            val index = view.id
            val item: DataSpotReview = listData[index]

            val intent = Intent(this, ActivitySpotReviewImage::class.java)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("dataReview", item)
            startActivity(intent)

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
        /*Log.i(
            ">> ${TAG_SHORT}",
            "onSimpleDialogActionClick requestCode: $requestCode"
        )*/
    }

    /**
     * アラート　ネガティブ
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    private fun getData() {

        // ※Firebase
        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to dataSpot.id)
        params.add("page" to condPage)

        val url = MyString().my_http_url_app() + "/spot/get_spot_review_images.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                /*if (loading != null) {
                    loading!!.onDismiss(300)
                }*/

                if (condPage == 1 && listData.count() > 0) {
                    recyclerView.smoothScrollToPosition(0)
                    listData.clear()
                    mAdapter.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val review_array = datas.getJSONArray("review")
                    for (i in 0..review_array.length() - 1) {
                        val obj = review_array.getJSONObject(i)
                        val user_enable = obj.getBoolean("user_enable")
                        val review_images = obj.getJSONArray("review_images")
                        val review_image_array: ArrayList<String> = ArrayList()
                        if (review_images.length() > 0) {
                            for (j in 0..review_images.length() - 1) {
                                review_image_array.add(review_images.getString(j))
                            }
                        }

                        if (!user_enable) {
                            // 店舗画像
                            listData.add(
                                DataSpotReview(
                                    obj.getInt("review_id"),
                                    dataSpot.id,
                                    dataSpot.name,
                                    0,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    review_image_array,
                                    "",
                                    0,
                                    false
                                )
                            )
                        } else {
                            // クチコミ画像
                            listData.add(
                                DataSpotReview(
                                    obj.getInt("review_id"),
                                    dataSpot.id,
                                    dataSpot.name,
                                    obj.getInt("user_id"),
                                    obj.getString("user_name"),
                                    obj.getString("user_icon"),
                                    obj.getString("user_detail"),
                                    "",
                                    obj.getString("user_review"),
                                    review_image_array,
                                    "",
                                    obj.getInt("good_num"),
                                    obj.getBoolean("enable_good")
                                )
                            )
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                    condPage++
                } else {
                    if (condPage == 1) {
                        showAlertNoData()
                    } else {}
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG_SHORT, error.toString())
                if (condPage == 1) {
                    showAlertNoData()
                }
            })
        }
    }

    private fun showAlertNoData() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_NO_DATA,
            title = "クチコミが見つかりませんでした",
            msg = "時間を置いて再度表示してください",
            positiveLabel = "OK",
            negativeLabel = null
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }


}
