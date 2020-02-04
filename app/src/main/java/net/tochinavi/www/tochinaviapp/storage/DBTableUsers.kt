package net.tochinavi.www.tochinaviapp.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import net.tochinavi.www.tochinaviapp.entities.DataUsers
import java.io.ByteArrayOutputStream
import java.util.*


/** サーバーとアプリのデータをやりとりするためのTable **/
class DBTableUsers(context: Context) {

    companion object {
        val TAG = "DBTableUsers"
    }

    enum class Ids(val rawValue: Int)  {
        member_login(1)
    }

    private val tableName = DataUsers.TABLE_NAME

    // 主キー //
    private val key_column: String = "id"

    // 初回値を新規登録をする
    init {
        // initialize
    }

    // カラム全取得のみで使える
    fun getResult(c: Cursor): DataUsers {

        // column: image
        var image_bmp: Bitmap? = null
        val blob = c.getBlob(c.getColumnIndex("image"))
        if (!blob.isEmpty()) {
            try {
                val opt = BitmapFactory.Options()
                opt.inJustDecodeBounds = false
                image_bmp = BitmapFactory.decodeByteArray(blob, 0, blob.size, opt)
            } catch (e: Exception) {
                e.message
            }
        }

        // column: auto_save
        val auto_save_str = c.getString(c.getColumnIndex("auto_save"))

        return DataUsers(
            c.getInt(c.getColumnIndex("id")),
            c.getInt(c.getColumnIndex("user_id")),
            c.getString(c.getColumnIndex("email")),
            c.getString(c.getColumnIndex("password")),
            c.getString(c.getColumnIndex("name")),
            image_bmp,
            auto_save_str.toBoolean()
            )
    }

    fun getAll(dBHelper: DBHelper): ArrayList<DataUsers> {
        var c: Cursor? = null
        var datas: ArrayList<DataUsers> = ArrayList()

        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            while (isResult) {
                datas.add(getResult(c))
                isResult = c.moveToNext()
            }
        } catch (e: Exception) {
            Log.e(TAG, "get $tableName all data: ${e.message}")
        } finally {
            if (c != null) {
                c.close()
            }
        }
        return datas
    }

    fun getData(dBHelper: DBHelper, ids: Ids): DataUsers? {


        var c: Cursor? = null
        var data: DataUsers? = null

        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName where id = ${ids.rawValue}")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            while (isResult) {
                data = getResult(c)
                isResult = c.moveToNext()
            }

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        } finally {

            if (c != null) {
                c.close()
            }
        }
        return data
    }

    /**
     * dataはワンデータ、insert or updateできるようにする
     */
    fun setData(dBHelper: DBHelper, ids: Ids, data: DataUsers) {
        val result = this.getData(dBHelper, ids)
        if (result != null) {
            // update
            this.update(dBHelper, data, "$key_column = ${data.id}")
        } else {
            // insert
            this.insert(dBHelper, data)
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val resize_bmp = bitmapResize(bitmap, 500)
        val baos = ByteArrayOutputStream()
        resize_bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        resize_bmp.recycle()
        return baos.toByteArray()
    }

    fun bitmapResize(origin_bmp: Bitmap, maxSize: Int): Bitmap {

        // リサイズ比
        val scale: Double
        if (origin_bmp.width >= origin_bmp.height) {
            scale = (maxSize / origin_bmp.width).toDouble()
        } else {
            scale = (maxSize / origin_bmp.height).toDouble()
        }

        // リサイズ
        val resize_bmp = Bitmap.createScaledBitmap(
            origin_bmp,
            (origin_bmp.width * scale).toInt(),
            (origin_bmp.height * scale).toInt(),
            true
        )
        return resize_bmp
    }

    fun getContentValues(data: DataUsers, new: Boolean): ContentValues {
        // bitmap -> byteArray
        var image_byte: ByteArray? = null
        if (data.image != null) {
            image_byte = bitmapToByteArray(data.image!!)
        }
        val cv = ContentValues()
        if (new) {
            cv.put(DataUsers.COL[0], data.id)
        }
        cv.put(DataUsers.COL[1], data.user_id)
        cv.put(DataUsers.COL[2], data.email)
        cv.put(DataUsers.COL[3], data.password)
        cv.put(DataUsers.COL[4], data.name)
        if (image_byte != null) {
            cv.put(DataUsers.COL[5], image_byte!!)
        } else {
            cv.put(DataUsers.COL[5], "")
        }
        cv.put(DataUsers.COL[6], data.auto_save)
        return cv
    }

    /** insert **/
    fun insert(dBHelper: DBHelper, data: DataUsers): Int {
        val cv = getContentValues(data, true)
        val rowId: Long = dBHelper.db!!.insert(tableName, null, cv)
        return  rowId.toInt()
    }

    /** update **/
    fun update(dBHelper: DBHelper, data: DataUsers, condition: String): Boolean {
        val cv = getContentValues(data, false)
        if (dBHelper.db!!.update(tableName, cv, condition, null) > 0) {
            return true
        }
        return false
    }

    /** delete **/
    fun delete(dBHelper: DBHelper, condition: String): Boolean {
        if (dBHelper.db!!.delete(tableName, condition, null) > 0) {
            return true
        }
        return false
    }

    /** count **/
    fun count(dBHelper: DBHelper, condition: String? = null): Int {
        val count = DatabaseUtils.queryNumEntries(dBHelper.db!!, tableName, condition, null)
        return count.toInt()
    }


}