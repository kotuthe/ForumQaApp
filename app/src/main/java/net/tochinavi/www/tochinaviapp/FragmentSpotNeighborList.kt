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
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.fragment_top.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotList
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.view.AlertNormal
import net.tochinavi.www.tochinaviapp.view.LoadingNormal
import org.json.JSONObject

// Topページを見ながら作成
// 1.絞り込みメニュー
// 2.検索処理
// 3.リストアダプター
// 4.listview
class FragmentSpotNeighborList : Fragment() {

    companion object {
        val TAG = "SpotNeighborList"
    }

    // リクエスト
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_CODE_ALERT_LOCATION: Int = 0x2
    private val REQUEST_NARROW: Int = 0x3

    private var mySP: MySharedPreferences? = null

    // UI //
    private var loading: LoadingNormal? = null

    // 変数 //
    private var listData: ArrayList<DataSpotList> = ArrayList()

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null

    // 条件
    private var condPage: Int = 0
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
        // Log.i(">> $TAG", "onCreate")
        mySP = MySharedPreferences(context!!)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // フラグメント　オンスクリーン
    override fun onResume() {
        super.onResume()
        Log.i(">> $TAG", "onResume")

        // Loadingの設定
        if (fragmentManager != null) {
            var enable: Boolean = false
            if (loading == null) { enable = true }

            if (loading != null && !loading!!.isVisible) { enable = true }
            if (enable) {
                loading = LoadingNormal.newInstance(
                    message = "読み込み中...",
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
        Log.i(">> $TAG", "onPause")

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
            Log.i(">> $TAG", "絞り込みへ")
            val intent = Intent(activity, ActivitySpotNeighborNarrow::class.java)
            startActivityForResult(intent, REQUEST_NARROW)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Log.i(">> $TAG", "onActivityResult: $requestCode, $resultCode, $data")
        when(requestCode) {
            REQUEST_NARROW -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    condCategory = data.getIntExtra("category_id", 0)
                    condCategoryType = data.getIntExtra("category_type", 0)
                    condDistance = data.getDoubleExtra("distance", 0.0)
                    condCoupon = data.getBooleanExtra("coupon", false)

                    // 検索へ
                }
            }
        }
    }

    /** 位置情報取得 -> 周辺検索への流れ　（周辺検索する場合はこの関数を読めばOK） **/
    private fun setLocation() {
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
                    //Log.i(">> $TAG", "onRequestPermissionsResult OK");
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
                        Log.i(">> $TAG", "getLatLon: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                        // この後周辺検索へ
                    } else {
                        // last location is null
                        Log.i(">> $TAG", "getLatLon: last location is null")
                        getLocationHighQuality()
                    }
                } else {
                    Log.i(">> $TAG", "getLatLon: error")
                    errorLocation()
                }
            })
    }

    /**
     * getLocationで位置取得できない時、さらに位置情報取得
     * ※位置情報サービスOFF -> ON に変更した時になる
     */
    private fun getLocationHighQuality() {
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 500
        request.fastestInterval = 300

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                mLocation = result.lastLocation
                // 現在地だけ欲しいので、1回取得したらすぐに外す
                mLocationClient!!.removeLocationUpdates(this)
                Log.i(">> $TAG", "getLatLon HighQuality: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                // この後周辺検索へ
            }
        }
        mLocationClient!!.requestLocationUpdates(request, mLocationCallback, null)
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {

        Log.i(">> $TAG", "errorLocation")
        if (!(mySP!!.get(MySharedPreferences.Keys.spot_neighbor_location_first_alert) as Boolean)) {
            // アラートを表示
            mySP!!.put(MySharedPreferences.Keys.spot_neighbor_location_first_alert, true)

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
        }
        // 位置情報なしの検索へ
    }

    /**
     * お店の検索
     */
    private fun onSearch() {
        Log.i(">> ${FragmentTop.TAG}", "onSearch")
        if (mLocation == null) {
            // 周辺検索はできないよ
            /*
            // アラート表示
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(getContext())
                            .setTitle("周辺のお店を検索できません")
                            .setMessage("周辺のお店を検索するには位置情報の利用を許可してください")
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
             */
            // データがnullですのエラー文を表示（listViewの裏）
            return
        }

        if (loading != null) {
            Log.i(">> ${FragmentTop.TAG}", "onSearch loading not null")
            loading!!.updateLayout("データの読み込み中...", true)
            loading!!.onDismiss(1000)
        }

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("page" to condPage)

        // 周辺検索（エラー判定済み）
        params.add("latitude" to mLocation!!.latitude)
        params.add("longitude" to mLocation!!.longitude)

        // カテゴリー
        if (condCategory > 0) {
            params.add("cond_category" to condCategory)
        }

        // 距離
        if (condDistance > 0) {
            params.add("cond_distance_range" to condDistance)
        }

        // クーポン
        if (condCoupon) {
            params.add("cond_coupon" to condCoupon)
        }

        // ログインID

        println(params)

        val url = MyString().my_http_url_app() + "/search_spot/search.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                if (loading != null) {
                    loading!!.onDismiss(300)
                }

                if (condPage == 1 && listData.count() > 0) {
                    recyclerView.smoothScrollToPosition(0)
                    listData.clear()
                    mAdapter!!.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {

                    val spot_array = datas.getJSONArray("spot_array")
                    for (i in 0..spot_array.length() - 1) {
                        val obj = spot_array.getJSONObject(i)
                        val review_image_list = obj.getJSONArray("review_image_list")
                        var review_image_array: ArrayList<String> = ArrayList()
                        if (review_image_list.length() > 0) {
                            for (j in 0..review_image_list.length() - 1) {
                                review_image_array.add(review_image_list.getString(j))
                            }
                        }
                        val distance: String = if (mLocation != null) obj.getString("distance") else ""
                        listData.add(
                            DataSpotList(
                                obj.getInt("id"),
                                obj.getInt("type"),
                                obj.getString("name"),
                                obj.getString("address"),
                                obj.getInt("parent_category_id"),
                                obj.getString("category"),
                                distance,
                                obj.getInt("review_num"),
                                obj.getString("image"),
                                obj.getBoolean("checkin_enable"),
                                obj.getBoolean("coupon_enable"),
                                obj.getInt("review_image_num"),
                                obj.getInt("favorite_num"),
                                review_image_array
                            )
                        )
                    }
                    mAdapter!!.notifyDataSetChanged()
                    condPage++
                } else {

                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }


}
