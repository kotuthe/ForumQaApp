package net.tochinavi.www.tochinaviapp.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class TaskDownloadImage(context: Context) : AsyncTask<String?, Void?, Uri?>() {
    private var listener: Listener? = null
    private val mContext: Context = context

    // 非同期処理
    override fun doInBackground(vararg params: String?): Uri? {
        val uri = params[0]!!
        val bitmap = downloadImage(uri) ?: return null
        val ext = File(uri).absoluteFile.extension
        Log.i(">> TaskDownloadImage", "doInBackground: 拡張子 $ext")
        return insertCacheDir(bitmap, ext)
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
    override fun onPostExecute(uri: Uri?) {
        if (listener != null) {
            listener!!.onSuccess(uri)
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onSuccess(uri: Uri?)
    }

    private fun downloadImage(address: String): Bitmap? {
        var bmp: Bitmap? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(address)

            // HttpURLConnection インスタンス生成
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
                        bmp = BitmapFactory.decodeStream(`is`)
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
        } finally {
            urlConnection?.disconnect()
        }
        return bmp
    }

    private fun insertCacheDir(bitmap: Bitmap, ext: String): Uri? {
        var path = ""
        try {
            // アプリ内キャッシュディレクトリ（キャッシュクリアやアプリ削除で消えるらしい）
            val root: File = mContext.cacheDir
            val fileName =
                System.currentTimeMillis().toString() + "." + ext
            var fos: FileOutputStream?
            var format = Bitmap.CompressFormat.JPEG
            if (ext == "png" || ext == "PNG") {
                format = Bitmap.CompressFormat.PNG
            }
            fos = FileOutputStream(File(root, fileName))
            bitmap.compress(format, 100, fos)
            fos.close()
            path = root.path + "/" + fileName
        } catch (e: java.lang.Exception) {
            Log.e("Error", "" + e.toString())
        } finally {
        }

        if (path.isEmpty()) {
            return null
        }

        return Uri.parse("file://%s".format(path))
    }
}