package net.tochinavi.www.tochinaviapp.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase

/**
 * Created by kotouno on 2017/04/27.
 */

class DBHelper(context: Context) {

    companion object {
        val TAG = "DBHelper"
    }

    var db: SQLiteDatabase? = null
    private val dbOpenHelper: DBOpenHelper

    init {
        this.dbOpenHelper = DBOpenHelper(context)
        establishDb()
    }

    private fun establishDb() {
        if (this.db == null) {
            this.db = this.dbOpenHelper.getWritableDatabase()
        }
    }

    fun cleanup() {
        if (this.db != null) {
            this.db!!.close()
            this.db = null
        }
    }

    /**
     * Databaseが削除できればtrue。できなければfalse
     * @param context
     * @return
     */
    fun isDatabaseDelete(context: Context): Boolean {
        var result = false
        /*if (this.db != null) {
            val file = context.getDatabasePath(dbOpenHelper.getDatabaseName())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                result = this.db!!.deleteDatabase(file) // kotlinでここがわからない
            }
        }*/
        return result
    }

    fun beginTransaction() {
        if (this.db != null) {
            this.db!!.beginTransaction()
        }
    }

    fun endTransaction() {
        if (this.db != null) {
            this.db!!.endTransaction()
        }
    }

    fun setTransactionSuccessful() {
        if (this.db != null) {
            this.db!!.setTransactionSuccessful()
        }
    }
}
