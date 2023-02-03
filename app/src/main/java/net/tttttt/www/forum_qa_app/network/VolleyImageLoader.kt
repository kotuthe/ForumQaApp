package net.nocoso.panelapp.network

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader

class VolleyImageLoader(queue: RequestQueue, imageCache: ImageLoader.ImageCache) : ImageLoader(queue, imageCache) {
    companion object {


        fun getImageListener(
            view: ImageView, defaultImageResId: Int, errorImageResId: Int
        ): ImageLoader.ImageListener {
            return object : ImageLoader.ImageListener {
                override fun onResponse(response: ImageLoader.ImageContainer, isImmediate: Boolean) {

                    if (response.bitmap != null) {
                        view.setImageBitmap(response.bitmap)
                    } else if (defaultImageResId != 0) {
                        view.setImageResource(defaultImageResId)
                    }
                }

                override fun onErrorResponse(error: VolleyError) {
                    if (errorImageResId != 0) {
                        view.setImageResource(errorImageResId)
                    }
                }
            }
        }


        fun getImageListener(
            view: ImageView, progressBar: ProgressBar?, errorImageResId: Int
        ): ImageLoader.ImageListener {
            return object : ImageLoader.ImageListener {
                override fun onResponse(response: ImageLoader.ImageContainer, isImmediate: Boolean) {

                    if (response.bitmap != null) {
                        view.setImageBitmap(response.bitmap)
                    } else {
                        view.setImageResource(errorImageResId)
                    }

                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                }

                override fun onErrorResponse(error: VolleyError) {
                    if (errorImageResId != 0) {
                        view.setImageResource(errorImageResId)
                    } else {
                        view.setImageResource(android.R.drawable.ic_menu_report_image)
                    }

                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}