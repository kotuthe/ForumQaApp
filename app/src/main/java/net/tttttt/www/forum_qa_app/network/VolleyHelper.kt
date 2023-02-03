package net.nocoso.panelapp.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleyHelper private constructor(context: Context) {

    init {
        mCtx = context
        mRequestQueue = requestQueue
    }

    fun addToRequestQueue(req: Request<*>) {
        requestQueue.add(req)
    }

    companion object {
        private var mInstance: VolleyHelper? = null
        private var mRequestQueue: RequestQueue? = null
        private var mCtx: Context? = null
        private var mImageLoader: VolleyImageLoader? = null

        @Synchronized
        fun getInstance(context: Context): VolleyHelper {
            if (mInstance == null) {
                mInstance = VolleyHelper(context)
            }
            return mInstance!!
        }

        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        val requestQueue: RequestQueue
            get() {
                if (mRequestQueue == null) {
                    mRequestQueue = Volley.newRequestQueue(mCtx!!.applicationContext)
                }
                return mRequestQueue!!
            }

        /**
         * リストまたはグリッドなど複数画像取得時のローダーインスタンス作成処理
         *
         * @param conetxt コンテキスト
         * @param bitmapCache キャッシュ容量設定
         * @return ローダーインスタンス
         */
        fun getImageLoader(conetxt: Context, bitmapCache: VolleyImageCache): VolleyImageLoader {
            if (mImageLoader == null) {
                mCtx = conetxt
                mImageLoader = VolleyImageLoader(requestQueue, bitmapCache)
            }

            return mImageLoader!!
        }
    }
}