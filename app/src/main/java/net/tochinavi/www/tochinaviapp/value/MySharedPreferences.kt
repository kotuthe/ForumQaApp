package net.tochinavi.www.tochinaviapp.value

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MySharedPreferences(context: Context) {
    // 保存先
    private val FILE_NAME = "app_tochinavi_sp"

    // 変数
    enum class Keys(val rawValue: String) {
        // ログインの有無
        status_login("status_login"),
        // 初回説明
        init_description_count("init_description_count"),
        // アプリ起動したときに1度実行される（起動時に初期化される）
        top_location_first_alert("top_location_first_alert"),
        spot_neighbor_location_first_alert("spot_neighbor_location_first_alert"),
        // AppDataの更新時間
        app_data_update_time("app_data_update_time")
    }

    // 広告用
    private val ADVT_KEY = "Advt_Cash_Close_"

    var mContext: Context? = null

    init {
        mContext = context
    }

    /** 通常 **/
    fun put(key: Keys, value: Any) {
        if (mContext == null) { return }
        val prefs = mContext!!.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)
        val sh: SharedPreferences.Editor = prefs.edit()
        when (key) {
            Keys.status_login -> {
                val v: Boolean = value as Boolean
                sh.putBoolean(Keys.status_login.rawValue, v)
            }
            Keys.init_description_count -> {
                val v: Int = value as Int
                sh.putInt(Keys.init_description_count.rawValue, v)
            }
            Keys.top_location_first_alert -> {
                val v: Boolean = value as Boolean
                sh.putBoolean(Keys.top_location_first_alert.rawValue, v)
            }
            Keys.spot_neighbor_location_first_alert -> {
                val v: Boolean = value as Boolean
                sh.putBoolean(Keys.spot_neighbor_location_first_alert.rawValue, v)
            }
            Keys.app_data_update_time -> {
                // 現在時刻をいれる
                if (value == 0) {
                    // 0 not cast long
                    sh.putLong(Keys.app_data_update_time.rawValue, 0)
                } else {
                    val v: Long = value as Long
                    sh.putLong(Keys.app_data_update_time.rawValue, v)
                }
            }
        }
        sh.commit()
    }

    fun get(key: Keys): Any? {
        if (mContext == null) { return null }
        val prefs = mContext!!.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)

        when (key) {
            Keys.status_login -> {
                return prefs.getBoolean(Keys.status_login.rawValue, false)
            }
            Keys.init_description_count -> {
                return prefs.getInt(Keys.init_description_count.rawValue, 0)
            }
            Keys.top_location_first_alert -> {
                return prefs.getBoolean(Keys.top_location_first_alert.rawValue, false)
            }
            Keys.spot_neighbor_location_first_alert -> {
                return prefs.getBoolean(Keys.spot_neighbor_location_first_alert.rawValue, false)
            }
            Keys.app_data_update_time -> {
                return prefs.getLong(Keys.app_data_update_time.rawValue, 0)
            }
        }

        return false
    }

    /** 広告用 **/
    fun put_advt(screenName: String) {
        // 現在時刻をセット
        if (mContext == null) { return }
        val prefs = mContext!!.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)
        val sh: SharedPreferences.Editor = prefs.edit()
        val spName = ADVT_KEY + screenName
        val now = Date(System.currentTimeMillis())
        sh.putLong(spName, now.time)
        sh .commit()
    }

    fun get_advt(screenName: String): Long? {
        if (mContext == null) { return  null}
        val spName = ADVT_KEY + screenName
        val prefs = mContext!!.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)
        return prefs.getLong(spName, 0)
    }

    /**
     * ログイン用：true(ログイン), false(ログアウト)
     */
    fun get_status_login(): Boolean {
        if (mContext == null) { return false }
        val res = this.get(Keys.status_login)
        if (res == null) { return false }
        return res as Boolean
    }

    fun set_status_login(login: Boolean) {
        this.put(Keys.status_login, login)
    }

}