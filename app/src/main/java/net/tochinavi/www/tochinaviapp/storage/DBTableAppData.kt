package net.tochinavi.www.tochinaviapp.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import net.tochinavi.www.tochinaviapp.entities.DataAppData
import net.tochinavi.www.tochinaviapp.value.convertDate
import net.tochinavi.www.tochinaviapp.value.convertString
import java.util.*

/** サーバーとアプリのデータをやりとりするためのTable **/
class DBTableAppData(context: Context) {

    companion object {
        val TAG = "DBTableAppData"
    }
    enum class Ids(val rawValue: Int)  {
        category_update(1),
        area_update(2),
        wish_update(3)
    }
    val tableName = DataAppData.TABLE_NAME

    // 初回値を新規登録をする
    init {
        // initialize
    }

    // カラム全取得のみで使える
    fun getResult(c: Cursor): DataAppData {
        val modified: String = c.getString(c.getColumnIndex("modified"))
        return DataAppData(
            c.getInt(c.getColumnIndex("id")),
            modified.convertDate()
            )
    }

    fun getAll(dBHelper: DBHelper): ArrayList<DataAppData> {
        var c: Cursor? = null
        var datas: ArrayList<DataAppData> = ArrayList()

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

    fun getData(dBHelper: DBHelper, ids: Ids): DataAppData? {


        var c: Cursor? = null
        var data: DataAppData? = null

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

    fun setInitData(dBHelper: DBHelper) {

        // 初回insertデータ(Idsが変わればこちらも追加する)
        val datas: ArrayList<DataAppData> = arrayListOf(
            DataAppData(1, null),
            DataAppData(2, null),
            DataAppData(3, null)
        )
        var c: Cursor? = null

        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            val insertDatas: ArrayList<DataAppData> = datas
            while (isResult) {
                val id = Integer.parseInt(c.getString(0))
                for ((index, item) in datas.withIndex()){
                    if (item.id == id) {
                        insertDatas.removeAt(index)
                    }
                }

                isResult = c.moveToNext()
            }

            // 足りないデータはinsert
            if (insertDatas.count() > 0) {
                for (item in insertDatas) {
                    this.insert(dBHelper, item)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        } finally {
            if (c != null) {
                c.close()
            }
        }
    }

    /* 更新日を更新する */
    fun updateModified(dBHelper: DBHelper, ids: Ids) {
        var modified = Date(System.currentTimeMillis())
        var data = DataAppData(ids.rawValue, modified)
        this.update(dBHelper, data, "id = ${data.id}")
    }

    /** insert **/
    fun insert(dBHelper: DBHelper, data: DataAppData) {
        val cv = ContentValues()
        cv.put(DataAppData.COL[0], data.id)
        var strModified = ""
        if (data.modified != null) {
            strModified = data.modified!!.convertString()
        }
        cv.put(DataAppData.COL[1], strModified)
        dBHelper.db!!.insert(tableName, null, cv)
    }

    /** update **/
    fun update(dBHelper: DBHelper, data: DataAppData, condition: String): Boolean {
        // val functions: Functions = Functions(mContext)
        val cv = ContentValues()
        var strModified: String = ""
        if (data.modified != null) {
            // strModified = functions.dateToString(data.modified!!)
            strModified = data.modified!!.convertString()
        }
        cv.put(DataAppData.COL[1], strModified)
        if (dBHelper.db!!.update(tableName, cv, condition, null) > 0) {
            return true
        }
        return false
    }


}