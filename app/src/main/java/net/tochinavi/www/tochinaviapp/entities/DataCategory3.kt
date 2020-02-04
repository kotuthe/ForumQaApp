package net.tochinavi.www.tochinaviapp.entities

import android.os.Parcel
import android.os.Parcelable
import android.util.Log


class DataCategory3(
    id: Int = 0,
    name: String = "",
    parent_id: Int = 0
): Parcelable {
    var id: Int = 0
    var name: String = ""
    var parent_id: Int = 0

    companion object {

        val TAG = "DataCategory3"
        val TABLE_NAME = "category3"
        val TABLE_NAME_OMISSION = "ca3"
        val COL = arrayOf(
            "id",
            "name",
            "parent_id"
        )
        val LEVEL: Int = 3

        @JvmField
        val CREATOR: Parcelable.Creator<DataCategory3> = object : Parcelable.Creator<DataCategory3> {
            override fun createFromParcel(`in`: Parcel): DataCategory3 {
                return DataCategory3(
                    `in`.readInt(),
                    `in`.readString()!!,
                    `in`.readInt()
                )
            }

            override fun newArray(size: Int): Array<DataCategory3?> {
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

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeInt(id)
        dest!!.writeString(name)
        dest!!.writeInt(parent_id)
    }

}