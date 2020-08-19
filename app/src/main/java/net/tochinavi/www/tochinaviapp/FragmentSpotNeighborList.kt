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
import android.view.*
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.fragment_spot_neighbor_list.*
import kotlinx.android.synthetic.main.listview_empty.view.*
import net.tochinavi.www.tochinaviapp.entities.*
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.network.HttpSpotInfo
import net.tochinavi.www.tochinaviapp.storage.*
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.*
import org.json.JSONObject

// 検討すること
// 周辺検索以外での「位置情報取得のダイアログ」は出さない
// 警告のみにする
// setTextViewHeaderを参考にパラメーターを追加する
class FragmentSpotNeighborList : Fragment() {

    companion object {
        val TAG = "FragmentSpotNeighborList"
        val TAG_SHORT = "SpotNeighborList"
    }

    // リクエスト
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_CODE_ALERT_LOCATION: Int = 0x2
    private val REQUEST_NARROW: Int = 0x3

    private lateinit var firebase: FirebaseHelper
    private lateinit var mySP: MySharedPreferences

    // UI //
    private var loading: LoadingNormal? = null

    // 変数 //
    private var listData: ArrayList<DataSpotList> = ArrayList()
    private var mAdapter: BaseAdapter? = null
    private var isEndScroll: Boolean = false

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null

    // 条件
    private var condPage: Int = 1
    private var condCategory: Int = 0
    private var condCategoryType: Int = 1 // 1,2,3
    private var condDistance: Double = ActivitySpotNeighborNarrow.dataDistanceArray[0]
    private var condCoupon: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spot_neighbor_list,container,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper(context!!)
        mySP = MySharedPreferences(context!!)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        // データの初期化
        condPage = 1
        condCategory = 0
        condCategoryType = 1
        condDistance = ActivitySpotNeighborNarrow.dataDistanceArray[0]
        condCoupon = false
        isEndScroll = false
        setTextViewParams()

        if (listData.count() > 0) {
            listData.clear()
            if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
        }

        // listViewの初期化
        mAdapter = ListSpotNeighborAdapter(context!!, listData)
        listView.apply {
            adapter = mAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                // スポット情報へ
                val item = listData[pos]
                firebase.sendSpotInfo(FirebaseHelper.screenName.Search_Near_List, item.type, item.id)
                if (item.type == 1) {
                    val intent = Intent(activity, ActivitySpotInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, ActivityHospitalInfo::class.java)
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

                loading!!.show(fragmentManager!!, LoadingNormal.TAG)
                loading!!.updateLayout(getString(R.string.loading_normal_message), true)

                // 位置情報を更新してから検索
                setLocation()

                // 数秒後に消す
                Handler().postDelayed({ refreshLayout.isRefreshing = false }, 1500)
            }
        }

        // 地図ボタン
        buttonMap.hide()
        buttonMap.setOnClickListener {
            if (listData.size > 0) {
                val intent = Intent(activity, ActivitySpotNeighborMap::class.java)
                if (mLocation != null) {
                    intent.putExtra("latitude", mLocation!!.latitude)
                    intent.putExtra("longitude", mLocation!!.longitude)
                }
                intent.putExtra("spotArray", listData)
                startActivity(intent)
            }
        }

        // 広告
        viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppNeighbor, resources)

    }

    // フラグメント　オンスクリーン
    override fun onResume() {
        super.onResume()

        if (activity != null) {
            activity!!.title = getString(R.string.spot_neighbor_list_title)
        }

        // Loadingの設定
        if (fragmentManager != null) {
            var enable: Boolean = false
            if (loading == null) { enable = true }

            if (loading != null && !loading!!.isVisible) { enable = true }
            if (enable) {
                loading = LoadingNormal.newInstance(
                    message = getString(R.string.loading_normal_message),
                    isProgress = true
                )
                loading!!.show(fragmentManager!!, LoadingNormal.TAG)
            }
        }

        setLocation()
    }

    // フラグメント　オフスクリーン
    override fun onPause() {
        super.onPause()

        // getLocationHighQualityの取得に時間がかかるため
        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    /** メニュー **/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // getActivity().getMenuInflater().inflate(R.menu.setting, menu);
        inflater.inflate(R.menu.search_narrow, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search_narrow) {
            // 絞り込みへ
            val intent = Intent(activity, ActivitySpotNeighborNarrow::class.java)
            intent.putExtra("category_id", condCategory)
            intent.putExtra("category_type", condCategoryType)
            intent.putExtra("distance", condDistance)
            intent.putExtra("coupon", condCoupon)

            startActivityForResult(intent, REQUEST_NARROW)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_NARROW -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    condCategory = data.getIntExtra("category_id", 0)
                    condCategoryType = data.getIntExtra("category_type", 0)
                    condDistance = data.getDoubleExtra("distance", 0.0)
                    condCoupon = data.getBooleanExtra("coupon", false)

                    // データの初期化
                    isEndScroll = false
                    condPage = 1
                    mLocation = null
                    if (listData.size > 0) {
                        listView.setSelection(0)
                    }
                    setTextViewParams()
                    // 次にonResumeが呼ばれる
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
        var category = ""
        if (condCategory > 0) {
            val db = DBHelper(context!!)
            try {
                when (condCategoryType) {
                    1 -> {
                        val data1: DataCategory1 =
                            DBTableCategory1(context!!).getData(db, condCategory)
                        category = data1.name
                    }
                    2 -> {
                        val data2: DataCategory2 =
                            DBTableCategory2(context!!).getData(db, condCategory)
                        category = data2.name
                    }
                    3 -> {
                        val data3: DataCategory3 =
                            DBTableCategory3(context!!).getData(db, condCategory)
                        category = data3.name
                    }
                    else -> {
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG_SHORT, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        // 範囲
        val distance: String = "%skm".format(condDistance)
        textViewParams.setText(if (category.isEmpty()) distance else "$category/$distance")

    }

    /** 位置情報取得 -> 周辺検索への流れ　（周辺検索する場合はこの関数を読めばOK） **/
    private fun setLocation() {
        hideListViewEmpty()
        // 端末の位置情報サービスをチェック
        val manager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        if (ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Log.i(">> $TAG", "checkPermission ok")
            // アプリの権限が許可してる
            getLocation()
        } else {
            // 許可してない
            //Log.i(">> $TAG", "checkPermission more request")
            requestLocationPermission()
        }
    }

    /**
     * Permissionの不許可チェック
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Log.i(">> $TAG", "requestLocationPermission alert");
            // パーミッションの許可で不許可にした場合
            errorLocation()
        } else {
            //Log.i(">> $TAG", "requestLocationPermission request")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_FINE_LOCATION
            )
        }
    }

    /**
     * Permissionアラートの結果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Log.i(">> $TAG", "onRequestPermissionsResult: $requestCode")
        when (requestCode) {
            REQUEST_PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可された時

                    // 近くにお気に入りしたスポットがあるときの処理 //
                    val intent = Intent(activity, ServiceNearWishSpot::class.java)
                    activity!!.stopService(intent)
                    activity!!.startService(intent)

                    getLocation()
                } else {
                    // 拒否された時
                    //Log.i(">> $TAG", "onRequestPermissionsResult alert");
                    errorLocation()
                }
            }
        }
    }

    /**
     * 位置情報を取得　Permissionで許可された時
     */
    private fun getLocation() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        mLocationClient!!.lastLocation.addOnCompleteListener(
            activity!!,
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
        if (loading != null) {
            // 5秒ほど時間がかかるため
            loading!!.updateLayout(getString(R.string.loading_normal_message) + "\nしばらくお待ちください...", true)
        }
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
        mLocationClient!!.requestLocationUpdates(request, mLocationCallback, null)
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {
        if (!(mySP.get(MySharedPreferences.Keys.spot_neighbor_location_first_alert) as Boolean)) {
            // アラート（1回のみ）
            mySP.put(MySharedPreferences.Keys.spot_neighbor_location_first_alert, true)

            if (fragmentManager != null) {
                val alert = AlertNormal.newInstance(
                    requestCode = REQUEST_CODE_ALERT_LOCATION,
                    title = "現在地を取得できませんでした",
                    msg = "周辺のお店を検索するには位置情報サービスをONにしてください",
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.setFragment(this)
                alert.show(fragmentManager!!, AlertNormal.TAG)
            }

            if (loading != null) {
                loading!!.onDismiss()
            }
        } else {
            // ローディング
            if (loading != null) {
                loading!!.updateLayout("現在地を取得できませんでした", true)
                loading!!.onDismiss(1000)
            }
        }

        if (listData.size > 0) {
            listData.clear()
            mAdapter!!.notifyDataSetChanged()
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

        if (loading != null) {
            loading!!.updateLayout(getString(R.string.loading_normal_message), true)
            loading!!.onDismiss(1000)
        }

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        val fParams: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("page" to condPage)
        fParams.add("page" to condPage.toString())

        // 周辺検索（エラー判定済み）
        params.add("latitude" to mLocation!!.latitude)
        params.add("longitude" to mLocation!!.longitude)
        fParams.add("lat_lon" to "%f,%f".format(mLocation!!.latitude, mLocation!!.longitude))

        // カテゴリー
        if (condCategory > 0) {
            params.add("cond_category" to condCategory)

            var tmpFParams = ""
            val db = DBHelper(context!!)
            try {
                val tableCategory2 = DBTableCategory2(context!!)
                val tableCategory3 = DBTableCategory3(context!!)
                when (condCategoryType) {
                    1 -> {
                        tmpFParams = "ca1: %d".format(condCategory)
                    }
                    2 -> {
                        val data_ca2 = tableCategory2.getData(db, condCategory)
                        tmpFParams = "ca2: %d{ca1: %d}".format(condCategory, data_ca2.parent_id)
                    }
                    3 -> {
                        val data_ca3 = tableCategory3.getData(db, condCategory)
                        val data_ca2 = tableCategory2.getData(db, data_ca3.parent_id)
                        tmpFParams = "ca3: %d{ca1: %d, ca2: %d}".format(condCategory, data_ca2.parent_id, data_ca2.id)
                    }
                    else -> {
                    }
                }
            } catch (e: Exception) {
                Log.e(HttpSpotInfo.TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
            fParams.add("cond_category" to tmpFParams)
        }

        // 距離
        if (condDistance > 0) {
            params.add("cond_distance_range" to condDistance)
            fParams.add("cond_distance_range" to condDistance.toString())
        }

        // クーポン
        if (condCoupon) {
            params.add("cond_coupon" to condCoupon)
            fParams.add("cond_coupon" to if (condCoupon) "1" else "0")
        }

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(context!!)
            try {
                val tableUsers = DBTableUsers(context!!)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                    fParams.add("user_id" to it.user_id.toString())
                })
            } catch (e: Exception) {
                Log.e(HttpSpotInfo.TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        firebase.sendScreen(FirebaseHelper.screenName.Search_Near_List, fParams)

        val url = MyString().my_http_url_app() + "/spot/get_neighbor_list.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                if (loading != null) {
                    loading!!.onDismiss(300)
                }

                if (condPage == 1 && listData.count() > 0) {
                    listView.setSelection(0)
                    listData.clear()
                    mAdapter!!.notifyDataSetChanged()
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
                                obj.getDouble("latitude"),
                                obj.getDouble("longitude")
                            )
                        )
                    }

                    mAdapter!!.notifyDataSetChanged()
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

                buttonMap.apply {
                    if (listData.size > 0) {
                        show()
                    } else {
                        hide()
                    }
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }


}
