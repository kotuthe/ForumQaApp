package net.tttttt.www.forum_qa_app.entities

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

class DataCategory1(
    id: Int = 0,
    name: String = "",
    sub_title: String = ""
): Parcelable {
    var id: Int = 0
    var name: String = ""
    var sub_title: String = ""

    companion object {

        val TAG = "DataCategory1"
        val TABLE_NAME = "category1"
        val TABLE_NAME_OMISSION = "ca1"
        val COL = arrayOf(
            "id",
            "name",
            "sub_title"
        )
        val LEVEL: Int = 1

        @JvmField
        val CREATOR: Parcelable.Creator<DataCategory1> = object : Parcelable.Creator<DataCategory1> {
            override fun createFromParcel(`in`: Parcel): DataCategory1 {
                return DataCategory1(
                    `in`.readInt(),
                    `in`.readString()!!,
                    `in`.readString()!!
                )
            }

            override fun newArray(size: Int): Array<DataCategory1?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
        this.id = id
        this.name = name
        this.sub_title = sub_title
    }

    fun _debug_log() {
        Log.i(TAG, "id: $id, name: $name, sub_title: $sub_title")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, flags: Int) {
        p0.writeInt(id)
        p0.writeString(name)
        p0.writeString(sub_title)
    }

}