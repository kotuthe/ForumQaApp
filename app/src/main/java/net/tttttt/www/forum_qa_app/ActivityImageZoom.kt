package net.tttttt.www.forum_qa_app

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import coil.Coil
import coil.load

class ActivityImageZoom : AppCompatActivity() {

    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor = 1.0f
    private lateinit var mPanGestureDetector: GestureDetectorCompat
    private var mTranslationX = 0f
    private var mTranslationY = 0f

    private var mImageWidth = 0f
    private var mImageHeight = 0f
    private var mDefaultImageWidth = 0f
    private var mDefaultImageHeight = 0f
    private var mViewPortWidth = 0f
    private var mViewPortHeight = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_zoom)

        if (supportActionBar != null) {
            supportActionBar!!.title = "テスト"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        /*imageView.load("https://www.tochinavi.net/img/kuchikomi/02/IMG01_f3d1329e1b59c81151b2c822d707fe16f2bab124.jpg") {

        }*/

        // ※後で調整する
        /*Coil.load(applicationContext, "https://www.tochinavi.net/img/kuchikomi/02/IMG01_f3d1329e1b59c81151b2c822d707fe16f2bab124.jpg") {
            target { drawable ->
                imageView.setImageDrawable(drawable)
                onLoadedImage()
            }
        }*/
    }

    /*
    private fun onLoadedImage() {
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        mPanGestureDetector = GestureDetectorCompat(this, PanListener())

        val viewTreeObserver = imageView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val imageAspectRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.drawable.intrinsicWidth.toFloat()
                    val viewAspectRatio = imageView.height.toFloat() / imageView.width.toFloat()

                    mDefaultImageWidth = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        imageView.width.toFloat()
                    } else {
                        // Portrait image
                        imageView.height.toFloat() / imageAspectRatio
                    }

                    mDefaultImageHeight = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        imageView.width.toFloat() * imageAspectRatio
                    } else {
                        // Portrait image
                        imageView.height.toFloat()
                    }

                    mImageWidth = mDefaultImageWidth
                    mImageHeight = mDefaultImageHeight

                    mViewPortWidth = imageView.width.toFloat()
                    mViewPortHeight = imageView.height.toFloat()
                }
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            mScaleGestureDetector.onTouchEvent(event)
            mPanGestureDetector.onTouchEvent(event)
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            /*
            mScaleFactor *= mScaleGestureDetector.scaleFactor
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 5.0f))
            imageView.scaleX = mScaleFactor
            imageView.scaleY = mScaleFactor
            */
            mScaleFactor *= mScaleGestureDetector.scaleFactor
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 5.0f))
            imageView.scaleX = mScaleFactor
            imageView.scaleY = mScaleFactor
            mImageWidth = mDefaultImageWidth * mScaleFactor
            mImageHeight = mDefaultImageHeight * mScaleFactor

            adjustTranslation(mTranslationX, mTranslationY)

            return true
        }
    }

    private inner class PanListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            val translationX = mTranslationX - distanceX
            val translationY = mTranslationY - distanceY

            // imageView.translationX = translationX
            // imageView.translationY = translationY
            adjustTranslation(translationX, translationY)

            return true
        }
    }

    private fun adjustTranslation(translationX: Float, translationY: Float) {
        val translationXMargin = Math.abs((mImageWidth - mViewPortWidth) / 2)
        val translationYMargin = Math.abs((mImageHeight - mViewPortHeight) / 2)

        if (translationX < 0) {
            mTranslationX = Math.max(translationX, -translationXMargin)
        } else {
            mTranslationX = Math.min(translationX, translationXMargin)
        }

        if (mTranslationY < 0) {
            mTranslationY = Math.max(translationY, -translationYMargin)
        } else {
            mTranslationY = Math.min(translationY, translationYMargin)
        }

        imageView.translationX = mTranslationX
        imageView.translationY = mTranslationY
    }
    */
}
