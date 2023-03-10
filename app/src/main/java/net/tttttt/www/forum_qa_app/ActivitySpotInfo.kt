package net.tttttt.www.forum_qa_app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotInfoBinding
/*
import kotlinx.android.synthetic.main.activity_spot_info.*
import kotlinx.android.synthetic.main.activity_spot_info.buttonMap
import kotlinx.android.synthetic.main.activity_spot_info.viewAdvtFooter
import kotlinx.android.synthetic.main.fragment_spot_neighbor_list.*
import kotlinx.android.synthetic.main.view_info_share.view.*
import kotlinx.android.synthetic.main.view_info_share.view.textViewTitle
import kotlinx.android.synthetic.main.view_info_spot_basic.view.buttonMap
import kotlinx.android.synthetic.main.view_info_spot_basic.view.listView
import kotlinx.android.synthetic.main.view_info_spot_basic_old.view.*
import kotlinx.android.synthetic.main.view_info_spot_review_old.view.*
*/
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotInfoBasic
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.network.HttpSpotInfo
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.*

class ActivitySpotInfo :
    AppCompatActivity(),
    OnMapReadyCallback,
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotInfo"
        val TAG_SHORT = "ActivitySpotInfo"
    }

    private lateinit var binding: ActivitySpotInfoBinding

    // 位置情報サービスの使用用途
    enum class LocationType {
        getAll,
        getSpot,
        checkin,
    }

    enum class LoginType {
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
    private lateinit var functions: Functions
    private lateinit var mContext: Context
    private lateinit var firebase: FirebaseHelper
    private lateinit var mySP: MySharedPreferences
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var mGoogleMap: GoogleMap
    private var reviewNumber: Int = 0
    private var isGetSpotInfo = false

    // 位置情報
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null
    private var targetLocationType: LocationType = LocationType.getAll

    // ビュー
    private var reviewAdapter: BaseAdapter? = null
    private var reviewListData: ArrayList<DataSpotReview> = ArrayList()
    private var basicAdapter: BaseAdapter? = null
    private var basicListData: ArrayList<DataSpotInfoBasic> = ArrayList()

    // 条件

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpotInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        mySP = MySharedPreferences(mContext)
        functions = Functions(mContext)

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
            dataSpot.name = it
        })

        if (supportActionBar != null) {
            supportActionBar!!.title = dataSpot.name
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
        setLocation()

    }

    // オフスクリーン
    override fun onPause() {
        super.onPause()

        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
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
        // Log.i(">> $TAG_SHORT", "onSimpleDialogActionClick requestCode: $requestCode")

        when (requestCode) {
            REQUEST_ALERT_NO_DATA -> {
                // データなしの場合は戻る
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
                loading!!.updateLayout(
                    if (dataSpot.bookMarkEnable)
                        "お気に入りを解除します..." else "お気に入りに登録します...", true
                )
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
                startActivity(MyIntent().phone(dataSpot.phone))
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
        binding.buttonPhone.setSpotInfoTintColor(true)
        binding.buttonMap.setSpotInfoTintColor(true)
        binding.buttonReview.setSpotInfoTopIcon(R.drawable.img_spot_info_btn_review)
        binding.buttonReviewImage.setSpotInfoTopIcon(R.drawable.img_spot_info_btn_camera)
        binding.buttonFavorite.setSpotInfoTopIcon(R.drawable.img_spot_info_btn_favorite)
        binding.buttonPhone.setOnClickListener {
            // 電話
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "電話する")
            showPhoneAlert()
        }

        binding.buttonMap.setOnClickListener {
            // 地図
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "地図をみる")
            val intent = Intent(this@ActivitySpotInfo, ActivitySpotMap::class.java)
            intent.putExtra("dataSpot", dataSpot)
            startActivity(intent)
        }

        // スポットの写真をクリックしたらギャラリー or プレビュー（one image）
        binding.imageViewMoreGallery.setOnClickListener {
            showReviewImageList()
        }
        binding.imageViewSpotImage.setOnClickListener {
            showReviewImageList()
        }

        binding.buttonCheckin.setOnClickListener {
            // チェックイン
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "チェックイン")
            if (mySP.get_status_login()) {
                // チェックイン
                if (dataSpot.checkinEnable) {
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

        binding.buttonReview.setOnClickListener {
            // クチコミ
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "クチコミをする")
            if (mySP.get_status_login()) {
                doInputReview(1)
            } else {
                // ログインしてください
                showNoLoginAlert(LoginType.review)
            }
        }

        binding.buttonReviewImage.setOnClickListener {
            // クチコミ　画像のみ
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "画像を投稿する")
            if (mySP.get_status_login()) {
                doInputReview(2)
            } else {
                // ログインしてください
                showNoLoginAlert(LoginType.review)
            }
        }

        binding.buttonFavorite.setOnClickListener {
            // お気に入り
            if (!isGetSpotInfo) {
                return@setOnClickListener
            }
            firebase.sendEvent(
                FirebaseHelper.screenName.Spot_Info,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "お気に入り")
            if (mySP.get_status_login()) {
                val message = if (dataSpot.bookMarkEnable)
                    "お気に入りを解除しますか？" else "お気に入りに登録しますか？"

                val positive_label = if (dataSpot.bookMarkEnable)
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


        val activity = this

        // 基本情報 //
        binding.layoutBasic.apply {
            textViewTitle.text = "基本情報"
            textViewSpotName.text = dataSpot.name

            val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
            mapFragment.getMapAsync(activity)
            buttonMap.setOnClickListener {
                // 地図へ
                firebase.sendEvent(
                    FirebaseHelper.screenName.Spot_Info,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "alt:地図")
                val intent = Intent(this@ActivitySpotInfo, ActivitySpotMap::class.java)
                intent.putExtra("dataSpot", dataSpot)
                startActivity(intent)
            }

            basicAdapter = ListSpotInfoBasicOldAdapter(mContext, basicListData)
            listView.apply {
                adapter = basicAdapter
                onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                    val item = basicListData[pos]
                    when (item.type) {
                        Constants.SPOT_BASIC_INFO_TYPE.address -> {
                            // 地図へ
                            firebase.sendEvent(
                                FirebaseHelper.screenName.Spot_Info,
                                FirebaseHelper.eventCategory.Cell,
                                FirebaseHelper.eventAction.Tap,
                                "alt:住所")
                            val intent = Intent(this@ActivitySpotInfo, ActivitySpotMap::class.java)
                            intent.putExtra("dataSpot", dataSpot)
                            startActivity(intent)
                        }
                        Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                            // 電話
                            firebase.sendEvent(
                                FirebaseHelper.screenName.Spot_Info,
                                FirebaseHelper.eventCategory.Cell,
                                FirebaseHelper.eventAction.Tap,
                                "alt:電話番号")
                            showPhoneAlert()
                        }
                        Constants.SPOT_BASIC_INFO_TYPE.coupon -> {
                            // クーポンへ
                            startActivity(MyIntent().web_browser(
                                MyString().my_http_url_coupon(dataSpot.id)))
                        }
                        Constants.SPOT_BASIC_INFO_TYPE.more_detail -> {
                            // 詳細へ
                            val intent = Intent(this@ActivitySpotInfo,
                                ActivitySpotInfoDetail::class.java)
                            intent.putExtra("dataSpot", dataSpot)
                            startActivity(intent)
                        }
                        else -> {

                        }
                    }
                }
            }
        }

        // クチコミ //
        reviewAdapter = ListSpotReviewAdapter(mContext, reviewListData)
        binding.layoutReview.listView.apply {
            adapter = reviewAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val intent = Intent(this@ActivitySpotInfo, ActivitySpotReviewDetail::class.java)
                intent.putExtra("dataSpot", dataSpot)
                intent.putExtra("dataReview", reviewListData[pos])
                startActivityForResult(intent, 0)
            }
        }
        // もっとみる
        binding.layoutReview.layoutMore.setOnClickListener {
            val intent = Intent(this@ActivitySpotInfo,
                ActivitySpotReviewList::class.java)
            intent.putExtra("dataSpot", dataSpot)
            startActivity(intent)
        }

        // お店のシェア //
        binding.layoutShare.apply {
            textViewTitle.text = "お店の情報をシェアする"
            textViewCopyTitle.text = "URLをコピーする"
            textViewCopy.text = ""
            textViewCopy.setOnClickListener {
                // クリップボードにコピー
                firebase.sendEvent(
                    FirebaseHelper.screenName.Spot_Info,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "alt:店舗情報コピー")
                functions.clipboardText(activity, (it as TextView).text.toString())

                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = "お店の情報をコピーしました。\n貼り付けることができます。",
                    msg = null,
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)

            }
            buttonMail.setLeftIcon(R.drawable.img_share_mail)
            buttonMail.setTextColor(ContextCompat.getColor(mContext, R.color.colorLinkBlue))
            buttonMail.setOnClickListener {
                // メールのシェア
                firebase.sendEvent(
                    FirebaseHelper.screenName.Spot_Info,
                    FirebaseHelper.eventCategory.Button,
                    FirebaseHelper.eventAction.Tap,
                    "メール")
                startActivity(
                    MyIntent().mail(dataSpot.snsShareTextLong))
            }
            buttonLine.setLeftIcon(R.drawable.img_share_line)
            buttonLine.setTextColor(ContextCompat.getColor(mContext, R.color.colorLinkBlue))
            buttonLine.setOnClickListener {
                // ラインのシェア
                firebase.sendEvent(
                    FirebaseHelper.screenName.Spot_Info,
                    FirebaseHelper.eventCategory.Button,
                    FirebaseHelper.eventAction.Tap,
                    "LINE")
                MyIntent().line(dataSpot.snsShareText, {
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
        }

        // 広告
        binding.viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppSpotInfo, resources)
    }

    private fun showReviewImageList() {
        firebase.sendEvent(
            FirebaseHelper.screenName.Spot_Info,
            FirebaseHelper.eventCategory.Image,
            FirebaseHelper.eventAction.Tap,
            "alt:店舗画像")
        if (dataSpot.moreImage) {
            // クチコミ写真一覧の表示
            val intent = Intent(this, ActivitySpotReviewImageList::class.java)
            intent.putExtra("dataSpot", dataSpot)
            startActivity(intent)
        } else {
            // 店舗写真の表示
            val modal = ModalUriImagePreview.newInstance(
                arrayListOf(Uri.parse(dataSpot.imageUrl)), 0, dataSpot.name)
            modal.show(supportFragmentManager, ModalUriImagePreview.TAG)
        }
    }

    /**
     * 電話をしますかアラート
     */
    private fun showPhoneAlert() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PHONE,
            title = dataSpot.name,
            msg = "%sに電話をする".format(dataSpot.phone),
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
        HttpSpotInfo(mContext).init_map_position(googleMap)
    }

    /** 位置情報取得 -> スポット取得 or チェックイン **/
    private fun setLocation() {
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

    private fun successLocation() {
        when (targetLocationType) {
            LocationType.getAll -> {
                getSpotData()
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
                getSpotData()
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
     * スポットデータの取得
     */
    private fun getSpotData() {
        HttpSpotInfo(mContext).get_spot_info(
            dataSpot.id,
            mLocation,
            {
                isGetSpotInfo = true
                dataSpot = it
                setLayoutDataSpot()
                getReviewMin()
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
        // 上部 //
        (binding.imageViewCategory.background as TransitionDrawable).startTransition(1000)
        binding.imageViewCategory.setImageDrawable(
            ContextCompat.getDrawable(this, MyImage().icon_category_mini(dataSpot.category_large_id)))
        binding.imageViewSpotImage.load(dataSpot.imageUrl) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        if (!dataSpot.simple_caption.isEmpty()) {
            binding.layoutSpotMessage.visibility = View.VISIBLE
            binding.textViewSpotMessage.text = dataSpot.simple_caption
        } else {
            binding.layoutSpotMessage.visibility = View.GONE
        }
        binding.imageViewMoreGallery.visibility =
            if (dataSpot.moreImage) View.VISIBLE else View.GONE

        binding.buttonCheckin.setSpotInfoTintColor(dataSpot.checkinEnable)
        binding.buttonReview.setSpotInfoTintColor(dataSpot.reviewEnable)
        binding.buttonReviewImage.setSpotInfoTintColor(dataSpot.imageEnable)
        binding.buttonFavorite.setSpotInfoTintColor(true)

        binding.buttonFavorite.text = getString(
            if (dataSpot.bookMarkEnable) R.string.spot_info_favorite else R.string.spot_info_favorite_dis)

        // 基本情報 //
        binding.layoutBasic.textViewTitle.text = "基本情報"
        binding.layoutBasic.textViewSpotName.text = dataSpot.name
        // 地図更新
        HttpSpotInfo(mContext).update_map_position(
            mGoogleMap,
            LatLng(dataSpot.latitude, dataSpot.longitude),
            dataSpot.category_large_id
        )
        basicListData.clear()
        basicListData.addAll(
            HttpSpotInfo(mContext).get_basic_list_data(dataSpot))
        basicAdapter!!.notifyDataSetChanged()

        // シェア
        binding.layoutShare.textViewCopy.text = dataSpot.snsShareText

        // 上部の項目を表示することで下にスライドしなくなる
        (binding.textViewInfo.background as TransitionDrawable).startTransition(500)
        binding.textViewInfo.text = dataSpot.simple_detail
    }

    /**
     * クチコミ画像を取得
     */
    // layoutReview.listView
    private fun getReviewMin() {
        HttpSpotInfo(mContext).get_review_min(
            dataSpot,
            { datas, all_number ->
                reviewNumber = all_number
                reviewListData.clear()
                reviewListData.addAll(datas)
                // レイアウトの更新
                setLayoutDataSpotReview()
            },
            {
                when (it) {
                    Constants.HTTP_STATUS.nodata -> {
                        // ギャラリーのデータなし
                        //binding.layoutReview.visibility = View.GONE
                    }
                    Constants.HTTP_STATUS.network -> {
                        //binding.layoutReview.visibility = View.GONE
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
            })
    }

    /**
     * DataSpotReviewで設定できるレイアウト
     */
    private fun setLayoutDataSpotReview() {
        binding.layoutReview.apply {
            if (reviewListData.size > 0) {
                //visibility = View.VISIBLE
                reviewAdapter!!.notifyDataSetChanged()

                textViewTitle.text = "お店・スポットのクチコミ"
                textViewNumber.text = "%d件".format(reviewNumber)
                layoutMore.visibility =
                    if (reviewNumber >= 6) View.VISIBLE else View.GONE
            } else {
                // will never called??
                //visibility = View.GONE
            }
        }
    }

    /**
     * チェックインする
     */
    private fun doCheckin() {
        HttpSpotInfo(mContext).do_checkin(
            dataSpot.id,
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
        HttpSpotInfo(mContext).do_favorite(
            dataSpot.id,
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
    private fun doInputReview(type: Int) {
        HttpSpotInfo(mContext).check_input_review(
            {
                // クチコミ投稿へ
                val intent = Intent(this, ActivityInputReview::class.java)
                intent.putExtra("dataSpot", dataSpot)
                intent.putExtra("type", type)
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
