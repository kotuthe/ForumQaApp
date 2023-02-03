package net.tttttt.www.forum_qa_app.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.util.Log
import net.tttttt.www.forum_qa_app.value.ifNotNull
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * input: Strin Array 複数URL
 * output: AnimationDrawable
 */
class TaskWebImageAnime(context: Context) : AsyncTask<ArrayList<String>?, Void?, AnimationDrawable?>() {
    private var listener: Listener? = null
    private val mContext: Context = context

    // 非同期処理
    override fun doInBackground(vararg params: ArrayList<String>?): AnimationDrawable? {
        val urls = params[0]!!

        // アニメーション
        val anim = AnimationDrawable()
        for (i in 0..urls.size - 1) {
            // Log.i(">> web image", "download: $i")
            ifNotNull(downloadImage(urls[i])) {
                anim.addFrame(BitmapDrawable(mContext.resources, it), 6000)
            }
        }
        anim.setExitFadeDuration(2000)
        anim.setEnterFadeDuration(1500)
        anim.isOneShot = false
        return anim
    }

    // 途中経過をメインスレッドに返す
    override fun onProgressUpdate(vararg progress: Void?) {
        super.onProgressUpdate(*progress)
    }


    /*
    // 非同期処理
    protected override fun doInBackground(vararg params: String): Bitmap? {
        return downloadImage(params[0])
    }


    // 途中経過をメインスレッドに返す
    protected override fun onProgressUpdate(vararg progress: Void) {
        //working cursor を表示させるようにしてもいいでしょう
    }
    */

    // 非同期処理が終了後、結果をメインスレッドに返す
    override fun onPostExecute(anime: AnimationDrawable?) {
        if (listener != null) {
            listener!!.onSuccess(anime)
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onSuccess(anime: AnimationDrawable?)
    }

    private fun downloadImage(address: String, inSampleSize: Int = 0): Bitmap? {
        var bmp: Bitmap? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(address)
            urlConnection = url.openConnection() as HttpURLConnection
            // タイムアウト設定
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 20000
            // リクエストメソッド
            urlConnection.requestMethod = "GET"
            // リダイレクトを自動で許可しない設定
            urlConnection.instanceFollowRedirects = false
            // ヘッダーの設定(複数設定可能)
            urlConnection.setRequestProperty("Accept-Language", "jp")
            // 接続
            urlConnection.connect()
            val resp: Int = urlConnection.responseCode
            when (resp) {
                HttpURLConnection.HTTP_OK -> try {
                    urlConnection.inputStream.use { `is` ->
                        val options: BitmapFactory.Options = BitmapFactory.Options()
                        if (inSampleSize > 0) {
                            options.inSampleSize = inSampleSize
                        }
                        bmp = BitmapFactory.decodeStream(`is`, null, options);
                        `is`.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            Log.d("debug", "downloadImage error")
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            // メモリ不足時は画像を縮小する　
            // Log.i(">> web image", "OutOfMemoryError: $address")
            bmp = downloadImage(
                address, if (inSampleSize == 0) 2 else inSampleSize * 2)
        } finally {
            urlConnection?.disconnect()
        }
        return bmp
    }
}