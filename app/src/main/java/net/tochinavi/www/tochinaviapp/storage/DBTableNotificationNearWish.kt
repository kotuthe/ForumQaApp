package net.tochinavi.www.tochinaviapp.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import net.tochinavi.www.tochinaviapp.BuildConfig
import net.tochinavi.www.tochinaviapp.entities.DataNotificationNearWish
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

class DBTableNotificationNearWish(context: Context) {
    companion object {
        val TAG = "DBTableNcNearWish"
        val TAG_SHORT = "DBTableNcNearWish"
        private val tableName = DataNotificationNearWish.TABLE_NAME
    }

    // Boolをdbに入れるとき
    fun to_db_boolean(bool: Boolean): Int {
        return if (bool) {
            1
        } else 2
    }

    // Boolをdbから取り出すとき
    fun return_db_boolean(value: Int): Boolean {
        return value == 1
    }

    fun to_db_array_int(array: IntArray): String {
        val join = ","
        var rVal: String = ""
        if (array.size > 0) {
            for (i in array.indices) {
                val `val` = array[i].toString()
                rVal += `val`
                if (i < array.size - 1) {
                    rVal += join
                }
            }
        }
        return rVal
    }

    fun to_db_json_array_int(json_array: JSONArray): String {
        if (json_array.length() > 0) {
            val array = IntArray(json_array.length())
            for (i in 0 until json_array.length()) {
                array[i] = json_array.getInt(i)
            }
            return to_db_array_int(array)
        }
        return ""
    }

    fun return_db_array_int(value: String): IntArray {
        val string_array = value.split(",").toTypedArray()
        if (!value.isEmpty() && string_array.size > 0) {
            val rArray = IntArray(string_array.size)
            for (i in string_array.indices) {
                val `val` = string_array[i].toInt()
                rArray[i] = `val`
            }
            return rArray
        }
        return intArrayOf()
    }

    fun getAll(
        dbHelper: DBHelper
    ): ArrayList<DataNotificationNearWish> {
        
        var c: Cursor? = null
        val datas: ArrayList<DataNotificationNearWish> = arrayListOf()

        // 既存のkeyを取得する
        try {
            val sql = StringBuffer()
            sql.append("select ")
            for (i in 0 until DataNotificationNearWish.COL.size) {
                val newLine =
                    if (i == DataNotificationNearWish.COL.size - 1) "" else ","
                sql.append(
                    " " + DataNotificationNearWish.TABLE_NAME_OMISSION + "." + DataNotificationNearWish.COL.get(
                        i
                    ).toString() + newLine
                )
            }
            sql.append(" from ")
            sql.append(" " + DataNotificationNearWish.TABLE_NAME + " " + DataNotificationNearWish.TABLE_NAME_OMISSION)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "sql : $sql")
            }

            c = dbHelper.db!!.rawQuery(sql.toString(), null)
            var isResult = c.moveToFirst()
            while (isResult) {
                val item = DataNotificationNearWish(
                    c.getString(0).toInt(), c.getString(1).toInt(),
                    c.getString(2), c.getString(3).toDouble(), c.getString(4).toDouble(),
                    c.getString(5)
                )
                item.setOpenData(
                    c.getString(6).toInt(),
                    return_db_array_int(c.getString(7)),
                    return_db_array_int(c.getString(8)),
                    return_db_array_int(c.getString(9)),
                    return_db_array_int(c.getString(10)),
                    return_db_boolean(c.getString(11).toInt()),
                    return_db_array_int(c.getString(12)),
                    return_db_array_int(c.getString(13)),
                    return_db_array_int(c.getString(14)),
                    return_db_array_int(c.getString(15)),
                    return_db_array_int(c.getString(16)),
                    return_db_array_int(c.getString(17)),
                    return_db_array_int(c.getString(18)),
                    return_db_array_int(c.getString(19)),
                    return_db_array_int(c.getString(20)),
                    return_db_array_int(c.getString(21)),
                    return_db_array_int(c.getString(22)),
                    return_db_array_int(c.getString(23)),
                    return_db_array_int(c.getString(24)),
                    return_db_array_int(c.getString(25)),
                    return_db_array_int(c.getString(26)),
                    return_db_array_int(c.getString(27)),
                    return_db_array_int(c.getString(28)),
                    return_db_array_int(c.getString(29)),
                    return_db_array_int(c.getString(30)),
                    return_db_array_int(c.getString(31)),
                    return_db_array_int(c.getString(32)),
                    return_db_array_int(c.getString(33)),
                    return_db_array_int(c.getString(34)),
                    return_db_array_int(c.getString(35)),
                    return_db_array_int(c.getString(36)),
                    return_db_array_int(c.getString(37)),
                    return_db_array_int(c.getString(38)),
                    return_db_array_int(c.getString(39)),
                    return_db_array_int(c.getString(40)),
                    return_db_array_int(c.getString(41)),
                    return_db_array_int(c.getString(42)),
                    return_db_array_int(c.getString(43)),
                    return_db_array_int(c.getString(44)),
                    return_db_array_int(c.getString(45)),
                    return_db_array_int(c.getString(46)),
                    return_db_array_int(c.getString(47)),
                    return_db_array_int(c.getString(48)),
                    return_db_array_int(c.getString(49)),
                    return_db_array_int(c.getString(50))
                )
                datas.add(item)
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

    // cvはこっちか、呼び出す側かはお任せ
    fun insert(dbHelper: DBHelper, cv: ContentValues?) {
        dbHelper.db!!.insert(DataNotificationNearWish.TABLE_NAME, null, cv)
    }

    // cvはこっちか、呼び出す側かはお任せ
    fun update(
        dbHelper: DBHelper,
        id: String,
        type: String,
        cv: ContentValues?
    ) {
        dbHelper.db!!.update(
            DataNotificationNearWish.TABLE_NAME,
            cv,
            DataNotificationNearWish.COL.get(0)
                .toString() + "=" + id + " AND " + DataNotificationNearWish.COL.get(1) + "=" + type,
            null
        )
    }

    /**
     * 通知したスポットの時間を更新(ServiceNearWishSpotで使用)
     */
    fun updateTime(
        dbHelper: DBHelper,
        id: String,
        type: String,
        time: String?
    ) {
        val cv = ContentValues()
        cv.put(DataNotificationNearWish.COL.get(5), time)
        update(dbHelper, id, type, cv)
    }

    fun setJsonData(dbHelper: DBHelper, json_array: JSONArray) {
        /*
        var insertArray: List<DataNotificationNearWish> =
            ArrayList<DataNotificationNearWish>()
        val c: Cursor? = null
        try {
            insertArray = getAll(dbHelper, c)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "error occured!! cause : " + e.message
            )
        } finally {
            c?.close()
            mergeJsonData(dbHelper, insertArray, json_array)
        }
        */

        val insertArray: ArrayList<DataNotificationNearWish> = getAll(dbHelper)
        mergeJsonData(dbHelper, insertArray, json_array)
    }


    private fun getTimeData(json_array: JSONArray): Array<IntArray>? {
        var index = 0
        val open_01_start = IntArray(2)
        val open_01_end = IntArray(2)
        val open_02_start = IntArray(2)
        val open_02_end = IntArray(2)
        try {
            if (json_array.length() >= 1) {
                val json_open_01 = json_array.getJSONObject(0)
                open_01_start[0] = json_open_01.getJSONArray("start").getInt(0)
                open_01_start[1] = json_open_01.getJSONArray("start").getInt(1)
                open_01_end[0] = json_open_01.getJSONArray("end").getInt(0)
                open_01_end[1] = json_open_01.getJSONArray("end").getInt(1)
                index++
            }
            if (json_array.length() >= 2) {
                val json_open_02 = json_array.getJSONObject(1)
                open_02_start[0] = json_open_02.getJSONArray("start").getInt(0)
                open_02_start[1] = json_open_02.getJSONArray("start").getInt(1)
                open_02_end[0] = json_open_02.getJSONArray("end").getInt(0)
                open_02_end[1] = json_open_02.getJSONArray("end").getInt(1)
                index++
            }
        } catch (e: Exception) {
            Log.w("exception: ", "" + e.message)
            return null
        }
        return if (index == 1) {
            arrayOf(open_01_start, open_01_end)
        } else arrayOf(open_01_start, open_01_end, open_02_start, open_02_end)
    }

    /**
     *
     * @param json_array
     * @return 0: 時間1 start, 1: 時間1 end, 2: 時間2 start, 3: 時間2 end (エラー時はnull)
     */
    private fun getOpenTimes(json_array: JSONArray): HashMap<Int, IntArray> {
        val rArray = HashMap<Int, IntArray>()
        try {
            var rIndex = 0
            for (i in 0..1) { // 時間1, 2
                if (json_array.length() >= i + 1) {
                    val obj = json_array.getJSONObject(i)
                    val start = intArrayOf(
                        obj.getJSONArray("start").getInt(0),
                        obj.getJSONArray("start").getInt(1)
                    )
                    val end = intArrayOf(
                        obj.getJSONArray("end").getInt(0),
                        obj.getJSONArray("end").getInt(1)
                    )
                    rArray[rIndex] = start
                    rArray[rIndex + 1] = end
                } else {
                    rArray[rIndex] = intArrayOf()
                    rArray[rIndex + 1] = intArrayOf()
                }
                rIndex += 2
            }
        } catch (e: Exception) {
            // Log.w("exception: ", "" + e.getMessage());
            return hashMapOf()
        }
        return rArray
    }


    private fun mergeJsonData(
        dbHelper: DBHelper,
        insertArray: ArrayList<DataNotificationNearWish>,
        json_array: JSONArray
    ) {
        val insertIds = ArrayList<Int>()
        val insertTypes = ArrayList<Int>()
        for (i in insertArray.indices) {
            insertIds.add(insertArray[i].id)
            insertTypes.add(insertArray[i].type)
        }
        try {
            for (i in 0 until json_array.length()) {
                // jsonデータの取得 //
                val obj = json_array.getJSONObject(i)
                val id = obj.getInt("id")
                val type = obj.getInt("type")
                val name = obj.getString("name")
                val latitude = obj.getDouble("latitude")
                val longitude = obj.getDouble("longitude")
                val obj_open_data = obj.getJSONObject("open_time_datas")
                val is_open = obj_open_data.getInt("is_open")
                val is_open_time_limit =
                    obj_open_data.getBoolean("is_open_time_limit")

                // テーブルの更新 //
                val insert =
                    if (insertIds.indexOf(id) == -1 || insertTypes.indexOf(type) == -1) true else false
                val cv = ContentValues()
                if (insert) {
                    cv.put(DataNotificationNearWish.COL.get(0), id)
                    cv.put(DataNotificationNearWish.COL.get(1), type)
                    cv.put(DataNotificationNearWish.COL.get(5), "")
                }

                // 共通
                cv.put(DataNotificationNearWish.COL.get(2), name)
                cv.put(DataNotificationNearWish.COL.get(3), latitude)
                cv.put(DataNotificationNearWish.COL.get(4), longitude)

                // open date //
                cv.put(DataNotificationNearWish.COL.get(6), is_open)
                var hm_open: HashMap<Int, IntArray> = hashMapOf()
                if (!obj_open_data.isNull("open")) {
                    hm_open = getOpenTimes(obj_open_data.getJSONArray("open"))
                }
                for (j in 0..3) {
                    cv.put(
                        DataNotificationNearWish.COL.get(7 + j),
                        if (!hm_open.isEmpty()) to_db_array_int(hm_open[j]!!) else ""
                    )
                }
                cv.put(
                    DataNotificationNearWish.COL.get(11),
                    to_db_boolean(is_open_time_limit)
                )
                var sub_is_open_limit: JSONArray = JSONArray()
                if (!obj_open_data.isNull("sub_is_open_limit")) {
                    sub_is_open_limit = obj_open_data.getJSONArray("sub_is_open_limit")
                }
                cv.put(
                    DataNotificationNearWish.COL.get(12),
                    to_db_json_array_int(sub_is_open_limit)
                )
                for (j in 0..8) {
                    var hm_sub_open: HashMap<Int, IntArray> = hashMapOf()
                    val target = "sub_open_" + String.format("%02d", j + 1)
                    if (!obj_open_data.isNull(target)) {
                        hm_sub_open = getOpenTimes(obj_open_data.getJSONArray(target))
                    }
                    for (k in 0..3) {
                        val index = 13 + 4 * j + k
                        cv.put(
                            DataNotificationNearWish.COL.get(index),
                            if (!hm_sub_open.isEmpty()) to_db_array_int(hm_sub_open[k]!!) else ""
                        )
                    }
                }
                var close_info: JSONArray = JSONArray()
                if (!obj_open_data.isNull("close_info")) {
                    close_info = obj_open_data.getJSONArray("close_info")
                }
                cv.put(
                    DataNotificationNearWish.COL.get(49),
                    to_db_json_array_int(close_info)
                )
                var close_ex_info: JSONArray = JSONArray()
                if (!obj_open_data.isNull("close_ex_info")) {
                    close_ex_info = obj_open_data.getJSONArray("close_ex_info")
                }
                cv.put(
                    DataNotificationNearWish.COL.get(50),
                    to_db_json_array_int(close_ex_info)
                )
                if (insert) {
                    insert(dbHelper, cv)
                } else {
                    update(dbHelper, id.toString(), type.toString(), cv)
                }
            }

            // 無効データは削除
            for_loop@ for (i in insertIds.indices) {
                for (j in 0 until json_array.length()) {
                    val obj = json_array.getJSONObject(j)
                    val id = obj.getInt("id")
                    val type = obj.getInt("type")
                    if (insertIds[i] == id && insertTypes[i] == type) {
                        continue@for_loop
                    }
                }
                dbHelper.db!!.delete(
                    DataNotificationNearWish.TABLE_NAME,
                    DataNotificationNearWish.COL.get(0)
                        .toString() + "=" + insertIds[i] + " AND " + DataNotificationNearWish.COL.get(
                        1
                    ) + "=" + insertTypes[i],
                    null
                )
            }
        } catch (e: Exception) {
            Log.w("exception: ", "" + e.message)
        }
    }
}