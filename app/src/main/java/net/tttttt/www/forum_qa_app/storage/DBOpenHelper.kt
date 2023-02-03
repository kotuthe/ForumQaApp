package net.tttttt.www.forum_qa_app.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by kotouno on 2017/04/27.
 */

class DBOpenHelper(var context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val TAG = "DBOpenHelper"
        val DB_NAME = "forum_qa_app.db"

        /**
         * バージョン履歴
         * ・2 => versionName "2.3"
         * ・3 => versionName "3.0"
         * ・4 => versionName "3.1"
         */
        val DB_VERSION = 4
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate version : " + db.version)
        this.execFileSQL(db, "create_table.sql")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade version : " + db.version)
        Log.d(TAG, "onUpgrade oldVersion : " + oldVersion)
        Log.d(TAG, "onUpgrade newVersion : " + newVersion)

        // データベースバージョンが古い場合はこちらで更新
        if (oldVersion == 1 || oldVersion == 2 || oldVersion == 3) {
            execFileSQL(db, "create_table.sql")
        }
    }

    /**
     * assetsフォルダのSQLファイルを実行する
     * @param db
     * @param fileName
     */
    private fun execFileSQL(db: SQLiteDatabase, fileName: String) {
        var inst: InputStream? = null
        var inReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        try {
            // 文字コード(UTF-8)を指定して、ファイルを読み込み
            inst = context.assets.open(fileName)
            inReader = InputStreamReader(inst!!, "UTF-8")
            reader = BufferedReader(inReader)

            // ファイル内の全ての行を処理
            var s: String? = ""
            do {
                s = reader.readLine()
                if (s != null) {

                    // Log.i(">> execSQL", s)

                    // 先頭と末尾の空白除去
                    s = s.trim { it <= ' ' }

                    // 文字が存在する場合（空白行は処理しない）
                    if (0 < s.length) {
                        // SQL実行
                        db.execSQL(s)
                    }

                }
            } while (s != null)

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

            if (inst != null) {
                try {
                    inst.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            if (inReader != null) {
                try {
                    inReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }


}
