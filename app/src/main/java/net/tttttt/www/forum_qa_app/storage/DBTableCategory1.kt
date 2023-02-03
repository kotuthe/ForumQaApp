package net.tttttt.www.forum_qa_app.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import net.tttttt.www.forum_qa_app.entities.DataCategory1


class DBTableCategory1(context: Context) {

    private val TAG = "DBTableCategory1"
    private val tableName = DataCategory1.TABLE_NAME

    // 主キー //
    private val key_column: String = "id"

    init {
        // initialize
    }

    fun setData(dBHelper: DBHelper, datas: ArrayList<MutableMap<String, Any>>) {
        var c: Cursor? = null
        var key_arrays: ArrayList<Int> = ArrayList()

        // 既存のkeyを取得する
        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            while (isResult) {
                key_arrays.add(c.getInt(c.getColumnIndex(key_column)))
                isResult = c.moveToNext()
            }
        } catch (e: Exception) {
            Log.e(TAG, "get $tableName all data: ${e.message}")
        } finally {
            if (c != null) {
                c.close()
            }
        }

        // 既にあればupdate, なければinsert
        for (data in datas) {
            var already = false

            for (key in key_arrays) {
                if (key == data.get(key_column)) {
                    already = true
                    break
                }
            }

            val input = DataCategory1(
                data.get("id") as Int,
                data.get("name") as String,
                data.get("sub_title") as String
            )

            var success = true
            if (already) {
                // update
                success = this.update(dBHelper, input, "$key_column = ${data.get(key_column)}")
            } else {
                // insert
                if (this.insert(dBHelper, input) < 0) {
                    success = false
                }
            }

            if (!success) {
                break
            }
        }

        // 既存のkeyとdatasを比較して無いデータは削除する
        for_loop@ for (key in key_arrays) {
            for (data in datas) {
                if (key == data.get(key_column)) {
                    continue@for_loop
                }
            }
            this.delete(dBHelper,  "$key_column = $key")
        }

    }

    fun getAll(dBHelper: DBHelper): ArrayList<DataCategory1> {
        var c: Cursor? = null
        var datas: ArrayList<DataCategory1> = ArrayList()

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

    fun getData(dBHelper: DBHelper, id: Int): DataCategory1 {
        var c: Cursor? = null
        var data = DataCategory1()

        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName where id = ${id}")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            while (isResult) {
                data = getResult(c)
                isResult = c.moveToNext()
            }
        } catch (e: Exception) {
            Log.e(TAG, "get $tableName all data: ${e.message}")
        } finally {
            if (c != null) {
                c.close()
            }
        }
        return data
    }

    // カラム全取得のみで使える
    fun getResult(c: Cursor): DataCategory1 {
        return DataCategory1(
            c.getInt(c.getColumnIndex("id")),
            c.getString(c.getColumnIndex("name")),
            c.getString(c.getColumnIndex("sub_title"))
        )
    }

    /** insert **/
    fun insert(dBHelper: DBHelper, data: DataCategory1): Int {
        val cv = ContentValues()
        cv.put(DataCategory1.COL[0], data.id)
        cv.put(DataCategory1.COL[1], data.name)
        cv.put(DataCategory1.COL[2], data.sub_title)
        val rowId: Long = dBHelper.db!!.insert(tableName, null, cv)
        return  rowId.toInt()
    }

    /** update **/
    fun update(dBHelper: DBHelper, data: DataCategory1, condition: String): Boolean {
        val cv = ContentValues()
        cv.put(DataCategory1.COL[1], data.name)
        cv.put(DataCategory1.COL[2], data.sub_title)
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