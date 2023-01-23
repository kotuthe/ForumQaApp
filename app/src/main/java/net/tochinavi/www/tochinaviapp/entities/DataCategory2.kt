package net.tochinavi.www.tochinaviapp.entities

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

class DataCategory2(
    id: Int = 0,
    name: String = "",
    parent_id: Int = 0
): Parcelable {
    var id: Int = 0
    var name: String = ""
    var parent_id: Int = 0

    companion object {

        val TAG = "DataCategory2"
        val TABLE_NAME = "category2"
        val TABLE_NAME_OMISSION = "ca2"
        val COL = arrayOf(
            "id",
            "name",
            "parent_id"
        )
        val LEVEL: Int = 2

        @JvmField
        val CREATOR: Parcelable.Creator<DataCategory2> = object : Parcelable.Creator<DataCategory2> {
            override fun createFromParcel(`in`: Parcel): DataCategory2 {
                return DataCategory2(
                    `in`.readInt(),
                    `in`.readString()!!,
                    `in`.readInt()
                )
            }

            override fun newArray(size: Int): Array<DataCategory2?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
        this.id = id
        this.name = name
        this.parent_id = parent_id
    }

    fun _debug_log() {
        Log.i(TAG, "id: $id, name: $name, parent_id: $parent_id")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, flags: Int) {
        p0.writeInt(id)
        p0.writeString(name)
        p0.writeInt(parent_id)
    }

}