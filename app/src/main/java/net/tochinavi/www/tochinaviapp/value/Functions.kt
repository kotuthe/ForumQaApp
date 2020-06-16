package net.tochinavi.www.tochinaviapp.value

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.TextView
import net.tochinavi.www.tochinaviapp.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class Functions(private val context: Context) {
    /**
     * タブレットかどうか判定
     * @return
     */
    val isTablet: Boolean
        get() = context.resources.configuration.smallestScreenWidthDp >= 600

    /**
     * 対象カテゴリーの大カテゴリーを取得
     * @param level カテゴリーのレベル
     * @param id カテゴリーのID
     * @return DataCategory1
     */
    /*
    fun getDataCategory1(level: Int, id: Int): DataCategory1 {
        if (level == DataCategory2.LEVEL) {
            val category: DataCategory2 = DBTableCategory2.getData(context, id.toString())
            return getDataCategory1(DataCategory1.LEVEL, category.getParentId())
        } else if (level == DataCategory3.LEVEL) {
            val category: DataCategory3 = DBTableCategory3.getData(context, id.toString())
            return getDataCategory1(DataCategory2.LEVEL, category.getParentId())
        }
        return DBTableCategory1.getData(context, id.toString())
    }
    */

    /**
     * 末尾に...がついた文字列に変換
     * @param str
     * @param endIndex
     * @return
     */
    fun getStringEndEllipsize(str: String, endIndex: Int): String {
        if (str.length > endIndex) {
            val rStr = str.substring(0, endIndex)
            return "$rStr..."
        }
        return str
    }

    /**
     * 配列の文字列を区切り文字を入れる
     * @param glue
     * @param array
     * @return
     */
    fun getStringImplode(
        glue: String?,
        array: List<String?>
    ): String? {
        var str: String? = ""
        for (i in array.indices) {
            str += array[i]
            if (array.size - 1 > i) {
                str += glue
            }
        }
        return str
    }

    /**
     * タイプの名前を取得
     * @param type
     * @return
     */
    fun getReviewTypeName(type: Int): String {
        var name = ""
        when (type) {
            1 -> name = "スポット"
            2 -> name = "イベント"
            4 -> name = "特集"
            5 -> name = "エリア"
        }
        return name
    }

    /**
     * タイプのカラー取得（color.xmlのデータ）
     * @param type
     * @return int(drawableデータ)
     */
    fun getReviewTypeColor(type: Int): Int {
        var drawable = 0
        /*
        when (type) {
            1 -> drawable = R.drawable.txt_my_review_type_1
            2 -> drawable = R.drawable.txt_my_review_type_2
            4 -> drawable = R.drawable.txt_my_review_type_4
            5 -> drawable = R.drawable.txt_my_review_type_5
        }
        */
        return drawable
    }

    /**
     * intをjoinする
     * @param a
     * @param b
     * @return
     */
    fun joinInt(a: Int, b: Int): Int {
        val str_a = a.toString()
        val str_b = b.toString()
        return try {
            (str_a + str_b).toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun explodeNotificationManagerId(id: Int): IntArray? {
        val str_id = id.toString()
        val str_spot_type = str_id.substring(0, 1)
        val str_spot_id = str_id.substring(1)
        return try {
            val spot_type = str_spot_type.toInt()
            val spot_id = str_spot_id.toInt()
            intArrayOf(spot_type, spot_id)
        } catch (e: NumberFormatException) {
            null
        }
    }

    // Serviceの起動状態を取得 //
    fun isWorkingService(className: String): Boolean {
        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (serviceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (className == serviceInfo.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * dpからpixelへの変換
     * @param dp
     * @return float pixel
     */
    /*fun convertDp2Px(dp: Float): Float {
        val metrics = context.resources.displayMetrics
        return dp * metrics.density
    }*/

    /**
     * リサイズするマトリクスを取得
     * 縮小の場合のみ、縮小のマトリクスをセットして返す
     *
     * @param file 入力画像
     * @param matrix 元のマトリクス
     * @return matrix リサイズ後のマトリクス
     */
    fun getResizedMatrix(
        file: File,
        matrix: Matrix,
        MAX_PIXEL: Int
    ): Matrix { // リサイズチェック用にメタデータ読み込み
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.path, options)
        val height = options.outHeight
        val width = options.outWidth
        // リサイズ比の取得（画像の短辺がMAX_PIXELになる比を求めます）
// float scale = Math.max((float) MAX_PIXEL / width, (float) MAX_PIXEL / height);
        val scale = Math.min(
            MAX_PIXEL.toFloat() / width,
            MAX_PIXEL.toFloat() / height
        )
        // 縮小のみのため、scaleは1.0未満の場合のみマトリクス設定
        if (scale < 1.0) {
            matrix.postScale(scale, scale)
        }
        return matrix
    }

    fun getRotateMatrix(
        file: File,
        matrix: Matrix
    ): Matrix {
        var matrix = matrix
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val ei = ExifInterface(file.path)
                matrix = setRotateExif(matrix, ei)
            } catch (e: Exception) {
            }
        }
        return matrix
    }

    fun getRotateMatrix(
        uri: Uri?,
        matrix: Matrix
    ): Matrix {
        var matrix = matrix
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val parcel =
                    context.contentResolver.openFileDescriptor(uri!!, "r")
                val fd = parcel!!.fileDescriptor
                val ei = ExifInterface(fd)
                matrix = setRotateExif(matrix, ei)
            } catch (e: Exception) {
            }
        }
        return matrix
    }

    fun setRotateExif(matrix: Matrix, ei: ExifInterface): Matrix {
        if (Build.VERSION.SDK_INT >= 24) {
            val parcel: ParcelFileDescriptor? = null
            try {
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_UNDEFINED -> {
                    }
                    ExifInterface.ORIENTATION_NORMAL -> {
                    }
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->  // 水平方向にリフレクト
                        matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_ROTATE_180 ->  // 180度回転
                        matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL ->  // 垂直方向にリフレクト
                        matrix.postScale(1f, -1f)
                    ExifInterface.ORIENTATION_ROTATE_90 ->  // 反時計回り90度回転
                        matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_TRANSVERSE -> {
                        // 時計回り90度回転し、垂直方向にリフレクト
                        matrix.postRotate(-90f)
                        matrix.postScale(1f, -1f)
                    }
                    ExifInterface.ORIENTATION_TRANSPOSE -> {
                        // 反時計回り90度回転し、垂直方向にリフレクト
                        matrix.postRotate(90f)
                        matrix.postScale(1f, -1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 ->  // 反時計回りに270度回転（時計回りに90度回転）
                        matrix.postRotate(-90f)
                }
            } catch (e: Exception) {
            } finally {
                if (parcel != null) {
                    try {
                        parcel.close()
                    } catch (ignored: IOException) {
                    }
                }
            }
        }
        return matrix
    }

    /**
     * リサイズ・回転後の画像を取得
     * 一時ファイルを作成する
     *
     * @param file オリジナル画像
     * @param matrix 回転・縮小を設定したマトリクス
     * @return file 一時保存先のファイル
     */
    fun getTemporaryFile(
        file: File,
        matrix: Matrix?,
        index: Int
    ): File { // 元画像の取得
        var file = file
        val originalPicture = BitmapFactory.decodeFile(file.path)
        val height = originalPicture.height
        val width = originalPicture.width
        // マトリクスをつけることで縮小、向きを反映した画像を生成
        val resizedPicture =
            Bitmap.createBitmap(originalPicture, 0, 0, width, height, matrix, true)
        // 一時ファイルの保存
        try {
            val rnd = System.currentTimeMillis()
            val ext = getExtension(file.name)
            val destination =
                File(context.cacheDir, "tmp$rnd.$ext")
            val outputStream = FileOutputStream(destination)
            resizedPicture.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // ファイルのインスタンスをリサイズ後のものに変更
            file = destination
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * ファイルのバスから拡張子を取得
     * @param fileName
     * @return
     */
    fun getExtension(fileName: String?): String? {
        if (fileName == null) return null
        val point = fileName.lastIndexOf(".")
        return if (point != -1) {
            fileName.substring(point + 1)
        } else null
    }

    fun getResizeBitmap(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? { // 写真があるか判定
        val file_path = uri.path!!.replace("file:", "")
        val file = File(file_path)
        if (!file.exists()) {
            return null
        }
        var bitmap: Bitmap? = null
        val original_options = BitmapFactory.Options()
        original_options.inJustDecodeBounds = true
        try {
            val orign_input =
                context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(orign_input, null, original_options)
            // reqHeightとreqWidthを元に圧縮する比率計算 //
            val original_height = original_options.outHeight
            val original_width = original_options.outWidth
            var inSampleSize = 1
            if (original_height > reqHeight || original_width > reqWidth) {
                inSampleSize = if (original_width > original_height) {
                    Math.round(original_height.toFloat() / reqHeight.toFloat())
                } else {
                    Math.round(original_width.toFloat() / reqWidth.toFloat())
                }
            }
            if (inSampleSize == 0) {
                inSampleSize = 1
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = inSampleSize
            val input = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(input, null, options)
            orign_input!!.close()
            input!!.close()
        } catch (e: FileNotFoundException) {
            return null
        } catch (e: Exception) {
            return null
        }
        return bitmap
    }

    /**
     * クリップボード：テキスト
     * 参考：
     * https://developer.android.com/guide/topics/text/copy-paste#PastePlainText
     */
    fun clipboardText(activity: Activity, text: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("simple text", text)
        clipboard.setPrimaryClip(clip)
    }

}