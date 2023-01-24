package net.tttttt.www.forum_qa_app.value

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import net.tttttt.www.forum_qa_app.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 変数作成時にnull判定ができる
 */
inline fun <T, R> ifNotNull(value: T?, thenPart: (T) -> R, elsePart: () -> R): R {
    return if (value != null) {
        thenPart(value)
    } else {
        elsePart()
    }
}

/**
 * 変数作成時にnull判定ができる（elseなしver）
 */
inline fun <T, R> ifNotNull(value: T?, thenPart: (T) -> R): R? {
    return if (value != null) {
        thenPart(value)
    } else {
        null
    }
}

/**
 * [変換] dp > px
 */
fun Float.convertDpToPx(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return this * metrics.density
}

/**
 * [変換] Date > String
 */
fun Date.convertString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(format, Locale.JAPAN)
    return sdf.format(this)
}

/**
 * [変換] 文字列の日付(String) > Date
 */
fun String.convertDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    if (this.isEmpty()) { return null }
    val sdf = SimpleDateFormat(format, Locale.JAPAN)
    var date: Date? = null
    try {
        date = sdf.parse(this)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date
}

/**
 * ボタンにアイコンをセット
 */
fun Button.setLeftIcon(icon: Int) {
    val drawable = ContextCompat.getDrawable(this.context, icon)
    val size = 18f.convertDpToPx(this.context).toInt()
    /*if (functions.isTablet()) {
        size = 22f.convertDpToPx(this.context).toInt()
    }*/
    drawable!!.setBounds(0, 0, size, size)
    this.setCompoundDrawables(drawable, null, null, null)
}

/**
 * SpotInfoのアイコン付きボタン
 */
fun Button.setSpotInfoLeftIcon(icon: Int) {
    val drawable = ContextCompat.getDrawable(this.context, icon)
    drawable!!.setBounds(0,0,20f.convertDpToPx(this.context).toInt(),20f.convertDpToPx(this.context).toInt())
    this.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null)

}


fun MaterialButton.setSpotInfoTopIcon(icon: Int) {
    val drawable = ContextCompat.getDrawable(this.context, icon)
    val iconSize = 20f.convertDpToPx(this.context).toInt()
    drawable!!.setBounds(0, 0, iconSize, iconSize)
    this.setCompoundDrawables(null, drawable, null, null)
}


/**
 * isEnableで設定するとdisable時にクリックができなくなるのを防ぐため
 */
fun MaterialButton.setSpotInfoTintColor(enable: Boolean) {
    val color = if (enable)
        ContextCompat.getColor(this.context, R.color.spot_info_btn_tint_color) else Color.LTGRAY

    this.setTextColor(color)
    if (this.icon != null) {
        this.iconTint = ColorStateList.valueOf(color)
    }
    if (this.compoundDrawables.size > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // API23以上
        this.compoundDrawableTintList = ColorStateList.valueOf(color)
    }

}


/*
fun getStringCurrentDateTime(): String {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)
        return dateToString(calendar.time)
    }

    fun dateToString(date: Date, format: String = "yyyy-MM-dd HH:mm:ss"): String {
        // val format: String = "yyyy-MM-dd HH:mm:ss"
        val sdf = SimpleDateFormat(format, Locale.JAPAN)
        return sdf.format(date)
    }

    fun stringToDate(str: String, format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
        // val format: String = "yyyy-MM-dd HH:mm:ss"
        val sdf = SimpleDateFormat(format, Locale.JAPAN)
        var date: Date? = null
        try {
            date = sdf.parse(str)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }
 */





