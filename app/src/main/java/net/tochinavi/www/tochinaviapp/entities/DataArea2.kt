package net.tochinavi.www.tochinaviapp.entities

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

/*
public static final String TAG = "area2";
    public static final String TABLE_NAME = "area2";
    public static final String TABLE_NAME_OMISSION = "area2";
    public static final String COL[] = {
            "id",
            "name",
            "parent_id"
    };

    private Integer id;
    private String name;
    private Integer parent_id;
 */

class DataArea2(
    id: Int = 0,
    name: String = "",
    parent_id: Int = 0
): Parcelable {
    var id: Int = 0
    var name: String = ""
    var parent_id: Int = 0

    companion object {
        val TAG = "DataArea2"
        val TABLE_NAME = "area2"
        val TABLE_NAME_OMISSION = "area2"
        val COL = arrayOf(
            "id",
            "name",
            "parent_id"
        )

        @JvmField
        val CREATOR: Parcelable.Creator<DataArea2> = object : Parcelable.Creator<DataArea2> {
            override fun createFromParcel(`in`: Parcel): DataArea2 {
                return DataArea2(
                    `in`.readInt(),
                    `in`.readString()!!,
                    `in`.readInt()
                )
            }

            override fun newArray(size: Int): Array<DataArea2?> {
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