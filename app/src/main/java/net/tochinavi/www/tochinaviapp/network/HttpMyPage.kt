package net.tochinavi.www.tochinaviapp.network

import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import net.tochinavi.www.tochinaviapp.entities.DataMySpotList
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import org.json.JSONObject

class HttpMyPage(context: Context) {

    companion object {
        val TAG = "HttpMyPage"
    }

    private var mContext: Context = context
    private var mySP: MySharedPreferences = MySharedPreferences(context)

    fun get_checkin_list(
        page: Int,
        thenPart: (ArrayList<DataMySpotList>, Int) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }
        params.add("page" to page)
        val url = MyString().my_http_url_app() + "/mypage/get_checkin_history.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val rArray: ArrayList<DataMySpotList> = ArrayList()
                    val number = datas.getInt("number")
                    val json_array = datas.getJSONArray("spot_array")
                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        rArray.add(DataMySpotList(
                            obj.getInt("spot_id"),
                            obj.getInt("type"),
                            obj.getString("name"),
                            obj.getString("address"),
                            obj.getString("category"),
                            obj.getString("image"),
                            obj.getString("date")
                        ))
                    }
                    thenPart(rArray, number)
                } else {
                    // データなし
                    elsePart(Constants.HTTP_STATUS.nodata)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                elsePart(Constants.HTTP_STATUS.network)
            })
        }
    }


    fun get_wish_list(
        page: Int,
        thenPart: (ArrayList<DataMySpotList>, Int) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }
        params.add("page" to page)
        val url = MyString().my_http_url_app() + "/mypage/get_wish_list.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val rArray: ArrayList<DataMySpotList> = ArrayList()
                    val number = datas.getInt("number")
                    val json_array = datas.getJSONArray("wishlist")
                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        rArray.add(DataMySpotList(
                            obj.getInt("id"),
                            obj.getInt("type"),
                            obj.getString("name"),
                            obj.getString("address"),
                            obj.getString("category"),
                            obj.getString("image"),
                            ""
                        ))
                    }
                    thenPart(rArray, number)
                } else {
                    // データなし
                    elsePart(Constants.HTTP_STATUS.nodata)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                elsePart(Constants.HTTP_STATUS.network)
            })
        }
    }

}