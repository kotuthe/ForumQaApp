package net.tochinavi.www.tochinaviapp.value

import android.content.Context
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





