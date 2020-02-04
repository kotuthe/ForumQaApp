package net.nocoso.panelapp.network

import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.toolbox.ImageLoader

class VolleyImageCache(isCache: Boolean) : ImageLoader.ImageCache {

    private var sCache: LruCache<String, Bitmap>? = null
    private var mIsCache = false

    init {
        synchronized(sLock) {
            mIsCache = isCache

            if (sCache == null) {
                val maxSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
                val cacheSize = maxSize / 8

                sCache = object : LruCache<String, Bitmap>(cacheSize) {
                    override fun sizeOf(key: String, bitmap: Bitmap): Int {
                        return bitmap.byteCount / 1024
                    }
                }
            }
        }
    }

    override fun getBitmap(url: String): Bitmap? {
        return if (mIsCache) {
            sCache!!.get(url)
        } else {
            null
        }
    }

    override fun putBitmap(url: String, bitmap: Bitmap) {
        if (mIsCache) {
            sCache!!.put(url, bitmap)
        }
    }

    companion object {
        val sLock = Any()
    }
}