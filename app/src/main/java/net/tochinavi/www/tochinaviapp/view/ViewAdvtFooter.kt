package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import coil.api.load
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.view_advt_footer.view.*
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import org.json.JSONObject
import java.util.*


class ViewAdvtFooter : FrameLayout {

    /** コンストラクタ **/
    constructor(context: Context): super(context) {}

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle) {}

    companion object {
        const val TAG = "ViewAdvtFooter"
        const val TAG_SHORT = "AdvtFooter"
    }

    private val INVISIBLE_TIME = 1 // 非表示の期間（単位：日）
    private var mySP: MySharedPreferences? = null

    enum class screenName {
        AppTopPage,
        AppNeighbor,
        AppSearch,
        AppSearchList,
        AppRanking,
        AppMyPage,
        AppSpotInfo
    }

    // コアなデータ //
    private var mContext: Context? = null

    // データ //
    private var targetScreenName: screenName? = null
    private var advtUrl: String? = null

    /**
     * CreateView的な
     */
    private fun initView() {
        mContext = context
        mySP = MySharedPreferences(mContext!!)

        inflate(mContext!!, R.layout.view_advt_footer, this)

        // 広告画像
        imageView.setOnClickListener {
            if (advtUrl != null && !advtUrl!!.isEmpty()) {
                val uri = Uri.parse(advtUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                mContext!!.startActivity(intent)
            }
        }

        // 閉じるボタン
        layoutClose.setOnClickListener {
            mySP!!.put_advt(targetScreenName!!.name)
            hideCurrentView()
        }
    }

    /**
     * ViewAdvtFooter自体を消す
     */
    private fun hideCurrentView() {
        this.removeAllViews()
        this.visibility = View.GONE
    }

    /**
     * 表示判定
     * @return
     */
    private fun getVisible(): Boolean {
        val time = mySP!!.get_advt(targetScreenName!!.name)
        if (time == null) return false
        val now = Date(System.currentTimeMillis())
        val oneDateTime = 1000 * 60 * 60 * 24.toLong()
        val diff = (now.time - time) / oneDateTime
        return if (diff > INVISIBLE_TIME) true else false
    }

    /**
     * 広告の設定
     * @param screen
     * @param resources
     */
    fun setAdvt(screen: screenName, resources: Resources?) {
        targetScreenName = screen

        if (getVisible()) {
            getData()
        } else {
            hideCurrentView()
        }
    }

    /**
     * 広告データの取得
     */
    private fun getData() {
        val url = MyString().my_http_url_app() + "/advt/get_advt.php"
        val params = listOf("screen_name" to targetScreenName!!.name)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    imageView.visibility = View.VISIBLE
                    layoutClose.visibility = View.VISIBLE

                    imageView.load(datas.getString("img")) {
                        placeholder(R.drawable.ic_image_placeholder)
                    }
                    advtUrl = datas.getString("url")
                } else {
                    hideCurrentView()
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                hideCurrentView()
            })
        }
    }
}