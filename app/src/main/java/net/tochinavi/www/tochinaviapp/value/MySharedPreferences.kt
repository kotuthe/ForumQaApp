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
        init_description_count("init_description_count"),
        top_location_alert_count("top_location_alert_count"),
        top_app_location_alert_count("top_app_location_alert_count")
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
            Keys.init_description_count -> {
                val v: Int = value as Int
                sh.putInt(Keys.init_description_count.rawValue, v)
            }
            Keys.top_location_alert_count -> {
                val v: Int = value as Int
                sh.putInt(Keys.top_location_alert_count.rawValue, v)
            }
            Keys.top_app_location_alert_count -> {
                val v: Int = value as Int
                sh.putInt(Keys.top_app_location_alert_count.rawValue, v)
            }
        }
        sh.commit()
    }

    fun get(key: Keys): Any? {
        if (mContext == null) { return null }
        val prefs = mContext!!.getSharedPreferences(FILE_NAME, AppCompatActivity.MODE_PRIVATE)

        when (key) {
            Keys.init_description_count -> {
                return prefs.getInt(Keys.init_description_count.rawValue, 0)
            }
            Keys.top_location_alert_count -> {
                return prefs.getInt(Keys.top_location_alert_count.rawValue, 0)
            }
            Keys.top_app_location_alert_count -> {
                return prefs.getInt(Keys.top_app_location_alert_count.rawValue, 0)
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
}