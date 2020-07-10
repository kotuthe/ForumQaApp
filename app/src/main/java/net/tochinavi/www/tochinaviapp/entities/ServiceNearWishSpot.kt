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
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
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
 * ここでは位置情報の許可は聞かない
 * 許可されてなければ通知はしない
 */
class ServiceNearWishSpot
    : IntentService(TAG) {

    companion object {
        // 定数 //
        const val TAG = "ServiceNearWishSpot"
        const val TAG_SHORT = "SNWishSpot"
    }

    // 設定値
    private val NOTIFICATION_MARGIN_TIME = (2 * 60 * 60 * 1000).toLong() // 通知の間隔(ミリ秒)
    private val NOTIFICATION_RANGE = 100.0 // 検索範囲(m)

    private lateinit var mContext: Context
    private lateinit var mySP: MySharedPreferences

    // Location 位置情報 //
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null

    // データ //
    private var dataSpotArray: MutableList<DataNotificationNearWish> = arrayListOf()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // startServiceで呼び出される
        mContext = applicationContext
        mySP = MySharedPreferences(mContext)

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
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    /**
     * Permissionのチェック
     */
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // アプリの権限が許可してる
            getLocation()
        }
    }

    /**
     * 位置情報を取得　Permissionで許可された時
     */
    private fun getLocation() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 省エネを優先(1分に1回の間隔で最速10秒で100mの移動があったとき更新)
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        request.interval = 60 * 1000 // 更新間隔(ms)
        request.fastestInterval = 10 * 1000 // 最短更新間隔(ms)
        request.smallestDisplacement = 100f // 最小移動距離(m)

        // 単発
        mLocationClient!!.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                mLocation = task.result
                /*
                Log.i(">> $TAG_SHORT", "getLatLon: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                Toast.makeText(applicationContext, "st:${mLocation!!.latitude}, ${mLocation!!.longitude}", Toast.LENGTH_LONG).show()
                */
                getWishData()
            }

            // 繰り返し
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    mLocation = result.lastLocation
                    /*
                    Log.i(">> $TAG_SHORT", "getLatLon: ${mLocation!!.latitude}, ${mLocation!!.longitude}")
                    Toast.makeText(applicationContext, "re:${mLocation!!.latitude}, ${mLocation!!.longitude}", Toast.LENGTH_LONG).show()
                    */
                    getWishData()
                }
            }
            mLocationClient!!.requestLocationUpdates(request, mLocationCallback, null)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        // Activityからのintentを受け取る
    }

    /**
     * お気に入りを取得
     */
    private fun getWishData() {
        val db: DBHelper = DBHelper(mContext)
        try {
            dataSpotArray.clear()
            dataSpotArray = DBTableNotificationNearWish(mContext).getAll(db)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "error occured!! cause : " + e.message
            )
        } finally {
            db.cleanup()
            if (dataSpotArray.size > 0) {
                crossLocationBorder()
            }
        }
    }

    /**
     * 通知範囲内であれば通知
     */
    private fun crossLocationBorder() {
        if (mLocation == null) return
        // Toast.makeText(this, "位置情報を更新", Toast.LENGTH_LONG).show();
        for (i in dataSpotArray.indices) {
            val data = dataSpotArray[i]
            var notification = false
            val diff = distanceToDestination(
                mLocation!!.latitude,
                mLocation!!.longitude,
                data.latitude,
                data.longitude
            )

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

    private fun viewNotification(data: DataNotificationNearWish) {
        // 通知の間隔を設定 //
        val dateNow = Date()
        var dateData = Date()
        var enable = false
        if (!data.time.isEmpty()) {
            /*try {
                dateData = data.time.convertDate()!!
            } catch (e: ParseException) {
                e.printStackTrace()
            }*/

            ifNotNull(data.time.convertDate(), {
                dateData = it
            })
            val dataTime = dateData.time
            if (dateNow.time - dataTime >= NOTIFICATION_MARGIN_TIME) {
                // 通知
                enable = true
            }
        } else {
            enable = true
        }
        if (!enable) return

        // 通知するものは時間を更新(データベース) //
        val db = DBHelper(mContext)
        try {
            db.beginTransaction()
            DBTableNotificationNearWish(mContext).updateTime(
                db, data.id.toString(), data.type.toString(), dateNow.convertString())
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            // 登録に失敗 //
            Log.e(TAG, "" + e.message)
        } finally {
            // 登録完了 //
            db.endTransaction()
            db.cleanup()

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
            intent.putExtra("name", data.name)
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
            val notificationId: Int = "%d%d".format(data.type, data.id).toInt()
            val notificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, mBuilder.build())
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
            return if (hm_opens[0] != null)
                checkEachOpenTime(hm_opens) else true
        }
        val now_week = getNowServerWeekDay()
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
                    if (hm_opens[0] != null)
                        checkEachOpenTime(hm_opens) else true
                }
            } else {
                // 基本営業時間の指定をチェック
                if (hm_opens[0] != null) checkEachOpenTime(hm_opens) else true
            }
        } else {
            // 基本営業時間の指定をチェック
            if (hm_opens[0] != null) checkEachOpenTime(hm_opens) else true
        }
    }

    /** サーバーデータは月曜日1~, androidは日曜日1~なのでサーバー用の現在の曜日を取得する  */
    private fun getNowServerWeekDay(): Int {
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
        latitude1: Double,
        longitude1: Double,
        latitude2: Double,
        longitude2: Double
    ): Double {
        // ラジアン値に変換
        val lat1 = latitude1 * (Math.PI / 180)
        val lon1 = longitude1 * (Math.PI / 180)
        val lat2 = latitude2 * (Math.PI / 180)
        val lon2 = longitude2 * (Math.PI / 180)
        val r = 6378.137 * 1000
        val dif_lon = lon2 - lon1
        return r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(dif_lon))
    }

}