package net.tochinavi.www.tochinaviapp.entities

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize

class DataArea1(
    id: Int = 0,
    name: String = ""
): Parcelable {
    var id: Int = 0
    var name: String = ""

    companion object {

        val TAG = "DataArea1"
        val TABLE_NAME = "area1"
        val TABLE_NAME_OMISSION = "area1"
        val COL = arrayOf(
            "id",
            "name"
        )

        @JvmField
        val CREATOR: Parcelable.Creator<DataArea1> = object : Parcelable.Creator<DataArea1> {
            override fun createFromParcel(`in`: Parcel): DataArea1 {
                return DataArea1(
                    `in`.readInt(),
                    `in`.readString()!!
                )
            }

            override fun newArray(size: Int): Array<DataArea1?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
        this.id = id
        this.name = name
    }


    fun _debug_log() {
        Log.i(TAG, "id: $id, name: $name")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, flags: Int) {
        p0.writeInt(id)
        p0.writeString(name)
    }

}
