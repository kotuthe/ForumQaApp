package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_login.*
import net.tochinavi.www.tochinaviapp.entities.DataUsers
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class ActivityLogin : AppCompatActivity() {

    companion object {
        val TAG = "FragmentMyPage"
    }

    // 変数 //
    private var mContext: Context? = null
    private var fromTag: String = ""
    private var data_users: DataUsers? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = applicationContext

        val intent = intent
        ifNotNull(intent.getStringExtra("tag"), {
            fromTag = it
        })

        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.login_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        hideLoading()

        // パスワード入力
        editPassword.setOnEditorActionListener() { v, actionId, event ->
            when(actionId){
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL, 999 -> {
                    checkInput()
                    true
                }
                else -> false
            }
        }

        // ログインボタン
        buttonLogin.setOnClickListener {
            checkInput()
        }

        // パスワード忘れた
        textViewForgetLink.setOnClickListener {
            val uri: Uri = Uri.parse(MyString().my_http_url_forget_pass())
            val i = Intent(Intent.ACTION_VIEW, uri)
            startActivity(i)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(msg: String) {
        viewLoading.visibility = View.VISIBLE
        textViewLoading.text = msg
    }

    private fun hideLoading() {
        viewLoading.visibility = View.GONE
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun checkInput() {
        actEmail.error = null
        editPassword.error = null

        var cancel = false
        val email = actEmail.text.toString()
        val password = editPassword.text.toString()

        if (email.isEmpty()) {
            cancel = true
            actEmail.error = "必須"
            actEmail.requestFocus()
        } else if (!isEmailValid(email)) {
            cancel = true
            actEmail.error = "正しく入力してください"
            actEmail.requestFocus()
        }

        if (password.isEmpty()) {
            cancel = true
            editPassword.error = "必須"
            editPassword.requestFocus()
        }

        if (!cancel) {
            doLogin(email, password)
        }

    }

    private fun doLogin(email: String, password: String) {
        showLoading("ログインしています...")

        val url = MyString().my_http_url_app() + "/login_auth.php"
        val params = listOf("email" to email, "password" to password)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    val obj = datas.getJSONObject("user")
                    data_users = DataUsers(
                        DBTableUsers.Ids.member_login.rawValue,
                        obj.getInt("id"),
                        email,
                        password,
                        obj.getString("name"),
                        null,
                        true
                    )

                    // 写真取得とDB登録へ
                    val task = myAsyncDLUserIcon()
                    task.execute(obj.getString("image"))
                } else {
                    errorLogin()
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                errorLogin()
            })
        }
    }

    private fun errorLogin() {
        showLoading("認証に失敗しました")
        Handler().postDelayed(Runnable {

            actEmail.error = "IDまたはパスワードが違います"
            actEmail.requestFocus()

            hideLoading()
        }, 800)
    }


    inner class myAsyncDLUserIcon : AsyncTask<String, Int, Void>() {

        var bitmap: Bitmap? = null
        override fun onPreExecute() {
            // 開始
        }

        override fun doInBackground(vararg param: String?): Void? {
            Log.i(">> ", "image path: ${param[0]!!}")
            bitmap = downloadWebImage(param[0]!!, 0, null)
            return null
        }

        override fun onProgressUpdate(vararg values: Int?) {
            // ローディング
        }

        override fun onPostExecute(result: Void?) {
            // 完了
            showLoading("認証しました")

            data_users!!.image = bitmap

            val db = DBHelper(mContext!!)
            try {
                DBTableUsers(mContext!!).setData(db, DBTableUsers.Ids.member_login, data_users!!)
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
                Handler().postDelayed(Runnable {
                    /*
                    when(fromTag) {
                        FragmentMyPage.TAG -> {
                        }
                        else -> {
                        }
                    }
                    */
                    val intent = Intent()
                    setResult(RESULT_OK, intent);
                    finish()
                }, 1000)
            }

        }
    }

    /**
     *
     * Web画像は向きを取得できないらし
     */
    tailrec fun downloadWebImage(path: String, inSampleSize: Int, bitmap: Bitmap?) : Bitmap
            = if (bitmap != null) {
        bitmap
    } else {
        try {
            val url = URL(path)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()
            var bmp_orign: Bitmap? = null
            val _in = conn.inputStream
            if (inSampleSize == 0) {
                // オリジナル
                bmp_orign = BitmapFactory.decodeStream(_in)
            } else {
                // 縮小
                val options = BitmapFactory.Options()
                options.inSampleSize = inSampleSize
                bmp_orign = BitmapFactory.decodeStream(_in, null, options)
            }
            _in.close()
            conn.disconnect()
            downloadWebImage(path, 0, bmp_orign)

        } catch (e: OutOfMemoryError) {
            // メモリ不足時は画像を縮小する(設定値は2のべき乗)
            Log.i(">> ", "downloadWebImage more $inSampleSize")
            downloadWebImage(path, if (inSampleSize == 0) 2 else inSampleSize * 2, null)
        } catch (e: IOException){
            // エラーの時はダミー画像を生成
            val dummy = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            downloadWebImage(path, 0, dummy)
        }
    }
}
