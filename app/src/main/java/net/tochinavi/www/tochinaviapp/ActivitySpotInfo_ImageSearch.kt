package net.tochinavi.www.tochinaviapp



import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_spot_info_image_search.*
import kotlinx.android.synthetic.main.view_info_share.view.*
import kotlinx.android.synthetic.main.view_info_spot_actions_footer.view.*
import kotlinx.android.synthetic.main.view_info_spot_basic.view.*
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoBasic
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.network.HttpSpotInfo
import net.tochinavi.www.tochinaviapp.value.*
import net.tochinavi.www.tochinaviapp.view.*


// 続きはここから
// もとに戻るボタンの実装
class ActivitySpotInfo_ImageSearch :
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

    enum class LoginType  {
        favorite,
        review,
        checkin,
    }

    // リクエスト
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_IN_LOGIN: Int = 0x2
    private val REQUEST_ALERT_NO_DATA: Int = 0x3
    private val REQUEST_ALERT_CHECKIN: Int = 0x4
    private val REQUEST_ALERT_NO_LOGIN: Int = 0x5
    private val REQUEST_ALERT_FAVORITE: Int = 0x6
    private val REQUEST_ALERT_MEMBER_MORE_ENTRY: Int = 0x7
    private val REQUEST_ALERT_PHONE: Int = 0x8

    // UI //
    private var loading: LoadingNormal? = null

    // 変数 //
    private var functions: Functions? = null
    private var mContext: Context? = null
    private lateinit var firebase: FirebaseHelper
    private var mySP: MySharedPreferences? = null
    private var dataSpot: DataSpotInfo? = null
    private lateinit var mGoogleMap: GoogleMap
    private var reviewImagesNumber: Int = 0
    private var isGetSpotInfo = false

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

        mContext = applicationContext
        firebase = FirebaseHelper(mContext!!)
        mySP = MySharedPreferences(mContext!!)
        functions = Functions(mContext!!)

        // スポット名取得
        val spot_id: Int = intent.getIntExtra("id", 0)
        dataSpot = DataSpotInfo(
            spot_id,
            1,
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

        if (supportActionBar != null) {
            supportActionBar!!.title = "写真から探す"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            // ログイン後
            REQUEST_IN_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    // スポットページの更新
                    getSpotData()
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
     * アラート　ポジティブ
     */
    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_NO_DATA -> {
                finish()
            }
            REQUEST_ALERT_NO_LOGIN -> {
                // ログイン画面へ
                val intent = Intent(this, ActivityLogin::class.java)
                intent.putExtra("tag", TAG)
                startActivityForResult(intent, REQUEST_IN_LOGIN)
            }
            REQUEST_ALERT_CHECKIN -> {
                // チェックインへ
                loading!!.updateLayout("チェックインをしています...", true)
                loading!!.show(supportFragmentManager, LoadingNormal.TAG)

                targetLocationType = LocationType.checkin
                setLocation()
            }
            REQUEST_ALERT_FAVORITE -> {
                // お気に入り
                loading!!.updateLayout(if (dataSpot!!.bookMarkEnable)
                    "お気に入りを解除します..." else "お気に入りに登録します...", true)
                loading!!.show(supportFragmentManager, LoadingNormal.TAG)

                // お気に入り登録へ
                doFavorite()
            }
            REQUEST_ALERT_MEMBER_MORE_ENTRY -> {
                // 会員情報で不足分を入力する
                startActivity(MyIntent().web_browser(MyString().my_http_url_member_info_update()))
            }
            REQUEST_ALERT_PHONE -> {
                // 電話をする
                startActivity(MyIntent().phone(dataSpot!!.phone))
            }

        }
    }

    /**
     * アラート　ネガティブ
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
        // Log.i(">> $TAG_SHORT", "onSimpleDialogNegativeClick requestCode: $requestCode")
    }

    /**
     * UI設定
     */
    private fun initLayout() {
        // UI
        loading = LoadingNormal.newInstance( message = "", isProgress = true )

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
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:クチコミ_写真")
            val index = view.id
            val intent = Intent(this, ActivitySpotReviewGallery_ImageSearch::class.java)
            intent.putExtra("selectIndex", index)
            intent.putExtra("allNumber", reviewImagesNumber)
            intent.putExtra("condPage", condReviewImagePage)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("imageListData", imageListData)
            startActivity(intent)
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
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:クチコミ_テキスト")
            val index = view.id
            val intent = Intent(this, ActivitySpotReviewDetail_ImageSearch::class.java)
            intent.putExtra("dataSpot", dataSpot)
            intent.putExtra("dataReview", textListData[index])
            startActivity(intent)
        })

        // 基本情報 //
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        layoutBasic.buttonMap.setOnClickListener {
            // 地図へ
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:地図")
            val intent = Intent(this@ActivitySpotInfo_ImageSearch, ActivitySpotMap::class.java)
            intent.putExtra("dataSpot", dataSpot)
            startActivity(intent)
        }

        layoutBasic.buttonWebDetail.setOnClickListener {
            // WEBへ
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:栃ナビ！サイトを見る")
            startActivity(MyIntent().web_browser(
                MyString().my_http_url_spot_info(dataSpot!!.id)))
        }

        basicAdapter = ListSpotInfoBasicAdapter(mContext!!, basicListData)
        layoutBasic.listView.apply {
            adapter = basicAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val item = basicListData[pos]
                when (item.type) {
                    Constants.SPOT_BASIC_INFO_TYPE.address -> {
                        // 地図へ
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:住所")
                        val intent = Intent(this@ActivitySpotInfo_ImageSearch, ActivitySpotMap::class.java)
                        intent.putExtra("dataSpot", dataSpot)
                        startActivity(intent)
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                        // 電話
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:電話")
                        showPhoneAlert()
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.coupon -> {
                        // クーポンへ
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:クーポン")
                        startActivity(MyIntent().web_browser(
                            MyString().my_http_url_coupon(dataSpot!!.id)))
                    }
                    else -> {

                    }
                }
            }
        }

        // お店のシェア //
        layoutShare.textViewTitle.text = "お店の情報をシェアする"
        layoutShare.textViewCopyTitle.text = "URLをコピーする"
        layoutShare.textViewCopy.text = ""
        layoutShare.textViewCopy.setOnClickListener {
            // クリップボードにコピー
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "alt:店舗情報コピー")
            functions!!.clipboardText(this, (it as TextView).text.toString())

            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "お店の情報をコピーしました。\n貼り付けることができます。",
                msg = null,
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)

        }
        layoutShare.buttonMail.setLeftIcon(R.drawable.img_share_mail)
        layoutShare.buttonMail.setOnClickListener {
            // メールのシェア
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "メール")
            startActivity(
                MyIntent().mail(dataSpot!!.snsShareTextLong))
        }
        layoutShare.buttonLine.setLeftIcon(R.drawable.img_share_line)
        layoutShare.buttonLine.setOnClickListener {
            // ラインのシェア
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "LINE")
            MyIntent().line(dataSpot!!.snsShareText, {
                startActivity(it)
            }, {
                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = null,
                    msg = "お店の情報をシェアするには\nLINEのインストールが必要です",
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
        }

        // アクションフッター //
        layoutActions.viewReview.setOnClickListener {
            // クチコミ投稿へ
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            // Firebase
            if (mySP!!.get_status_login()) {
                doInputReview()
            } else {
                // ログインしてください
                showNoLoginAlert(LoginType.review)
            }
        }
        layoutActions.viewFavorite.setOnClickListener {
            // お気に入り
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }

            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "IS:お気に入り")

            if (mySP!!.get_status_login()) {
                val message = if (dataSpot!!.bookMarkEnable)
                    "お気に入りを解除しますか？" else "お気に入りに登録しますか？"

                val positive_label = if (dataSpot!!.bookMarkEnable)
                    "解除する" else "登録する"

                val alert = AlertNormal.newInstance(
                    requestCode = REQUEST_ALERT_FAVORITE,
                    title = null,
                    msg = message,
                    positiveLabel = positive_label,
                    negativeLabel = "キャンセル"
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            } else {

                // ログインしてください
                showNoLoginAlert(LoginType.favorite)
            }


        }
        layoutActions.viewCheckin.setOnClickListener {
            // チェックイン
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }

            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "チェックイン")

            if (mySP!!.get_status_login()) {
                // チェックイン
                if (dataSpot!!.checkinEnable) {
                    // できる
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_CHECKIN,
                        title = "チェックインしますか？",
                        msg = null,
                        positiveLabel = "チェックイン",
                        negativeLabel = "キャンセル"
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                } else {
                    // できない
                    val alert = AlertNormal.newInstance(
                        requestCode = 0,
                        title = "チェックインできません",
                        msg = "※同じスポットで\n連続でチェックインしている\nまたは範囲外のスポットです",
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
            } else {
                // ログインへ誘導
                showNoLoginAlert(LoginType.checkin)
            }

        }
        layoutActions.viewMap.setOnClickListener {
            // 地図
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "地図をみる")
            val intent = Intent(this@ActivitySpotInfo_ImageSearch, ActivitySpotMap::class.java)
            intent.putExtra("dataSpot", dataSpot)
            startActivity(intent)

        }
        layoutActions.viewPhone.setOnClickListener {
            // 電話
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "電話する")
            showPhoneAlert()
        }
    }

    /**
     * 電話をしますかアラート
     */
    private fun showPhoneAlert() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PHONE,
            title = dataSpot!!.name,
            msg = "%sに電話をする".format(dataSpot!!.phone),
            positiveLabel = "電話をする",
            negativeLabel = "キャンセル"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    /**
     * ログインしますかアラート
     */
    private fun showNoLoginAlert(type: LoginType) {
        var title = ""
        when (type) {
            LoginType.checkin -> {
                title = "チェックインをする"
            }
            LoginType.favorite -> {
                title = "お気に入り"
            }
            LoginType.review -> {
                title = "クチコミ投稿"
            }
        }

        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_NO_LOGIN,
            title = title,
            msg = "ご利用するにはログインが必要です",
            positiveLabel = "ログイン",
            negativeLabel = "キャンセル"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        HttpSpotInfo(mContext!!).init_map_position(googleMap)
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
                        successLocation()
                    } else {
                        // last location is null
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
                    errorLocation()
                }
            })
    }

    /**
     * getLocationで位置取得できない時、さらに位置情報取得
     * ※位置情報サービスOFF -> ON に変更した時になる
     */
    private fun getLocationHighQuality() {
        // 取得が長いのでアラート
        if (targetLocationType == LocationType.checkin) {
            loading!!.updateLayout("しばらくお待ちください...", true)
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
                doCheckin()
            }
        }
        // リセット
        targetLocationType = LocationType.getAll
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {

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
                loading!!.onDismiss()
                // アラート表示
                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = "チェックインできません",
                    msg = "位置情報サービスをONにしてください",
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            }
        }
        // リセット
        targetLocationType = LocationType.getAll

    }

    /**
     * クチコミ画像を取得
     */

    private fun getReviewImages() {
        HttpSpotInfo(mContext!!).get_review_images(
            dataSpot!!,
            condReviewImagePage,
            { datas, all_number ->
                reviewImagesNumber = all_number
                imageListData.addAll(datas)
                imageAdapter!!.notifyDataSetChanged()

                if (condReviewImagePage == 1) {
                    // スケルトンビュー
                    (rcvReviewImage.background as TransitionDrawable).startTransition(1000)
                    getReviewTexts()
                }
                condReviewImagePage++
            },
            {
                if (condReviewImagePage == 1) {
                    when (it) {
                        Constants.HTTP_STATUS.nodata -> {
                            // ギャラリーのデータなし
                            viewReviewImageArea.visibility = View.GONE
                            getReviewTexts()
                        }
                        Constants.HTTP_STATUS.network -> {
                            val alert = AlertNormal.newInstance(
                                requestCode = REQUEST_ALERT_NO_DATA,
                                title = "クチコミを取得できません",
                                msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。",
                                positiveLabel = "OK",
                                negativeLabel = null
                            )
                            alert.show(supportFragmentManager, AlertNormal.TAG)
                        }
                    }
                }
            })
    }

    /**
     * クチコミテキストを取得
     */
    private fun getReviewTexts() {
        HttpSpotInfo(mContext!!).get_review_texts(
            dataSpot!!,
            condReviewTextPage,
            {
                textListData.addAll(it)
                textAdapter!!.notifyDataSetChanged()

                if (condReviewTextPage == 1) {
                    // スケルトンビュー
                    (rcvReviewText.background as TransitionDrawable).startTransition(500)
                    getSpotData()
                }
                condReviewTextPage++
            },
            {
                if (condReviewTextPage == 1) {
                    when (it) {
                        Constants.HTTP_STATUS.nodata -> {
                            // クチコミテキストのデータなし
                            viewReviewTextArea.visibility = View.GONE
                            getSpotData()
                        }
                        Constants.HTTP_STATUS.network -> {
                            val alert = AlertNormal.newInstance(
                                requestCode = REQUEST_ALERT_NO_DATA,
                                title = "クチコミを取得できません",
                                msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。",
                                positiveLabel = "OK",
                                negativeLabel = null
                            )
                            alert.show(supportFragmentManager, AlertNormal.TAG)
                        }
                    }
                }
            })
    }

    /**
     * スポットデータの取得
     */
    private fun getSpotData() {
        HttpSpotInfo(mContext!!).get_spot_info(
            dataSpot!!.id,
            mLocation,
            {
                isGetSpotInfo = true
                dataSpot = it
                setLayoutDataSpot()
            },
            {
                var msg: String? = null
                if (it == Constants.HTTP_STATUS.network) {
                    msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                }
                val alert = AlertNormal.newInstance(
                    requestCode = REQUEST_ALERT_NO_DATA,
                    title = "お店の情報を取得できません",
                    msg = msg,
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
    }

    /**
     * dataSpotで設定できるレイアウト
     */
    private fun setLayoutDataSpot() {
        if (dataSpot != null) {
            // 地図更新
            HttpSpotInfo(mContext!!).update_map_position(
                mGoogleMap,
                LatLng(dataSpot!!.latitude, dataSpot!!.longitude),
                dataSpot!!.category_large_id
            )

            // 基本情報
            basicListData.clear()
            basicListData.addAll(
                HttpSpotInfo(mContext!!).get_is_basic_list_data(dataSpot!!))
            basicAdapter!!.notifyDataSetChanged()

            // シェア
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

    /**
     * チェックインする
     */
    private fun doCheckin() {
        HttpSpotInfo(mContext!!).do_checkin(
            dataSpot!!.id,
            mLocation,
            {
                loading!!.onDismiss()
                if (!it.isEmpty()) {
                    // 獲得した称号を表示
                    val modal = ModalGetBadge.newInstance(it)
                    modal.show(supportFragmentManager, ModalGetBadge.TAG)
                } else {
                    // 完了メッセージ
                    val alert = AlertNormal.newInstance(
                        requestCode = 0,
                        title = "チェックインを完了しました!",
                        msg = "※同じスポットのチェックインは1日1回です",
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
                // スポットを更新
                getSpotData()
            },
            {
                loading!!.onDismiss()
                var msg: String? = null
                if (it == Constants.HTTP_STATUS.network) {
                    msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                }
                // ※元の方ではスポットページの更新してた
                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = "チェックインできません",
                    msg = msg,
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
    }

    /**
     * お気に入り
     */
    private fun doFavorite() {
        HttpSpotInfo(mContext!!).do_favorite(
            dataSpot!!.id,
            {
                loading!!.onDismiss(800)
                // スポットを更新
                getSpotData()
            },
            {
                loading!!.onDismiss(800)
                var msg: String? = null
                if (it == Constants.HTTP_STATUS.network) {
                    msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。"
                }
                // ※元の方ではスポットページの更新してた
                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = "お気に入りを更新できません",
                    msg = msg,
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
    }

    /**
     * クチコミをする
     */
    private fun doInputReview() {
        firebase.sendEvent(
            FirebaseHelper.screenName.IS_Spot_Info,
            FirebaseHelper.eventCategory.Button,
            FirebaseHelper.eventAction.Tap,
            "IS:クチコミ")

        HttpSpotInfo(mContext!!).check_input_review(
            {
                // クチコミ投稿へ
                val intent = Intent(this, ActivityInputReview::class.java)
                intent.putExtra("dataSpot", dataSpot)
                // intent.putExtra("type", "all")
                startActivity(intent)
            },
            {
                when (it) {
                    Constants.HTTP_STATUS.nodata -> {
                        // 入力を促す
                        val alert = AlertNormal.newInstance(
                            requestCode = REQUEST_ALERT_MEMBER_MORE_ENTRY,
                            title = "クチコミ投稿できません",
                            msg = "クチコミを投稿するには、「性別、誕生日」を登録する必要があります。\n登録を行いますか？",
                            positiveLabel = "登録する",
                            negativeLabel = "キャンセル"
                        )
                        alert.show(supportFragmentManager, AlertNormal.TAG)
                    }
                    Constants.HTTP_STATUS.network -> {
                        val alert = AlertNormal.newInstance(
                            requestCode = 0,
                            title = "クチコミ投稿できません",
                            msg = "サーバーと通信できませんでした。しばらく時間を置いてからページを開いてください。",
                            positiveLabel = "OK",
                            negativeLabel = null
                        )
                        alert.show(supportFragmentManager, AlertNormal.TAG)
                    }
                }
            })
    }

}
