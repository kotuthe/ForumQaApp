package net.tochinavi.www.tochinaviapp.entities


import android.Manifest
import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import net.tochinavi.www.tochinaviapp.ActivityHospitalInfo
import net.tochinavi.www.tochinaviapp.ActivitySpotInfo
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableNotificationNearWish
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.convertDate
import net.tochinavi.www.tochinaviapp.value.convertString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import java.text.ParseException
import java.util.*

/**
 * Created by kotouno on 2018/01/04.
 *
 * Myページの設定に「通知を許可」を用意してアプリの通知許可設定に飛ばす
 */
class ServiceNearWishSpot
    : IntentService(TAG),
    ConnectionCallbacks,
    OnConnectionFailedListener,
    LocationListener {

    private var mContext: Context? = null
    private lateinit var mySP: MySharedPreferences

    // Location 位置情報 //
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null

    // データ //
    private var dataSpotArray: MutableList<DataNotificationNearWish> =
        ArrayList()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // startServiceで呼び出される
        mContext = applicationContext
        mySP = MySharedPreferences(mContext!!)

        // 現在地を取得
        buildGoogleApiClient()
        createLocationRequest()
        buildLocationSettingsRequest()
        mGoogleApiClient!!.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    override fun onHandleIntent(intent: Intent?) {
        // Activityからのintentを受け取る
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    /*

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    */

    private fun viewNotification(data: DataNotificationNearWish) {
        // 通知の間隔を設定 //
        // val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val dateNow = Date()
        var dateData = Date()
        var enable = false
        if (!data.time.isEmpty()) {
            try {
                // dateData = sdf.parse(data.time)
                dateData = data.time.convertDate()!!
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val dataTime = dateData.time
            // Log.i(TAG, "diff time: " + (dateNow.getTime() - dataTime));
            if (dateNow.time - dataTime >= NOTIFICATION_MARGIN_TIME) {
                // 通知
                enable = true
            }
        } else {
            enable = true
        }
        if (!enable) return

        // Log.i(TAG, "notification!!");

        // 通知するものは時間を更新(データベース) //
        val dbHelper = DBHelper(mContext!!)
        try {
            // start transaction
            dbHelper.beginTransaction()
            DBTableNotificationNearWish(mContext!!).updateTime(
                dbHelper, data.id.toString(), data.type.toString(),
                dateNow.convertString()// sdf.format(dateNow)
            )
            // transaction Successful
            dbHelper.setTransactionSuccessful()
        } catch (e: Exception) {
            // 登録に失敗 //
            Log.e(TAG, "" + e.message)
        } finally {
            // 登録完了 //
            dbHelper.endTransaction()
            dbHelper.cleanup()

            // 通知設定 //
            // 遷移先のスポットページ //
            var intent: Intent? = null
            if (data.type == 1) {
                // スポット情報へ
                intent = Intent(application, ActivitySpotInfo::class.java)
            } else {
                // 病院
                intent = Intent(application, ActivityHospitalInfo::class.java)
            }
            intent.putExtra("id", data.id)
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP // 起動中のアプリがあってもこちらを優先する
                    or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED // 起動中のアプリがあってもこちらを優先する
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            // 第2引数が設定されないと情報が更新されないのでとりあえずspotのidを代入
            val contentIntent = PendingIntent.getActivity(
                application,
                data.id,
                intent,
                0
            )

            // 通知レイアウト //
            val mBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(application, "notification_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("近くのお気に入りスポット")
                .setContentText(data.name)
            // 振動設定
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
            mBuilder.setContentIntent(contentIntent)
            // val notificationId: Int = functions.joinInt(data.type, data.id)
            val notificationId: Int = "%d%d".format(data.type, data.id).toInt()
            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, mBuilder.build())
        }
    }

    private fun crossLocationBorder() {
        if (mCurrentLocation == null) return
        // Toast.makeText(this, "位置情報を更新", Toast.LENGTH_LONG).show();
        for (i in dataSpotArray.indices) {
            val data = dataSpotArray[i]
            var notification = false
            val diff = distanceToDestination(
                mCurrentLocation!!.latitude,
                mCurrentLocation!!.longitude,
                data.latitude,
                data.longitude
            )

            // 本番
            // 範囲内のチェック
            if (diff <= NOTIFICATION_RANGE) {
                // 営業時間内のチェック
                if (checkSpotOpenTime(data)) {
                    notification = true
                }
            }
            if (notification) {
                // 通知設定
                viewNotification(data)
            }
        }
    }

    private fun getWishData() {
        val data: DataNotificationNearWish? = null
        var dBHelper: DBHelper? = null
        try {
            dBHelper = DBHelper(mContext!!)
            dataSpotArray.clear()
            dataSpotArray = DBTableNotificationNearWish(mContext!!).getAll(dBHelper)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "error occured!! cause : " + e.message
            )
        } finally {
            dBHelper?.cleanup()
            if (dataSpotArray.size > 0) {
                crossLocationBorder()
            }
        }
    }

    /**
     * 営業時間内の判定
     * @param data
     * @return
     */
    private fun checkSpotOpenTime(data: DataNotificationNearWish): Boolean {
        var is_shop_holiday = false
        // 「定休日が祝日の場合は営業」にチェックがある
        if (!data.close_ex_info.isEmpty()) {
            for (i in 0 until data.close_ex_info.size) {
                if (data.close_ex_info[i] == 1) {
                    is_shop_holiday = true
                    break
                }
            }
        }
        val hm_opens: HashMap<Int, IntArray> = data.getGroupOpen()
        if (is_shop_holiday) {
            // 基本営業時間の指定をチェック
            return if (hm_opens[0] != null) {
                checkEachOpenTime(hm_opens)
            } else {
                true
            }
        }
        val now_week = nowServerWeekDay
        // 定休日のチェック //
        if (!data.close_info.isEmpty()) {
            for (i in 0 until data.close_info.size) {
                if (data.close_info[i] == now_week) {
                    // 定休日
                    return false
                }
            }
        }

        // 現在の曜日の営業時間を確認
        return if (data.is_open_time_limit) {
            // 曜日にチェックがあるか
            if (!data.sub_is_open_limit.isEmpty()) {
                // 対象の曜日にチェックがあるか
                val hm_sub_opens: HashMap<Int, IntArray> = data.getGroupSub_open(now_week)
                if (hm_sub_opens[0] != null) {
                    // 曜日の営業時間はのチェック //
                    checkEachOpenTime(hm_sub_opens)
                } else {
                    // 基本営業時間の指定をチェック
                    if (hm_opens[0] != null) {
                        checkEachOpenTime(hm_opens)
                    } else {
                        true
                    }
                }
            } else {
                // 基本営業時間の指定をチェック
                if (hm_opens[0] != null) {
                    checkEachOpenTime(hm_opens)
                } else {
                    true
                }
            }
        } else {
            // 基本営業時間の指定をチェック
            if (hm_opens[0] != null) {
                checkEachOpenTime(hm_opens)
            } else {
                true
            }
        }
    }

    /** サーバーデータは月曜日1~, androidは日曜日1~なのでサーバー用の現在の曜日を取得する  */
    private val nowServerWeekDay: Int
        private get() {
            val cal = Calendar.getInstance()
            val now_week = cal[Calendar.DAY_OF_WEEK]
            var rVal = 0
            rVal = when (now_week) {
                1 -> 7
                else -> now_week - 1
            }
            return rVal
        }

    /** 各時間のチェック  */
    // ここでえらってる
    private fun checkEachOpenTime(hm_time_data: HashMap<Int, IntArray>): Boolean {
        val nowDate = Calendar.getInstance()
        var index = 0

        for (i in 0..1) {
            if (hm_time_data[index] == null) {
                break
            }

            val start = Calendar.getInstance()

            start[
                    nowDate[Calendar.YEAR],
                    nowDate[Calendar.MONTH],
                    nowDate[Calendar.DATE],
                    if (hm_time_data[index]!!.size > 0) hm_time_data[index]!![0] else 0,
                    if (hm_time_data[index]!!.size > 0) hm_time_data[index]!![1] else 0
            ] = 0

            val end = Calendar.getInstance()
            end[
                    nowDate[Calendar.YEAR],
                    nowDate[Calendar.MONTH],
                    nowDate[Calendar.DATE],
                    if (hm_time_data[index + 1]!!.size > 0) hm_time_data[index + 1]!![0] else 0,
                    if (hm_time_data[index + 1]!!.size > 0) hm_time_data[index + 1]!![1] else 0
            ] = 59

            // start < now_date < end
            if (0 < nowDate.compareTo(start) && nowDate.compareTo(end) < 0) {
                return true
            }
            index += 2
        }
        return false
    }

    /**
     * 緯度経度の2点間距離の計算
     * http://log.nissuk.info/2012/04/2.html
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return メートル単位
     */
    private fun distanceToDestination(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // ラジアン値に変換
        var lat1 = lat1
        var lon1 = lon1
        var lat2 = lat2
        var lon2 = lon2
        lat1 = lat1 * (Math.PI / 180)
        lon1 = lon1 * (Math.PI / 180)
        lat2 = lat2 * (Math.PI / 180)
        lon2 = lon2 * (Math.PI / 180)
        val r = 6378.137 * 1000
        val dif_lon = lon2 - lon1
        return r * Math.acos(
            Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(
                lat2
            ) * Math.cos(dif_lon)
        )
    }

    /**
     * Builds a GoogleApiClient. Uses the `#addApi` method to request the
     * LocationServices API.
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(mContext!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        // 省エネを優先(1分に1回の間隔で最速10秒で100mの移動があったとき更新)
        mLocationRequest = LocationRequest()
        // mLocationRequest.setInterval(10000);
        mLocationRequest!!.interval = 60000
        mLocationRequest!!.fastestInterval = 10000
        mLocationRequest!!.smallestDisplacement = 100f
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    protected fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    override fun onConnected(bundle: Bundle?) {
        if (mContext == null) return
        val result =
            LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                LocationSettingsRequest.Builder().setAlwaysShow(true)
                    .addLocationRequest(mLocationRequest!!).build()
            )
        result.setResultCallback { locationSettingsResult ->
            val status =
                locationSettingsResult.status
            if (status.statusCode == LocationSettingsStatusCodes.SUCCESS) {

                // 位置情報の取得を開始
                if (isLocationEnabled(mContext!!)) {
                    if (mGoogleApiClient!!.isConnected) {
                        startLocationUpdates()
                    }
                }
            } else {
                // Log.i(TAG, "位置情報が利用できないので通知はキャンセル");
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        // Log.i(TAG_SHORT, "onConnectionSuspended i" + i);
    }

    override fun onLocationChanged(location: Location) {
        if (!mySP.get_status_login()) {
            return
        }
        if (mContext == null) return
        if (ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (mGoogleApiClient == null) return
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            getWishData()

            // Log.i(TAG, "change location: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        } else {
            // Log.i(TAG, "位置情報が利用できないので通知はキャンセル");
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        TODO("Not yet implemented")
    }

    /**
     * Requests location updates from the FusedLocationApi.
     * http://qiita.com/daisy1754/items/aa9ad75d1a84b745469b
     */
    protected fun startLocationUpdates() {
        if (mContext == null) return
        if (ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (!mGoogleApiClient!!.isConnected) { return }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
            ).setResultCallback { status ->
                if (status.isSuccess) {
                    // Log.i(TAG_SHORT, "App Indexing API: Recorded recipe view end successfully."+ status.toString());
                } else {
                    // Log.i(TAG_SHORT, "App Indexing API: There was an error recording the recipe view." + status.toString());
                }
            }
        } else {
            // Log.i(TAG, "位置情報が利用できないので通知はキャンセル");
        }
    }

    companion object {
        // 定数 //
        const val TAG = "ServiceNearWishSpot"

        // 設定値
        private const val NOTIFICATION_MARGIN_TIME = 2 * 60 * 60 * 1000 // 通知の間隔(ミリ秒)
            .toLong()
        private const val NOTIFICATION_RANGE = 500.0 // 100.0 // 検索範囲(m)
        fun isLocationEnabled(context: Context): Boolean {
            var locationMode = 0
            val locationProviders: String
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    locationMode = Settings.Secure.getInt(
                        context.contentResolver,
                        Settings.Secure.LOCATION_MODE
                    )
                } catch (e: SettingNotFoundException) {
                    e.printStackTrace()
                }
                locationMode != Settings.Secure.LOCATION_MODE_OFF
            } else {
                locationProviders = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
                )
                !TextUtils.isEmpty(locationProviders)
            }
        }
    }
}