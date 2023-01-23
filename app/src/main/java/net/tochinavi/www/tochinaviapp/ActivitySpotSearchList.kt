package net.tochinavi.www.tochinaviapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.fragment_spot_neighbor_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tochinavi.www.tochinaviapp.entities.*
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.storage.*
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.*
import org.json.JSONObject

class ActivitySpotSearchList : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotSearchList"
        val TAG_SHORT = "SpotSearchList"
    }

    // リクエスト
    private val REQUEST_NARROW: Int = 0x1

    private lateinit var firebase: FirebaseHelper

    // UI //
    private lateinit var loading: LoadingNormal

    // 変数 //
    private lateinit var mySP: MySharedPreferences
    private lateinit var mContext: Context
    private lateinit var mAdapter: BaseAdapter
    private var listData: ArrayList<DataSpotList> = ArrayList()
    private var isEndScroll: Boolean = false

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null

    // 条件
    private var condPage: Int = 1
    private var condWord: String = ""
    // Pair < first: level, second: id >
    private var condCategoryArray: ArrayList<Pair<Int, Int>> = arrayListOf()
    private var condAreaArray: ArrayList<Int> = arrayListOf()
    private var condCoupon: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_search_list)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        mySP = MySharedPreferences(mContext)
        condWord = intent.getStringExtra("word").toString()

        ifNotNull(intent.getIntExtra("category", 0), {
            if (it > 0) {
                // 第1カテゴリー
                condCategoryArray.add(Pair(1, it))
            }
        })

        if (supportActionBar != null) {
            supportActionBar!!.title = "検索結果"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // ローディング
        loading = LoadingNormal.newInstance(
            message = getString(R.string.loading_normal_message),
            isProgress = true
        )
        loading.show(supportFragmentManager, LoadingNormal.TAG)

        // listViewの初期化
        mAdapter = ListSpotNeighborAdapter(mContext, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // スポット情報へ
                val item = listData[pos]
                firebase.sendSpotInfo(FirebaseHelper.screenName.Search_List, item.type, item.id)
                if (item.type == 1) {
                    val intent = Intent(this@ActivitySpotSearchList, ActivitySpotInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@ActivitySpotSearchList, ActivityHospitalInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
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
                            onSearch()
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
                isEndScroll = false
                condPage = 1
                mLocation = null

                loading.show(supportFragmentManager, LoadingNormal.TAG)
                loading.updateLayout(getString(R.string.loading_normal_message), true)

                // 位置情報を更新してから検索
                setLocation()

                // 数秒後に消す
                Handler().postDelayed({ refreshLayout.isRefreshing = false }, 1500)
            }
        }

        setTextViewParams()

        // 広告
        viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppSearchList, resources)
    }

    override fun onPause() {
        super.onPause()

        // getLocationHighQualityの取得に時間がかかるため
        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback!!)
        }
    }

    override fun onResume() {
        super.onResume()
        setLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_narrow, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.search_narrow -> {
                // 絞り込みへ
                val intent = Intent(this@ActivitySpotSearchList, ActivitySpotSearchNarrow::class.java)
                intent.putExtra("word", condWord)
                intent.putExtra("category", condCategoryArray)
                intent.putExtra("area", condAreaArray)
                intent.putExtra("coupon", condCoupon)
                startActivityForResult(intent, REQUEST_NARROW)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_NARROW -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    condWord = data.getStringExtra("word")!!
                    condCategoryArray = data.getSerializableExtra("category") as ArrayList<Pair<Int, Int>>
                    condAreaArray = data.getIntegerArrayListExtra("area")!!
                    condCoupon = data.getBooleanExtra("coupon", false)

                    // データの初期化
                    isEndScroll = false
                    condPage = 1
                    mLocation = null
                    if (listData.size > 0) {
                        listView.setSelection(0)
                    }
                    setTextViewParams()
                    // このあと onResumeへ
                }
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

    private fun setTextViewParams() {

        val cond_array: ArrayList<String> = ArrayList()
        if (!condWord.isEmpty()) {
            cond_array.add(condWord)
        }

        // item first
        var category: String? = null
        var area: String? = null
        val db = DBHelper(mContext)
        try {
            // カテゴリー
            if (condCategoryArray.size > 0) {
                var targetIndex = 0
                var otherNum = 0

                if (condCategoryArray.size > 2) {
                    targetIndex = (1..(condCategoryArray.size - 1)).random()
                    otherNum = condCategoryArray.size - 2
                } else if (condCategoryArray.size > 1) {
                    targetIndex = 1
                }

                val id = condCategoryArray[targetIndex].second
                when (condCategoryArray[targetIndex].first) {
                    1 -> {
                        val data1: DataCategory1 =
                            DBTableCategory1(mContext).getData(db, id)
                        category = data1.name
                    }
                    2 -> {
                        val data2: DataCategory2 =
                            DBTableCategory2(mContext).getData(db, id)
                        category = data2.name
                    }
                    3 -> {
                        val data3: DataCategory3 =
                            DBTableCategory3(mContext).getData(db, id)
                        category = data3.name
                    }
                    else -> {
                    }
                }
                if (otherNum > 0) {
                    category = "$category..."
                }
            }

            // エリア
            if (condAreaArray.size > 0) {
                val data: DataArea2 =
                    DBTableArea2(mContext).getData(db, condAreaArray[0])
                area = data.name
                if (condAreaArray.size > 1) {
                    area = "$area..."
                }
            }

        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        if (category != null) {
            cond_array.add(category)
        }
        if (area != null) {
            cond_array.add(area)
        }

        textViewParams.text =
            if (cond_array.size > 0) cond_array.joinToString("/") else ""

    }

    /** 位置情報取得 -> 周辺検索への流れ　（周辺検索する場合はこの関数を読めばOK） **/
    private fun setLocation() {
        hideListViewEmpty()
        // 端末の位置情報サービスをチェック
        val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // パーミッションのチェックが必要か確認
            if (Build.VERSION.SDK_INT >= 23) {
                // Android6以上
                checkPermission()
            } else {
                // それ以外
                getLocation()
            }
        } else {
            errorLocation()
        }
    }

    /**
     * Permissionのチェック
     */
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Log.i(">> $TAG", "checkPermission ok")
            // アプリの権限が許可してる
            getLocation()
        } else {
            // 許可してない
            errorLocation()
        }
    }

    /**
     * 位置情報を取得　Permissionで許可された時
     */
    private fun getLocation() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationClient!!.lastLocation.addOnCompleteListener(
            this,
            OnCompleteListener<Location?> { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        mLocation = task.result
                        // この後周辺検索へ
                        onSearch()
                    } else {
                        // last location is null
                        getLocationHighQuality()
                    }
                } else {
                    errorLocation()
                }
            })
    }

    /**
     * getLocationで位置取得できない時、さらに位置情報取得
     * ※位置情報サービスOFF -> ON に変更した時になる
     */
    private fun getLocationHighQuality() {
        // 5秒ほど時間がかかるため
        loading.updateLayout(getString(R.string.loading_normal_message) + "\nしばらくお待ちください...", true)

        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 500
        request.fastestInterval = 300

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                mLocation = result.lastLocation
                // 現在地だけ欲しいので、1回取得したらすぐに外す
                mLocationClient!!.removeLocationUpdates(this)
                // この後周辺検索へ
                onSearch()
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationClient!!.requestLocationUpdates(request, mLocationCallback!!, null)
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {
        if (!(mySP.get(MySharedPreferences.Keys.spot_search_location_first_alert) as Boolean)) {
            // アラート（1回のみ）
            mySP.put(MySharedPreferences.Keys.spot_search_location_first_alert, true)
            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "現在地を取得できませんでした",
                msg = "周辺のお店を検索するには位置情報サービスをONにしてください",
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)

            loading.onDismiss()
        } else {
            // ローディング
            loading.updateLayout("現在地を取得できませんでした", true)
            loading.onDismiss(1000)
        }

        if (listData.size > 0) {
            listData.clear()
            mAdapter.notifyDataSetChanged()
            condPage = 1 // 次の検索のため
        }
        showListViewEmpty("現在地を取得できませんでした。\n周辺のお店を検索するには位置情報サービスをONにしてください。")
    }

    /**
     * お店の検索
     */
    private fun onSearch() {
        if (mLocation == null) {
            // 周辺検索はできないよ (多分ここにはこないと思う)
            errorLocation()
            return
        }

        loading.updateLayout(getString(R.string.loading_normal_message), true)

        // ※後でFirebase
        val params: ArrayList<Pair<String, Any>> = ArrayList()
        val fParams: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("page" to condPage)
        fParams.add("page" to condPage.toString())

        // 周辺検索（エラー判定済み）
        params.add("latitude" to mLocation!!.latitude)
        params.add("longitude" to mLocation!!.longitude)
        fParams.add("lat_lon" to "%f,%f".format(mLocation!!.latitude, mLocation!!.longitude))

        if (!condWord.isEmpty()) {
            params.add("cond_word" to condWord)
            fParams.add("cond_word" to condWord)
        }

        // カテゴリー
        if (condCategoryArray.size > 0) {
            if (condCategoryArray.size > 1) {
                // 第２、３
                val tmpFParams: ArrayList<String> = arrayListOf()
                for (i in 1..condCategoryArray.size - 1) {
                    val item = condCategoryArray[i]
                    params.add("cond_category[]" to item.second)
                    tmpFParams.add("ca%d:%d".format(item.first, item.second))
                }
                fParams.add("cond_category" to tmpFParams.joinToString(","))
            } else {
                // 第１
                params.add("cond_category[]" to condCategoryArray[0].second)
                fParams.add("cond_category" to "ca1:%d".format(condCategoryArray[0].second))
            }
        }

        // エリアID
        if (condAreaArray.size > 0) {
            val tmpFParams: ArrayList<Int> = arrayListOf()
            for (i in 0..condAreaArray.size - 1) {
                params.add("cond_area[]" to condAreaArray[i])
                tmpFParams.add(condAreaArray[i])
            }
            fParams.add("cond_area" to tmpFParams.joinToString(","))
        }

        // クーポン
        if (condCoupon) {
            params.add("cond_coupon" to condCoupon)
            fParams.add("cond_coupon" to if (condCoupon) "1" else "0")
        }

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                    fParams.add("user_id" to it.user_id.toString())
                })
            } catch (e: Exception) {
                Log.e(TAG_SHORT, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        firebase.sendScreen(FirebaseHelper.screenName.Search_List, fParams)

        val url = MyString().my_http_url_app() + "/spot/search.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                loading.onDismiss(300)

                if (condPage == 1 && listData.count() > 0) {
                    listView.setSelection(0)
                    listData.clear()
                    mAdapter.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val spot_array = datas.getJSONArray("spot_array")
                    for (i in 0..spot_array.length() - 1) {
                        val obj = spot_array.getJSONObject(i)
                        listData.add(
                            DataSpotList(
                                obj.getInt("id"),
                                obj.getInt("type"),
                                obj.getString("name"),
                                obj.getString("address"),
                                obj.getInt("parent_category_id"),
                                obj.getString("category"),
                                obj.getString("distance"),
                                obj.getInt("review_num"),
                                obj.getString("image"),
                                obj.getBoolean("checkin_enable"),
                                obj.getBoolean("coupon_enable"),
                                0,
                                0,
                                arrayListOf(),
                                0.0,
                                0.0
                            )
                        )
                    }

                    mAdapter.notifyDataSetChanged()
                    condPage++
                    isEndScroll = false // 成功以外は更新は止めるためにここだけにfalseを設定する
                    hideListViewEmpty()
                } else {
                    // 検索結果なし
                    // ページ1の時はエラー文の表示する
                    if (condPage == 1) {
                        showListViewEmpty("対象のスポットが見つかりませんでした")
                    }
                }
            }, failure = { error ->
                // 通信エラー
                loading.onDismiss(300)
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }

}
