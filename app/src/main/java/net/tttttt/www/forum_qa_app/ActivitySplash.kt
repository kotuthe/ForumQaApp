package net.tttttt.www.forum_qa_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import net.tttttt.www.forum_qa_app.databinding.ActivitySplashBinding
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableAppData
import net.tttttt.www.forum_qa_app.value.MySharedPreferences

class ActivitySplash : Activity() {
    private val TAG: String = "ActivitySplash"

    private lateinit var binding: ActivitySplashBinding
    private val mHandler = Handler()
    private var mContext: Context? = null
    // private var dbHelper: DBHelper? = null
    private var mySP: MySharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val anim = AnimationUtils.loadAnimation(this, R.anim.aplash_img)
        binding.imageLogo.startAnimation(anim)

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
                mHandler.postDelayed(mRunnable, 1000)
        }

    }

    override fun onStop() {
        super.onStop()
        mHandler.removeCallbacks(mRunnable)
    }

    private val mRunnable = Runnable {

        // MainActivityに移行
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

        /*if (mySP!!.get(MySharedPreferences.Keys.init_description_count) as Int == 0) {
            // 初回説明に遷移
            val intent = Intent(this, ActivityInitDescription::class.java)
            startActivity(intent)
            finish()
        } else {
            // MainActivityに移行
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }*/

    }

}