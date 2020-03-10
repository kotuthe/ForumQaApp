package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableAppData
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import java.util.*

class ActivitySplash : Activity() {
    private val TAG: String = "ActivitySplash"
    private val mHandler = Handler()
    private var mContext: Context? = null
    // private var dbHelper: DBHelper? = null
    private var mySP: MySharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // データの初期設定
        mySP = MySharedPreferences(this)
        mySP!!.put(MySharedPreferences.Keys.app_data_update_time, 0)
        mySP!!.put(MySharedPreferences.Keys.top_location_first_alert, false)
        mySP!!.put(MySharedPreferences.Keys.spot_neighbor_location_first_alert, false)

        // ステータスバーの色
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(
            applicationContext,
            R.color.colorTopStatus
        )

        // データベースの初回値の設定
        val db = DBHelper(this)
        try {
            db.beginTransaction()
            DBTableAppData(this).setInitData(db)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        } finally {
            db.endTransaction()
            db.cleanup()
            // Topへ遷移
            mHandler.postDelayed(mRunnable, 800)
        }

    }

    override fun onStop() {
        super.onStop()
        mHandler.removeCallbacks(mRunnable)
    }

    private val mRunnable = Runnable {

        /*
        // テスト　後で直す
        val intent = Intent(this, ActivityInitDescription::class.java)
        startActivity(intent)
        finish()
        */
        if (mySP!!.get(MySharedPreferences.Keys.init_description_count) as Int == 0) {
            // 初回説明に遷移
            val intent = Intent(this, ActivityInitDescription::class.java)
            startActivity(intent)
            finish()
        } else {
            // MainActivityに移行
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}