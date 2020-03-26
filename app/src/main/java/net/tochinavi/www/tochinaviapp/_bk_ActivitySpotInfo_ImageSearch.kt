package net.tochinavi.www.tochinaviapp



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_spot_info_image_search.*
import kotlinx.android.synthetic.main.fragment_top.*
import kotlinx.android.synthetic.main.view_info_share.view.*
import kotlinx.android.synthetic.main.view_info_spot_actions_footer.view.*
import kotlinx.android.synthetic.main.view_info_spot_basic.view.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoBasic
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.network.HttpSpotInfo
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableCategory1
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.*
import net.tochinavi.www.tochinaviapp.view.*
import org.json.JSONObject


// 続きはここから
// しっかり丁寧に作ること
class _bk_ActivitySpotInfo_ImageSearch :
    AppCompatActivity(),
    OnMapReadyCallback,
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotInfo_ImageSearch"
        val TAG_SHORT = "ActivitySpotInfo_IS"
    }

    // 位置情報サービスの使用用途
    enum class LocationType  {
        getAll,
        getSpot,
        checkin,
    }

    // リクエスト
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_ALERT_NO_DATA: Int = 0x2

    // UI //
    //layoutBasic
    //layoutShare

    // 変数 //
    private var mContext: Context? = null
    private var mySP: MySharedPreferences? = null
    private var dataSpot: DataSpotInfo? = null
    private lateinit var mGoogleMap: GoogleMap
    private var reviewImagesNumber: Int = 0

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null
    private var targetLocationType: LocationType = LocationType.getAll

    // ビュー
    private var imageAdapter: RecyclerISReviewImagesAdapter? = null
    private var imageListData: ArrayList<DataSpotReview> = ArrayList()
    private var textAdapter: RecyclerISReviewTextsAdapter? = null
    private var textListData: ArrayList<DataSpotReview> = ArrayList()
    private var basicAdapter: BaseAdapter? = null
    private var basicListData: ArrayList<DataSpotInfoBasic> = ArrayList()

    // 条件
    private var condReviewImagePage: Int = 1
    private var condReviewTextPage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_info_image_search)

        Log.i(">> $TAG_SHORT", "onCreate")
        mContext = applicationContext
        mySP = MySharedPreferences(mContext!!)

        // スポット名取得
        val spot_id: Int = intent.getIntExtra("id", 0)
        dataSpot = DataSpotInfo(
            spot_id,
            "",
            false,
            "",
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
        ifNotNull(intent.getStringExtra("name"), {
            dataSpot!!.name = it
        })

        initLayout()
        setLocation()
    }

    // オフスクリーン
    override fun onPause() {
        super.onPause()

        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        Log.i(">> $TAG_SHORT", "onSimpleDialogActionClick requestCode: $requestCode")
        when (requestCode) {
            REQUEST_ALERT_NO_DATA -> {
                finish()
            }
        }
    }

    override fun onSimpleDialogNegativeClick(requestCode: Int) {
        Log.i(">> $TAG_SHORT", "onSimpleDialogNegativeClick requestCode: $requestCode")
    }

    private fun initLayout() {
        Log.i(">> $TAG_SHORT", "initLayout")

        // 上部 //
        if (!dataSpot!!.name.isEmpty()) {
            textViewSpotName.text = dataSpot!!.name
        }

        // クチコミ写真 //
        imageAdapter = RecyclerISReviewImagesAdapter(mContext!!, imageListData)
        rcvReviewImage.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 列
            val lmg = GridLayoutManager(context, imageAdapter!!.spanCount)
            lmg.orientation = LinearLayoutManager.HORIZONTAL
            lmg.spanSizeLookup = imageAdapter!!.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(imageAdapter!!.mItemDecoration)
            adapter = imageAdapter

            // イベント //
            clearOnScrollListeners()
            addOnScrollListener(RecyclerInfiniteScrollListener(lmg) {
                // 続きを検索
                Log.i(">> $TAG_SHORT", "rcvReviewImage: 続き $condReviewImagePage")
                getReviewImages()
            })

            // クチコミ写真をタップしている時はScrollviewはスクロールさせない
            setOnTouchListener { view, motionEvent ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }

        imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            // ギャラリーへ
            val index = view.id
            Log.i(">> $TAG_SHORT", "ギャラリーへ : ${imageListData[index].id}")
        })

        // クチコミテキスト //
        textAdapter = RecyclerISReviewTextsAdapter(mContext!!, textListData)
        rcvReviewText.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 列
            val lmg = GridLayoutManager(context, textAdapter!!.spanCount)
            lmg.orientation = LinearLayoutManager.HORIZONTAL
            lmg.spanSizeLookup = textAdapter!!.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(textAdapter!!.mItemDecoration)
            adapter = textAdapter

            // イベント //
            clearOnScrollListeners()
            addOnScrollListener(RecyclerInfiniteScrollListener(lmg) {
                // 続きを検索
                Log.i(">> $TAG_SHORT", "rcvReviewText: 続き $condReviewTextPage")
                getReviewTexts()
            })

            // クチコミをタップしている時はScrollviewはスクロールさせない
            setOnTouchListener { view, motionEvent ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
        /* いい感じのページャー（まだ使えない）
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rcvReviewText)
         */

        textAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            // クチコミ詳細へ
            val index = view.id
            Log.i(">> $TAG_SHORT", "クチコミ詳細へ : ${textListData[index].id}")
        })


        // 基本情報 //
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        layoutBasic.buttonWebDetail.setOnClickListener {
        }

        basicAdapter = ListSpotInfoBasicAdapter(mContext!!, basicListData)
        layoutBasic.listView.apply {
            adapter = basicAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                Log.i(">> $TAG_SHORT", "position: $pos")
            }
        }

        // アクションフッター //
        layoutActions.viewReview.setOnClickListener {

        }
        layoutActions.viewFavorite.setOnClickListener {

        }
        layoutActions.viewCheckin.setOnClickListener {

        }
        layoutActions.viewMap.setOnClickListener {

        }
        layoutActions.viewPhone.setOnClickListener {

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(">> $TAG_SHORT", "onMapReady")
        mGoogleMap = googleMap
        val sydney = LatLng(-34.0, 151.0) // 宇都宮駅
        mGoogleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    /** 地図の描画更新  */
    private fun updateMapPosition() {
        if (dataSpot != null) {
            var bmpPin = BitmapFactory.decodeResource(resources, MyImage().icon_category_pin(1))
            val lat_lng = LatLng(dataSpot!!.latitude, dataSpot!!.longitude)
            if (dataSpot!!.category_large_id > 0) {
                bmpPin = BitmapFactory.decodeResource(
                    resources,
                    MyImage().icon_category_pin(dataSpot!!.category_large_id)
                )
            }
            mGoogleMap.addMarker(
                MarkerOptions()
                    .position(lat_lng)
                    .title("Spot")
                    .icon(BitmapDescriptorFactory.fromBitmap(bmpPin))
            )
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat_lng, 15f))
        }
    }


    /** 位置情報取得 -> スポット取得 or チェックイン **/
    private fun setLocation() {
        // 端末の位置情報サービスをチェック
        val manager = mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        if (ActivityCompat.checkSelfPermission(mContext!!,
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Log.i(">> $TAG", "requestLocationPermission alert");
            // パーミッションの許可で不許可にした場合
            errorLocation()
        } else {
            //Log.i(">> $TAG", "requestLocationPermission request")
            // ※2度でまだが、Activityの場合はこれをしないとコードエラー
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_FINE_LOCATION
                )
            }
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
        mLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        mLocationClient!!.lastLocation.addOnCompleteListener(
            this,
            OnCompleteListener<Location?> { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        mLocation = task.result
                        Log.i(">> ${TAG_SHORT}", "getLatLon: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                        successLocation()
                    } else {
                        // last location is null
                        Log.i(">> ${TAG_SHORT}", "getLatLon: last location is null")
                        when (targetLocationType) {
                            LocationType.getAll, LocationType.getSpot -> {
                                // 時間がかかる場合は位置情報なしでデータ取得する
                                errorLocation()
                            }
                            LocationType.checkin -> {
                                // チェックイン
                                getLocationHighQuality()
                            }
                        }
                    }
                } else {
                    Log.i(">> ${TAG_SHORT}", "getLatLon: error")
                    errorLocation()
                }
            })
    }

    /**
     * getLocationで位置取得できない時、さらに位置情報取得
     * ※位置情報サービスOFF -> ON に変更した時になる
     */
    private fun getLocationHighQuality() {
        /* チェックインの場合はloadingが出る
        if (loading != null) {
            // 5秒ほど時間がかかるため
            loading!!.updateLayout(getString(R.string.loading_normal_message) + "\nしばらくお待ちください...", true)
        }
        */
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 500
        request.fastestInterval = 300

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                mLocation = result.lastLocation
                // 現在地だけ欲しいので、1回取得したらすぐに外す
                mLocationClient!!.removeLocationUpdates(this)
                Log.i(">> ${TAG_SHORT}", "getLatLon HighQuality: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                successLocation()
            }
        }
        mLocationClient!!.requestLocationUpdates(request, mLocationCallback, null)
    }

    private fun successLocation() {
        when (targetLocationType) {
            LocationType.getAll -> {
                getReviewImages()
            }
            LocationType.getSpot -> {
                // スポットの取得 先に写真＆クチコミを取得してから
                getSpotData()
            }
            LocationType.checkin -> {
                // チェックイン
            }
        }
        // リセット
        targetLocationType = LocationType.getAll
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {
        Log.i(">> ${TAG_SHORT}", "errorLocation")

        mLocation = null
        when (targetLocationType) {
            LocationType.getAll -> {
                // そのまま取得へ
                getReviewImages()
            }
            LocationType.getSpot -> {
                // そのまま取得へ
                getSpotData()
            }
            LocationType.checkin -> {
                // 「チェックインできない」アラートを表示後、処理を中止
            }
        }
        // リセット
        targetLocationType = LocationType.getAll

        /*
        val alert = AlertNormal.newInstance(
                    requestCode = REQUEST_CODE_ALERT_LOCATION,
                    title = "現在地を取得できませんでした",
                    msg = "周辺のお店を検索するには位置情報サービスをONにしてください",
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.setFragment(this)
                alert.show(fragmentManager!!, AlertNormal.TAG)
         */

    }

    /**
     * クチコミ画像を取得
     */
    private fun getReviewImages() {
        Log.i(">> $TAG_SHORT", "getReviewImages")

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to dataSpot!!.id)
        params.add("page" to condReviewImagePage)
        val url = MyString().my_http_url_app() + "/search_spot/get_spot_review_images.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val json_array = datas.getJSONArray("review")
                    reviewImagesNumber = datas.getInt("all_number")

                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        val js_review_images = obj.getJSONArray("review_images")
                        var review_image_array: ArrayList<String> = arrayListOf()
                        if (js_review_images.length() > 0) {
                            for (j in 0..js_review_images.length() - 1) {
                                review_image_array.add(js_review_images.getString(j))
                            }
                        }

                        if (obj.getBoolean("user_enable")) {
                            // クチコミの写真
                            val dataReview = DataSpotReview(
                                obj.getInt("review_id"),
                                dataSpot!!.id,
                                dataSpot!!.name,
                                obj.getInt("user_id"),
                                obj.getString("user_name"),
                                obj.getString("user_icon"),
                                obj.getString("user_detail"),
                                obj.getString("review_date"),
                                obj.getString("user_review"),
                                review_image_array,
                                "",
                                obj.getInt("good_num"),
                                obj.getBoolean("enable_good")
                            )
                            imageListData.add(dataReview)

                        } else {
                            // スポットの写真
                            val dataReview = DataSpotReview(
                                obj.getInt("review_id"),
                                dataSpot!!.id,
                                dataSpot!!.name,
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
                            imageListData.add(dataReview)
                        }
                    }
                    imageAdapter!!.notifyDataSetChanged()

                    if (condReviewImagePage == 1) {
                        // スケルトンビュー
                        (rcvReviewImage.background as TransitionDrawable).startTransition(1000)
                        getReviewTexts()
                    }
                    condReviewImagePage++

                } else {
                    // データなし ※ここが通る場合はサーバー側のエラー
                    // スポットページに到達しないため何かを考える（ただしcondReviewTextPage == 1の時だけ）
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }
    //////////////////////////
    /**
     * クチコミテキストを取得（続きはここから）
     */
    private fun getReviewTexts() {
        Log.i(">> $TAG_SHORT", "getReviewTexts")

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to dataSpot!!.id)
        params.add("page" to condReviewTextPage)
        val url = MyString().my_http_url_app() + "/search_spot/get_spot_review_texts.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val json_array = datas.getJSONArray("review")

                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        val js_review_images = obj.getJSONArray("review_images")
                        var review_image_array: ArrayList<String> = arrayListOf()
                        if (js_review_images.length() > 0) {
                            for (j in 0..js_review_images.length() - 1) {
                                review_image_array.add(js_review_images.getString(j))
                            }
                        }

                        val dataReview = DataSpotReview(
                            obj.getInt("review_id"),
                            dataSpot!!.id,
                            dataSpot!!.name,
                            obj.getInt("user_id"),
                            obj.getString("user_name"),
                            obj.getString("user_icon"),
                            obj.getString("user_detail"),
                            obj.getString("review_date"),
                            obj.getString("user_review"),
                            review_image_array,
                            "",
                            obj.getInt("good_num"),
                            obj.getBoolean("enable_good")
                        )
                        textListData.add(dataReview)

                    }
                    textAdapter!!.notifyDataSetChanged()

                    if (condReviewTextPage == 1) {
                        // スケルトンビュー
                        (rcvReviewText.background as TransitionDrawable).startTransition(500)
                        getSpotData()
                    }
                    condReviewTextPage++

                } else {
                    // データなし ※ここが通る場合はサーバー側のエラー
                    // スポットページに到達しないため何かを考える（ただしcondReviewTextPage == 1の時だけ）
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }

    /**
     * スポットデータの取得
     */
    private fun getSpotData() {
        Log.i(">> $TAG_SHORT", "getSpotData")

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to dataSpot!!.id)

        // 周辺検索（エラー判定済み）
        if (mLocation != null) {
            params.add("latitude" to mLocation!!.latitude)
            params.add("longitude" to mLocation!!.longitude)
        }

        // ログインID
        if (mySP!!.get_status_login()) {
            val db = DBHelper(mContext!!)
            try {
                val tableUsers = DBTableUsers(mContext!!)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG_SHORT, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        println(params)

        val url = MyString().my_http_url_app() + "/spot/get_spot_info.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val obj = datas.getJSONObject("info")
                    dataSpot = DataSpotInfo(
                        dataSpot!!.id,
                        obj.getString("image"),
                        obj.getBoolean("more_image"),
                        obj.getString("name"),
                        obj.getString("address"),
                        obj.getString("phone"),
                        obj.getString("hour"),
                        obj.getString("holiday"),
                        obj.getString("simple_detail"),
                        obj.getString("simple_caption"),
                        obj.getDouble("latitude"),
                        obj.getDouble("longitude"),
                        obj.getInt("category_large_id"),
                        datas.getBoolean("checkin"),
                        true,
                        obj.getBoolean("review_photo_flag"),
                        obj.getBoolean("wishlist_flag"),
                        obj.getString("sns_share_text"),
                        obj.getString("sns_share_text_long"),
                        obj.getInt("is_free"),
                        obj.getBoolean("coupon_enable")
                    )
                    setLayoutDataSpot()
                } else {
                    // データなし
                    // 「取得できませんでした」アラートを表示して、
                    // OK押したあとは前のページに戻る
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(FragmentTop.TAG, error.toString())
            })
        }
    }

    /**
     * dataSpotで設定できるレイアウト
     */
    private fun setLayoutDataSpot() {
        if (dataSpot != null) {
            // 地図更新
            updateMapPosition()

            // 基本情報
            basicListData.clear()
            if (!dataSpot!!.address.isEmpty()) {
                basicListData.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.address, dataSpot!!.address))
            }
            if (!dataSpot!!.phone.isEmpty()) {
                basicListData.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.phone, dataSpot!!.phone))
            }
            if (!dataSpot!!.hour.isEmpty()) {
                basicListData.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.hour, dataSpot!!.hour))
            }
            if (!dataSpot!!.holiday.isEmpty()) {
                basicListData.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.holiday, dataSpot!!.holiday))
            }
            if (dataSpot!!.coupon_enable) {
                basicListData.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.coupon, "クーポン情報はこちら"))
            }
            basicAdapter!!.notifyDataSetChanged()

            // シェア
            layoutShare.textViewTitle.text = "お店の情報をシェアする"
            layoutShare.textViewCopyTitle.text = "URLをコピーする"
            layoutShare.textViewCopy.text = dataSpot!!.snsShareText

            // アクションフッター
            layoutActions.imageViewFavorite.setImageDrawable(
                ContextCompat.getDrawable(mContext!!, if (dataSpot!!.bookMarkEnable)
                    R.drawable.img_spot_info_favorite else R.drawable.img_spot_info_favorite_dis))
            layoutActions.imageViewCheckin.setImageDrawable(
                ContextCompat.getDrawable(mContext!!, if (dataSpot!!.checkinEnable)
                    R.drawable.img_spot_info_action_checkin else R.drawable.img_spot_info_action_checkin_dis))

            // 上部の項目を表示することで下にスライドしなくなる
            textViewSpotName.text = dataSpot!!.name

        }

    }

}
