package net.tttttt.www.forum_qa_app.entities

import java.util.*

class DataNotificationNearWish(
    id: Int,
    type: Int,
    name: String,
    latitude: Double,
    longitude: Double,
    time: String
) {

    companion object {
        val TAG = "DataNotificationNearWish"
        val TABLE_NAME = "notification_near_wish"
        val TABLE_NAME_OMISSION = "notification_near_wish"
        val COL = arrayOf(
            "id",
            "type",
            "name",
            "latitude",
            "longitude",
            "time",
            "is_open",
            "open_01_start",
            "open_01_end",
            "open_02_start",
            "open_02_end",
            "is_open_time_limit",
            "sub_is_open_limit",
            "sub_open_01_01_start",
            "sub_open_01_01_end",
            "sub_open_01_02_start",
            "sub_open_01_02_end",
            "sub_open_02_01_start",
            "sub_open_02_01_end",
            "sub_open_02_02_start",
            "sub_open_02_02_end",
            "sub_open_03_01_start",
            "sub_open_03_01_end",
            "sub_open_03_02_start",
            "sub_open_03_02_end",
            "sub_open_04_01_start",
            "sub_open_04_01_end",
            "sub_open_04_02_start",
            "sub_open_04_02_end",
            "sub_open_05_01_start",
            "sub_open_05_01_end",
            "sub_open_05_02_start",
            "sub_open_05_02_end",
            "sub_open_06_01_start",
            "sub_open_06_01_end",
            "sub_open_06_02_start",
            "sub_open_06_02_end",
            "sub_open_07_01_start",
            "sub_open_07_01_end",
            "sub_open_07_02_start",
            "sub_open_07_02_end",
            "sub_open_08_01_start",
            "sub_open_08_01_end",
            "sub_open_08_02_start",
            "sub_open_08_02_end",
            "sub_open_09_01_start",
            "sub_open_09_01_end",
            "sub_open_09_02_start",
            "sub_open_09_02_end",
            "close_info",
            "close_ex_info")
    }

    var id: Int = 0
    var type: Int = 0
    var name: String = ""
    var latitude = 0.0
    var longitude = 0.0
    var time: String = ""
    var is_open: Int = 0
    var open_01_start: IntArray = intArrayOf() // [時, 分]
    var open_01_end: IntArray = intArrayOf()
    var open_02_start: IntArray = intArrayOf()
    var open_02_end: IntArray = intArrayOf()
    var is_open_time_limit: Boolean = false
    var sub_is_open_limit: IntArray = intArrayOf()
    var sub_open_01_01_start: IntArray = intArrayOf()
    var sub_open_01_01_end: IntArray = intArrayOf()
    var sub_open_01_02_start: IntArray = intArrayOf()
    var sub_open_01_02_end: IntArray = intArrayOf()
    var sub_open_02_01_start: IntArray = intArrayOf()
    var sub_open_02_01_end: IntArray = intArrayOf()
    var sub_open_02_02_start: IntArray = intArrayOf()
    var sub_open_02_02_end: IntArray = intArrayOf()
    var sub_open_03_01_start: IntArray = intArrayOf()
    var sub_open_03_01_end: IntArray = intArrayOf()
    var sub_open_03_02_start: IntArray = intArrayOf()
    var sub_open_03_02_end: IntArray = intArrayOf()
    var sub_open_04_01_start: IntArray = intArrayOf()
    var sub_open_04_01_end: IntArray = intArrayOf()
    var sub_open_04_02_start: IntArray = intArrayOf()
    var sub_open_04_02_end: IntArray = intArrayOf()
    var sub_open_05_01_start: IntArray = intArrayOf()
    var sub_open_05_01_end: IntArray = intArrayOf()
    var sub_open_05_02_start: IntArray = intArrayOf()
    var sub_open_05_02_end: IntArray = intArrayOf()
    var sub_open_06_01_start: IntArray = intArrayOf()
    var sub_open_06_01_end: IntArray = intArrayOf()
    var sub_open_06_02_start: IntArray = intArrayOf()
    var sub_open_06_02_end: IntArray = intArrayOf()
    var sub_open_07_01_start: IntArray = intArrayOf()
    var sub_open_07_01_end: IntArray = intArrayOf()
    var sub_open_07_02_start: IntArray = intArrayOf()
    var sub_open_07_02_end: IntArray = intArrayOf()
    var sub_open_08_01_start: IntArray = intArrayOf()
    var sub_open_08_01_end: IntArray = intArrayOf()
    var sub_open_08_02_start: IntArray = intArrayOf()
    var sub_open_08_02_end: IntArray = intArrayOf()
    var sub_open_09_01_start: IntArray = intArrayOf()
    var sub_open_09_01_end: IntArray = intArrayOf()
    var sub_open_09_02_start: IntArray = intArrayOf()
    var sub_open_09_02_end: IntArray = intArrayOf()
    var close_info: IntArray = intArrayOf()
    var close_ex_info: IntArray = intArrayOf()

    init {
        this.id = id
        this.type = type
        this.name = name
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
    }

    fun setOpenData(
        is_open: Int,
        open_01_start: IntArray,
        open_01_end: IntArray,
        open_02_start: IntArray,
        open_02_end: IntArray,
        is_open_time_limit: Boolean,
        sub_is_open_limit: IntArray,
        sub_open_01_01_start: IntArray,
        sub_open_01_01_end: IntArray,
        sub_open_01_02_start: IntArray,
        sub_open_01_02_end: IntArray,
        sub_open_02_01_start: IntArray,
        sub_open_02_01_end: IntArray,
        sub_open_02_02_start: IntArray,
        sub_open_02_02_end: IntArray,
        sub_open_03_01_start: IntArray,
        sub_open_03_01_end: IntArray,
        sub_open_03_02_start: IntArray,
        sub_open_03_02_end: IntArray,
        sub_open_04_01_start: IntArray,
        sub_open_04_01_end: IntArray,
        sub_open_04_02_start: IntArray,
        sub_open_04_02_end: IntArray,
        sub_open_05_01_start: IntArray,
        sub_open_05_01_end: IntArray,
        sub_open_05_02_start: IntArray,
        sub_open_05_02_end: IntArray,
        sub_open_06_01_start: IntArray,
        sub_open_06_01_end: IntArray,
        sub_open_06_02_start: IntArray,
        sub_open_06_02_end: IntArray,
        sub_open_07_01_start: IntArray,
        sub_open_07_01_end: IntArray,
        sub_open_07_02_start: IntArray,
        sub_open_07_02_end: IntArray,
        sub_open_08_01_start: IntArray,
        sub_open_08_01_end: IntArray,
        sub_open_08_02_start: IntArray,
        sub_open_08_02_end: IntArray,
        sub_open_09_01_start: IntArray,
        sub_open_09_01_end: IntArray,
        sub_open_09_02_start: IntArray,
        sub_open_09_02_end: IntArray,
        close_info: IntArray,
        close_ex_info: IntArray
    ) {
        this.is_open = is_open
        this.open_01_start = open_01_start
        this.open_01_end = open_01_end
        this.open_02_start = open_02_start
        this.open_02_end = open_02_end
        this.is_open_time_limit = is_open_time_limit
        this.sub_is_open_limit = sub_is_open_limit
        this.sub_open_01_01_start = sub_open_01_01_start
        this.sub_open_01_01_end = sub_open_01_01_end
        this.sub_open_01_02_start = sub_open_01_02_start
        this.sub_open_01_02_end = sub_open_01_02_end
        this.sub_open_02_01_start = sub_open_02_01_start
        this.sub_open_02_01_end = sub_open_02_01_end
        this.sub_open_02_02_start = sub_open_02_02_start
        this.sub_open_02_02_end = sub_open_02_02_end
        this.sub_open_03_01_start = sub_open_03_01_start
        this.sub_open_03_01_end = sub_open_03_01_end
        this.sub_open_03_02_start = sub_open_03_02_start
        this.sub_open_03_02_end = sub_open_03_02_end
        this.sub_open_04_01_start = sub_open_04_01_start
        this.sub_open_04_01_end = sub_open_04_01_end
        this.sub_open_04_02_start = sub_open_04_02_start
        this.sub_open_04_02_end = sub_open_04_02_end
        this.sub_open_05_01_start = sub_open_05_01_start
        this.sub_open_05_01_end = sub_open_05_01_end
        this.sub_open_05_02_start = sub_open_05_02_start
        this.sub_open_05_02_end = sub_open_05_02_end
        this.sub_open_06_01_start = sub_open_06_01_start
        this.sub_open_06_01_end = sub_open_06_01_end
        this.sub_open_06_02_start = sub_open_06_02_start
        this.sub_open_06_02_end = sub_open_06_02_end
        this.sub_open_07_01_start = sub_open_07_01_start
        this.sub_open_07_01_end = sub_open_07_01_end
        this.sub_open_07_02_start = sub_open_07_02_start
        this.sub_open_07_02_end = sub_open_07_02_end
        this.sub_open_08_01_start = sub_open_08_01_start
        this.sub_open_08_01_end = sub_open_08_01_end
        this.sub_open_08_02_start = sub_open_08_02_start
        this.sub_open_08_02_end = sub_open_08_02_end
        this.sub_open_09_01_start = sub_open_09_01_start
        this.sub_open_09_01_end = sub_open_09_01_end
        this.sub_open_09_02_start = sub_open_09_02_start
        this.sub_open_09_02_end = sub_open_09_02_end
        this.close_info = close_info
        this.close_ex_info = close_ex_info
    }

    fun getGroupOpen(): HashMap<Int, IntArray> {
        val rArray = HashMap<Int, IntArray>()
        rArray[0] = this.open_01_start
        rArray[1] = this.open_01_end
        rArray[2] = this.open_02_start
        rArray[3] = this.open_02_end
        return rArray
    }

    fun getGroupSub_open(target: Int): HashMap<Int, IntArray> {
        val rArray = HashMap<Int, IntArray>()
        when (target) {
            1 -> {
                rArray[0] = sub_open_01_01_start
                rArray[1] = sub_open_01_01_end
                rArray[2] = sub_open_01_02_start
                rArray[3] = sub_open_01_02_end
            }
            2 -> {
                rArray[0] = sub_open_02_01_start
                rArray[1] = sub_open_02_01_end
                rArray[2] = sub_open_02_02_start
                rArray[3] = sub_open_02_02_end
            }
            3 -> {
                rArray[0] = sub_open_03_01_start
                rArray[1] = sub_open_03_01_end
                rArray[2] = sub_open_03_02_start
                rArray[3] = sub_open_03_02_end
            }
            4 -> {
                rArray[0] = sub_open_04_01_start
                rArray[1] = sub_open_04_01_end
                rArray[2] = sub_open_04_02_start
                rArray[3] = sub_open_04_02_end
            }
            5 -> {
                rArray[0] = sub_open_05_01_start
                rArray[1] = sub_open_05_01_end
                rArray[2] = sub_open_05_02_start
                rArray[3] = sub_open_05_02_end
            }
            6 -> {
                rArray[0] = sub_open_06_01_start
                rArray[1] = sub_open_06_01_end
                rArray[2] = sub_open_06_02_start
                rArray[3] = sub_open_06_02_end
            }
            7 -> {
                rArray[0] = sub_open_07_01_start
                rArray[1] = sub_open_07_01_end
                rArray[2] = sub_open_07_02_start
                rArray[3] = sub_open_07_02_end
            }
            8 -> {
                rArray[0] = sub_open_08_01_start
                rArray[1] = sub_open_08_01_end
                rArray[2] = sub_open_08_02_start
                rArray[3] = sub_open_08_02_end
            }
            9 -> {
                rArray[0] = sub_open_09_01_start
                rArray[1] = sub_open_09_01_end
                rArray[2] = sub_open_09_02_start
                rArray[3] = sub_open_09_02_end
            }
            else -> {
            }
        }
        return rArray
    }
}