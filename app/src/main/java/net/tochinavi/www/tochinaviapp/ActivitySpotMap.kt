package net.tochinavi.www.tochinaviapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_spot_map.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.value.MyImage
import net.tochinavi.www.tochinaviapp.view.AlertNormal

/**
 * 経路表示の時のみに現在地取得を使う
 * 周辺検索を下にプログラムを書く
 */
class ActivitySpotMap :
    AppCompatActivity(),
    OnMapReadyCallback,
    AlertNormal.OnSimpleDialogClickListener {

    // リクエスト
    private val REQUEST_ALERT_PERMISSION: Int = 0x1

    companion object {
        val TAG = "ActivitySpotMap"
        val TAG_SHORT = "ActivitySpotMap"
    }

    private lateinit var firebase: FirebaseHelper
    private lateinit var screenName: FirebaseHelper.screenName

    // データ
    private lateinit var mMap: GoogleMap
    private var mPolyline: Polyline? = null // 有無判定のため
    private lateinit var dataSpot: DataSpotInfo

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_map)

        firebase = FirebaseHelper(applicationContext)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // type, spot_id, name, latitude, longitudem category_id
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo

        screenName = if (dataSpot.type == 1) FirebaseHelper.screenName.Spot_Info_Map else FirebaseHelper.screenName.Hospital_Info_Map
        firebase.sendScreen(screenName, arrayListOf(Pair("id", dataSpot.id.toString())))

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // 目的地まで案内
        buttonRoute.setOnClickListener {

            firebase.sendEvent(
                screenName, FirebaseHelper.eventCategory.Button, FirebaseHelper.eventAction.Tap, "目的地まで案内:Google Maps")

            // Google Mapで経路表示
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )
            intent.data =
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=%f,%f".format(dataSpot.latitude, dataSpot.longitude))
            startActivity(intent)
        }

        // パーミッションチェック
        if (!checkPermission()) {
            showLocationAlert()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // menuInflater.inflate(R.menu.spot_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_show_route -> {
                /*
                // 経路を表示
                // アナリティクス送信
                mFirebase.sendEvent(
                        (spotType == 1 ? FirebaseHelper.screenName.Spot_Info_Map : FirebaseHelper.screenName.Hospital_Info_Map),
                        FirebaseHelper.eventCategory.Button,
                        FirebaseHelper.eventAction.Tap,
                        "メニュー: 経路を表示"
                );

                // 位置取得 > 経路取得
                if (checkPermission()) {
                    getLocation()
                } else {
                    showLocationAlert()
                }
                 */
            }
            R.id.menu_cancel_route -> {
                /*
                // 経路解除
                if (mPolyline != null) {
                    mGoogleMap.clear();
                    mPolyline = null;
                    setMarker(spotLatitude, spotLongitude);

                    // アナリティクス送信
                    mFirebase.sendEvent(
                            (spotType == 1 ? FirebaseHelper.screenName.Spot_Info_Map : FirebaseHelper.screenName.Hospital_Info_Map),
                            FirebaseHelper.eventCategory.Button,
                            FirebaseHelper.eventAction.Tap,
                            "メニュー: 経路を解除"
                    );
                }
                 */
            }
            R.id.menu_return_current -> {
                /*
                // 現在地に戻る
                startLocationReturnCurrent();

                // アナリティクス送信
                mFirebase.sendEvent(
                        (spotType == 1 ? FirebaseHelper.screenName.Spot_Info_Map : FirebaseHelper.screenName.Hospital_Info_Map),
                        FirebaseHelper.eventCategory.Button,
                        FirebaseHelper.eventAction.Tap,
                        "現在地に戻る"
                );
                 */
            }
            R.id.menu_show_all -> {
                /*
                // 全体表示
                startLocationShowAll();

                // アナリティクス送信
                mFirebase.sendEvent(
                        (spotType == 1 ? FirebaseHelper.screenName.Spot_Info_Map : FirebaseHelper.screenName.Hospital_Info_Map),
                        FirebaseHelper.eventCategory.Button,
                        FirebaseHelper.eventAction.Tap,
                        "全体を表示"
                );
                 */
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // メニュー項目の変更
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        /*
        MenuItem menuShowRoute = menu.findItem(R.id.menu_show_route);
        MenuItem menuCancelRoute = menu.findItem(R.id.menu_cancel_route);
        if (mPolyline != null) {
            menuShowRoute.setVisible(false);
            menuCancelRoute.setVisible(true);
        } else {
            menuShowRoute.setVisible(true);
            menuCancelRoute.setVisible(false);
        }
        return true;
         */
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * アラート　ポジティブ
     */
    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_PERMISSION -> {
                // 特に何もしない
            }
        }
    }

    /**
     * アラート　ネガティブ
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    /**
     * Permissionのチェック
     * ・端末の位置情報サービスのチェック
     * ・パーミッションのチェック
     */
    private fun checkPermission(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // パーミッションのチェックが必要か確認
            if (Build.VERSION.SDK_INT >= 23) {
                // Android6以上
                return ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
            // それ以外
            return true
        }
        return false
    }

    private fun showLocationAlert() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PERMISSION,
            title = "位置情報サービスをONにしてください",
            msg = "位置情報サービスは「現在地の取得」と「経路を表示」に使用します",
            positiveLabel = "OK",
            negativeLabel = null
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    /**
     * 位置情報を取得　Permissionで許可された時
     */
    private fun getLocation() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        mLocationClient!!.lastLocation.addOnCompleteListener(
            this,
            OnCompleteListener<Location?> { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        mLocation = task.result
                        // Log.i(">> $TAG", "getLatLon: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                        // setRoute()
                    } else {
                        // last location is null
                        // Log.i(">> $TAG", "getLatLon: last location is null")
                    }
                } else {
                    // Log.i(">> $TAG", "getLatLon: error")
                }
            })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setSpotMarker()
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
        zoomMap(dataSpot.latitude, dataSpot.longitude)
    }

    /**
     * マーカーの設置
     */
    private fun setSpotMarker() {
        val bmp = BitmapFactory.decodeResource(
            resources,
            MyImage().icon_category_pin(dataSpot.category_large_id)
        )
        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(dataSpot.latitude, dataSpot.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
        )
    }

    /**
     * 1点の位置からいい感じにZoomしてくれる
     * @param latitude
     * @param longitude
     */
    private fun zoomMap(latitude: Double, longitude: Double) {
        val south = latitude * (1 - 0.00001)
        val west = longitude * (1 - 0.00001)
        val north = latitude * (1 + 0.00001)
        val east = longitude * (1 + 0.00001)
        val bounds = LatLngBounds.builder()
            .include(LatLng(south, west))
            .include(LatLng(north, east))
            .build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0))
    }

    /**
     * Googleの経路取得のAPIが有料化したため、使わない
     */
    private fun setRoute() {
        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("origin" to "%f,%f".format(mLocation!!.latitude, mLocation!!.longitude)) // 現在地
        params.add("destination" to "%f,%f".format(dataSpot.latitude, dataSpot.longitude)) // 目的地
        params.add("sensor" to "false")
        params.add("mode" to "driving")
        params.add("alternatives" to "true")

        val url = "http://maps.googleapis.com/maps/api/directions/json"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                // 画面クリアしてから、経路を表示する
                if (mPolyline != null) {
                    mMap.clear()
                    mPolyline = null
                    setSpotMarker()
                }
                val routes = json.obj().getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                val points = overviewPolyline.getString("points")
                val list: List<LatLng> = decodePoly(points)

                for (i in 0..list.size - 1) {
                    val src = list[i]
                    val dest = list[i + 1]
                    mPolyline = mMap.addPolyline(
                        PolylineOptions()
                            .add(
                                LatLng(src.latitude, src.longitude),
                                LatLng(dest.latitude, dest.longitude)
                            )
                            .width(10f).color(Color.BLUE).geodesic(true)
                    )
                }
                // 処理後Zoom
                zoomMapFromTo(
                    mLocation!!.latitude,
                    mLocation!!.longitude,
                    dataSpot.latitude,
                    dataSpot.longitude
                )


            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
            })
        }
    }

    /**
     * 2点間の位置からいい感じにZoomしてくれる
     * @param fromLat
     * @param fromLng
     * @param toLat
     * @param toLng
     */
    private fun zoomMapFromTo(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ) {
        var south = 0.0
        var west = 0.0
        var north = 0.0
        var east = 0.0
        if (fromLat > toLat) {
            south = toLat
            north = fromLat
        } else {
            south = fromLat
            north = toLat
        }
        if (fromLng > toLng) {
            west = toLng
            east = fromLng
        } else {
            west = fromLng
            east = toLng
        }
        south *= 1 - 0.00015
        west *= 1 - 0.00015
        north *= 1 + 0.00015
        east *= 1 + 0.00015
        val bounds = LatLngBounds.builder()
            .include(LatLng(south, west))
            .include(LatLng(north, east))
            .build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0))
    }

    /**
     * APIから取得した情報からルート情報を読み取る
     * @param encoded
     * @return
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = java.util.ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }
}
