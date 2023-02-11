package net.tttttt.www.forum_qa_app


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tab_item_main.view.*
import net.tttttt.www.forum_qa_app.FragmentMyPage
import net.tttttt.www.forum_qa_app.entities.DataAppData
import net.tttttt.www.forum_qa_app.entities.ServiceNearWishSpot
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableAppData
import net.tttttt.www.forum_qa_app.storage.DBTableNotificationNearWish
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.TabMainAdapter
import org.json.JSONObject


/*
ServiceNearWishSpotの停止とかがある
 */

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    // 変数 //
    private lateinit var mContext: Context
    private lateinit var mySP: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = applicationContext
        mySP = MySharedPreferences(mContext)

        startServices()
        initLayout()
    }

    private val mOnNavigationItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_top -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, FragmentTop())
                    .commit()
            }

            R.id.navigation_mypage -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, FragmentMyPage())
                    .commit()
            }

            R.id.navigation_topic -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, FragmentTopics())
                    .commit()
            }

            R.id.navigation_menu -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, FragmentSpotSearch())
                    .commit()
            }
        }
        true
    }


    private fun initLayout() {
        // ヘッダー
        // setActionBar(0)
        // setStatusBar(0)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(2).isEnabled = false

        bottomNavigationView.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
        floatingActionButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, FragmentPostQa())
                .commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, FragmentTop())
            .commit()

        // フッタータブ
        /*val adapter = TabMainAdapter(supportFragmentManager,this)
        viewPager.adapter = adapter
        viewPager.setPagingEnabled(false)
        viewPager.currentItem = Constants.TAB_ITEM.TOP.ordinal
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                setActionBar(position)
                setStatusBar(position)
            }
        })
        setTabLayout(viewPager)*/
    }

    /**
     * ActionBarのレイアウト
     */
    @SuppressLint("RestrictedApi")
    private fun setActionBar(position: Int) {
        if (supportActionBar == null) { return }
        supportActionBar!!.setShowHideAnimationEnabled(false)
        if (position == 0) {
            supportActionBar!!.hide()
        } else {
            supportActionBar!!.show()
        }
    }

    /** ステータスバーの色変更  */
    private fun setStatusBar(position: Int) {
        if (position == 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(
                applicationContext,
                R.color.colorTopStatus
            )
        } else {
            window.statusBarColor = ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimaryDark
            )
        }
    }

    /**
     * タブのレイアウト
     */
    /*private fun setTabLayout(viewPager: ViewPager) {
        tabLayout.setupWithViewPager(viewPager)

        // Top
        val itemTop: View = createTabItem(R.drawable.ic_tab_top, "写真")
        tabLayout.getTabAt(Constants.TAB_ITEM.TOP.ordinal)!!.customView = itemTop

        // 周辺
        val itemNeighbor: View = createTabItem(R.drawable.ic_tab_neighbor, "周辺")
        tabLayout.getTabAt(Constants.TAB_ITEM.NEIGHBOR.ordinal)!!.customView = itemNeighbor

        // カテゴリー
        val itemSearch: View = createTabItem(R.drawable.ic_tab_category, "カテゴリー", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.SEARCH.ordinal)!!.customView = itemSearch

        // ランキング
        val itemRanking: View = createTabItem(R.drawable.ic_tab_ranking, "ランキング", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.RANKING.ordinal)!!.customView = itemRanking

        // マイページ
        val itemMyPage: View = createTabItem(R.drawable.ic_tab_mypage, "Myページ", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.MY_PAGE.ordinal)!!.customView = itemMyPage

    }*/

    /**
     * タブのアイテムの作成
     */
    /*private fun createTabItem(icon: Int, title: String, titleSize: Float = 10f): View {
        val inflater = LayoutInflater.from(this)
        val itemView: View = inflater.inflate(R.layout.tab_item_main, null)
        itemView.tabIcon.setImageDrawable(ContextCompat.getDrawable(this, icon))
        itemView.tabText.setText(title)
        itemView.tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
        return itemView
    }*/

    /**
     * サービスの開始
     */
    private fun startServices() {

        updateWishData()

        // 周辺のスポットを開始
        val intent = Intent(application, ServiceNearWishSpot::class.java)
        stopService(intent)
        startService(intent)
    }

    /**
     * お気に入りの更新
     */
    private fun updateWishData() {
        // ログインID
        if (mySP.get_status_login()) {

            val params: ArrayList<Pair<String, Any>> = ArrayList()
            val db = DBHelper(mContext)
            try {
                val tableAppData = DBTableAppData(mContext)
                val tableUsers = DBTableUsers(mContext)

                ifNotNull(tableAppData.getData(db, DBTableAppData.Ids.wish_update), {
                    if (it.modified != null) {
                        params.add("modified_date" to it.modified!!.convertString())
                    }
                })
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }

            val url = MyString().my_http_url_app() + "/app_data/get_wish_data.php"
            url.httpGet(params).responseJson { request, response, result ->
                result.fold(success = { json ->
                    val datas = json.obj().get("datas") as JSONObject
                    if (datas.get("result") as Boolean) {
                        val modified_date = datas.getString("modified_date")
                        val wishlist = datas.getJSONArray("wishlist")

                        // データベースの初回値の設定
                        val db = DBHelper(this)
                        try {
                            db.beginTransaction()

                            // appと更新日とnotificationのスポットを登録
                            val cv = ContentValues()
                            cv.put(DataAppData.COL[1], modified_date)
                            DBTableAppData(mContext).update(
                                db, DataAppData(DBTableAppData.Ids.wish_update.rawValue, modified_date.convertDate()), "%s=%d".format(DataAppData.COL[0], DBTableAppData.Ids.wish_update.rawValue))
                            DBTableNotificationNearWish(mContext).setJsonData(db, wishlist)

                            db.setTransactionSuccessful()
                        } catch (e: Exception) {
                            Log.e(TAG, e.message.toString())
                        } finally {
                            db.endTransaction()
                            db.cleanup()
                        }
                    }

                }, failure = { error ->
                    // 通信エラー
                    Log.e(TAG, error.toString())
                })
            }
        }

    }



}
