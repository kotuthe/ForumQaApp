package net.tochinavi.www.tochinaviapp


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import kotlinx.android.synthetic.main.tab_item_ranking.view.*
import net.tochinavi.www.tochinaviapp.entities.DataRanking
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.value.MyIntent
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.view.ListRankingAdapter
import net.tochinavi.www.tochinaviapp.view.TouchListenerSetSpeed
import net.tochinavi.www.tochinaviapp.view.ViewAdvtFooter
import org.json.JSONObject



class FragmentRanking : Fragment() {

    companion object {
        val TAG = "Ranking"

        fun newInstance(): FragmentRanking {
            return FragmentRanking()
        }
    }

    // 数字はpossition
    enum class Type(val rawValue: Int)  {
        review(0),
        checkin(1),
    }

    private lateinit var firebase: FirebaseHelper

    private var listData: ArrayList<DataRanking> = ArrayList()
    private var mAdapter: BaseAdapter? = null
    // 保持データ
    private var arrayReview: ArrayList<DataRanking> = ArrayList()
    private var arrayCheckin: ArrayList<DataRanking> = ArrayList()

    private var selectType: Type = Type.review

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        // データの初期化
        selectType = Type.review

        // EmptyView
        showListViewEmpty(getString(R.string.loading_normal_message))

        // タブ　レイアウト
        val itemReview: View = createTabItem(R.drawable.ic_ranking_tab_review, getString(R.string.ranking_tag_review), false)
        tabLayout.getTabAt(0)!!.customView = itemReview

        val itemCheckin: View = createTabItem(R.drawable.ic_ranking_tab_checkin, getString(R.string.ranking_tag_checkin), true)
        tabLayout.getTabAt(1)!!.customView = itemCheckin

        tabLayout.clearOnTabSelectedListeners()
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    Type.checkin.rawValue -> {
                        selectType = Type.checkin
                    }
                    Type.review.rawValue -> {
                        selectType = Type.review
                    }
                }
                // リストの変更
                updateListView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // listViewの初期化
        mAdapter = ListRankingAdapter(context!!, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // ユーザー詳細へ
                startActivity(
                    MyIntent().web_browser(
                    MyString().my_http_url_member_detail(listData[pos].id)))
            }

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

                newGetData()

                // 数秒後に消す
                Handler().postDelayed({ refreshLayout.isRefreshing = false }, 1500)
            }
        }

        // 広告
        viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppRanking, resources)
    }

    override fun onResume() {
        super.onResume()
        // ランキング > Myページ > ランキング　は　update

        if (activity != null) {
            activity!!.title = getString(R.string.ranking_title)
        }

        if (arrayCheckin.isEmpty() && arrayReview.isEmpty()) {
            // データの更新
            newGetData()
        } else {
            // リスト表示
            updateListView()
        }
    }

    /**
     * タブのアイテムの作成
     */
    private fun createTabItem(icon: Int, title: String, separator: Boolean, titleSize: Float = 15f): View {
        val inflater = LayoutInflater.from(context!!)
        val itemView: View = inflater.inflate(R.layout.tab_item_ranking, null)
        itemView.tabIcon.setImageDrawable(ContextCompat.getDrawable(context!!, icon))
        itemView.tabText.text = title
        itemView.tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
        // itemView.viewSeparator.visibility = if (separator) View.VISIBLE else View.INVISIBLE
        return itemView
    }

    /**
     * ここではアイコンは表示しない
     */
    private fun showListViewEmpty(message: String) {
        layoutEmpty.visibility = View.VISIBLE
        layoutEmpty.imageViewIcon.visibility = View.GONE
        layoutEmpty.textViewMsg.text = message
    }

    private fun hideListViewEmpty() {
        layoutEmpty.visibility = View.GONE
    }

    private fun newGetData() {
        arrayCheckin.clear()
        arrayReview.clear()
        getData(1)
    }

    /**
     * リストの切り替え
     */
    private fun updateListView() {
        if (listView == null) return

        // Topへ
        listView.post(Runnable { listView.setSelection(0) })

        when (selectType) {
            Type.checkin -> {
                listData.clear()
                listData.addAll(arrayCheckin)
            }
            Type.review -> {
                listData.clear()
                listData.addAll(arrayReview)
            }
        }
        if (listData.size > 0) {
            showListViewEmpty("まだランキングはありません")
        } else {
            hideListViewEmpty()
        }

        // アダプター更新
        mAdapter!!.notifyDataSetChanged()
    }


    private fun getData(id: Int) {
        firebase.sendScreen(FirebaseHelper.screenName.Ranking,
            arrayListOf(Pair("type", id.toString())))

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to id) // 1 or 2

        val url = MyString().my_http_url_app() + "/ranking/get_ranking.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                var _listData: ArrayList<DataRanking> = ArrayList()
                if (result) {
                    val spot_array = datas.getJSONArray("user_array")
                    for (i in 0..spot_array.length() - 1) {
                        val obj = spot_array.getJSONObject(i)
                        _listData.add(DataRanking(
                            obj.getInt("rank"),
                            obj.getString("image"),
                            obj.getInt("id"),
                            obj.getString("name"),
                            obj.getString("info"),
                            obj.getString("description")
                        ))
                    }
                }

                when (id) {
                    1 -> {
                        // チェックイン
                        if (!_listData.isEmpty()) { arrayCheckin.addAll(_listData) }
                        getData(2)
                    }
                    2 -> {
                        // クチコミ
                        if (!_listData.isEmpty()) { arrayReview.addAll(_listData) }
                        // リストの更新
                        updateListView()
                    }
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
                // 空だけどリストの更新はする
                updateListView()
            })
        }
    }

}
