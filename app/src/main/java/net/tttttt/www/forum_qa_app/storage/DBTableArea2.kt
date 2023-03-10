package net.tttttt.www.forum_qa_app.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import net.tttttt.www.forum_qa_app.entities.DataArea2


class DBTableArea2(context: Context) {

    private val TAG = "DBTableArea2"
    private val tableName = DataArea2.TABLE_NAME

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

            val input = DataArea2(
                data.get("id") as Int,
                data.get("name") as String,
                data.get("parent_id") as Int
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

    /**
     * 全て取得
     */
    fun getAll(dBHelper: DBHelper): ArrayList<DataArea2> {
        var c: Cursor? = null
        var datas: ArrayList<DataArea2> = ArrayList()

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

    /**
     * 1データ
     */
    fun getData(dBHelper: DBHelper, id: Int): DataArea2 {
        var c: Cursor? = null
        var data = DataArea2()

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

    /**
     * area1のIDを対象としたarea2のデータを取得
     */
    fun getTargetChild(dBHelper: DBHelper, parent_id: Int): java.util.ArrayList<DataArea2> {
        var array: java.util.ArrayList<DataArea2> = java.util.ArrayList()

        var c: Cursor? = null

        try {
            val sql = StringBuffer()
            sql.append("select * from $tableName where parent_id = $parent_id")
            c = dBHelper.db!!.rawQuery(sql.toString(), null)

            var isResult = c!!.moveToFirst()
            while (isResult) {
                array.add(getResult(c))
                isResult = c.moveToNext()
            }
        } catch (e: Exception) {
            Log.e(TAG, "get $tableName all data: ${e.message}")
        } finally {
            if (c != null) {
                c.close()
            }
        }

        return array
    }


    // カラム全取得のみで使える
    fun getResult(c: Cursor): DataArea2 {
        return DataArea2(
            c.getInt(c.getColumnIndex("id")),
            c.getString(c.getColumnIndex("name")),
            c.getInt(c.getColumnIndex("parent_id"))
        )
    }

    /** insert **/
    fun insert(dBHelper: DBHelper, data: DataArea2): Int {
        val cv = ContentValues()
        cv.put(DataArea2.COL[0], data.id)
        cv.put(DataArea2.COL[1], data.name)
        cv.put(DataArea2.COL[2], data.parent_id)
        val rowId: Long = dBHelper.db!!.insert(tableName, null, cv)
        return  rowId.toInt()
    }

    /** update **/
    fun update(dBHelper: DBHelper, data: DataArea2, condition: String): Boolean {
        val cv = ContentValues()
        cv.put(DataArea2.COL[1], data.name)
        cv.put(DataArea2.COL[2], data.parent_id)
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