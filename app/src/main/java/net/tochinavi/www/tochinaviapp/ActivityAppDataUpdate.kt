package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_app_data_update.*
import net.tochinavi.www.tochinaviapp.storage.*
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.convertString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import org.json.JSONArray
import org.json.JSONObject


class ActivityAppDataUpdate : AppCompatActivity() {

    companion object {
        val TAG = "ActivityAppDataUpdate"
    }

    // 変数 //
    private var mContext: Context? = null
    private var fromTag: String = ""

    // テーブルのデータ
    private var category_modified_date: String = ""
    private var area_modified_date: String = ""
    private var data_category1: ArrayList<MutableMap<String, Any>> = ArrayList()
    private var data_category2: ArrayList<MutableMap<String, Any>> = ArrayList()
    private var data_category3: ArrayList<MutableMap<String, Any>> = ArrayList()
    private var data_area1: ArrayList<MutableMap<String, Any>> = ArrayList()
    private var data_area2: ArrayList<MutableMap<String, Any>> = ArrayList()

    // AppData
    private var app_category_date: String = ""
    private var app_area_date: String = ""

    private var mySP: MySharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_data_update)

        mContext = applicationContext
        mySP = MySharedPreferences(this)

        val intent = intent
        ifNotNull(intent.getStringExtra("tag"), {
            fromTag = it
        })

        // categoryとareaの更新日を取得
        val db = DBHelper(mContext!!)
        try {
            val tableAppData = DBTableAppData(mContext!!)
            ifNotNull(tableAppData.getData(db, DBTableAppData.Ids.category_update), {
                app_category_date = it.modified!!.convertString()
            })
            ifNotNull(tableAppData.getData(db, DBTableAppData.Ids.area_update), {
                app_area_date = it.modified!!.convertString()
            })
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }

        // UI
        buttonRetry.setOnClickListener {
            getCategory()
        }
        buttonClose.setOnClickListener {
            goBack()
        }

        showLoading("データのダウンロードしています...")
        getCategory()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {

        if (event!!.action === KeyEvent.ACTION_DOWN) {
            when (event!!.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    // trueにすることで処理を中断(Activityが終了しなくなる)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun showLoading(text: String) {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            textViewMsg.text = text
            buttonRetry.visibility = View.GONE
        }
    }

    fun finishLoading(text: String, retry: Boolean) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            textViewMsg.text = text
            buttonRetry.visibility = if (retry) View.VISIBLE else View.GONE
        }
    }

    /**
     * カテゴリーデータの取得：1番目
     */
    fun getCategory() {
        // ボタンを消す
        buttonRetry.visibility = View.GONE
        buttonClose.visibility = View.GONE

        showLoading("サーバーに接続しています\nカテゴリーデータを取得中．．．")

        val url = MyString().my_http_url_app() + "/app_data/get_category.php?"
        val params = listOf("modified_date" to app_category_date)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {

                    category_modified_date = datas.getString("modified_date")

                    val category1_array: JSONArray = datas.get("category1") as JSONArray
                    for (i in 0..(category1_array.length() - 1)) {
                        val item = category1_array.getJSONObject(i)
                        data_category1.add(mutableMapOf(
                            "id" to item.get("id"),
                            "name" to item.get("name"),
                            "sub_title" to item.get("sub_title")
                        ))
                    }

                    val category2_array: JSONArray = datas.get("category2") as JSONArray
                    for (i in 0..(category2_array.length() - 1)) {
                        val item = category2_array.getJSONObject(i)
                        data_category2.add(mutableMapOf(
                            "id" to item.get("id"),
                            "name" to item.get("name"),
                            "parent_id" to item.get("parent_id")
                        ))
                    }

                    val category3_array: JSONArray = datas.get("category3") as JSONArray
                    for (i in 0..(category3_array.length() - 1)) {
                        val item = category3_array.getJSONObject(i)
                        data_category3.add(mutableMapOf(
                            "id" to item.get("id"),
                            "name" to item.get("name"),
                            "parent_id" to item.get("parent_id")
                        ))
                    }

                }
                getArea()
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                errorUpdate()
            })
        }

    }

    /**
     * エリアーデータの取得：1番目
     */
    fun getArea() {
        showLoading("サーバーに接続しています\nエリアデータを取得中．．．")

        val url = MyString().my_http_url_app() + "/app_data/get_area.php"
        val params = listOf("modified_date" to app_area_date)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {

                    area_modified_date = datas.getString("modified_date")

                    val area1_array: JSONArray = datas.get("area1") as JSONArray
                    for (i in 0..(area1_array.length() - 1)) {
                        val item = area1_array.getJSONObject(i)
                        data_area1.add(mutableMapOf(
                            "id" to item.get("id"),
                            "name" to item.get("name")
                        ))
                    }

                    val area2_array: JSONArray = datas.get("area2") as JSONArray
                    for (i in 0..(area2_array.length() - 1)) {
                        val item = area2_array.getJSONObject(i)
                        data_area2.add(mutableMapOf(
                            "id" to item.get("id"),
                            "name" to item.get("name"),
                            "parent_id" to item.get("parent_id")
                        ))
                    }

                }

                updateTable()

            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                errorUpdate()
            })
        }

    }

    /**
     * データの取得に失敗したら
     */
    fun errorUpdate() {
        val db = DBHelper(mContext!!)
        try {
            // カテゴリーまたはエリアでデータがゼロの場合はトップに行かないようにする
            if (
                DBTableCategory1(mContext!!).count(db) == 0 ||
                DBTableCategory2(mContext!!).count(db) == 0 ||
                DBTableCategory3(mContext!!).count(db) == 0 ||
                DBTableArea1(mContext!!).count(db) == 0 ||
                DBTableArea2(mContext!!).count(db) == 0
            ) {
                buttonClose.visibility = View.GONE
            } else {
                buttonClose.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }

        finishLoading(
            "ネットワークに接続することができません。ネットワークの接続状況を確認してください。",
            true
        )
    }

    /**
     * データの取得が成功したら
     */
    fun updateTable() {
        var updated = false
        val db = DBHelper(mContext!!)
        try {
            if (!category_modified_date.isEmpty()) {
                updated = true
                DBTableAppData(mContext!!).updateModified(db, DBTableAppData.Ids.category_update)
            }
            if (!area_modified_date.isEmpty()) {
                updated = true
                DBTableAppData(mContext!!).updateModified(db, DBTableAppData.Ids.area_update)
            }
            if (data_category1.size > 0) {
                DBTableCategory1(mContext!!).setData(db, data_category1)
            }
            if (data_category2.size > 0) {
                DBTableCategory2(mContext!!).setData(db, data_category2)
            }
            if (data_category3.size > 0) {
                DBTableCategory3(mContext!!).setData(db, data_category3)
            }
            if (data_area1.size > 0) {
                DBTableArea1(mContext!!).setData(db, data_area1)
            }
            if (data_area2.size > 0) {
                DBTableArea2(mContext!!).setData(db, data_area2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
            val count = mySP!!.get(MySharedPreferences.Keys.init_description_count) as Int
            mySP!!.put(MySharedPreferences.Keys.init_description_count, count + 1)
            // アップデートがなければ２秒遅延
            if (!updated) {
                Handler().postDelayed(Runnable {
                    goBack()
                }, 2000)
            } else {
                goBack()
            }
        }
    }

    fun goBack() {
        when (fromTag) {
            // 前のページに戻る
            FragmentTop.TAG -> {
                finish()
            }
            // MainActivityへ
            ActivityInitDescription.TAG -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                finish()
            }
        }
    }

}
