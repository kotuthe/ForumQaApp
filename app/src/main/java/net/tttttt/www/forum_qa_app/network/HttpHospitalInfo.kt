package net.tttttt.www.forum_qa_app.network

import android.content.Context
import android.location.Location
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import net.tttttt.www.forum_qa_app.entities.DataBadge
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotInfoDetail
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.Constants
import net.tttttt.www.forum_qa_app.value.MySharedPreferences
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.ifNotNull
import org.json.JSONArray
import org.json.JSONObject

class HttpHospitalInfo(context: Context) {

    companion object {
        val TAG = "HttpHospitalInfo"
    }

    private var mySP: MySharedPreferences = MySharedPreferences(context)
    private var mContext: Context = context

    /**
     * 病院情報取得
     */
    fun get_hospital_info(
        id: Int,
        location: Location?,
        thenPart: (DataSpotInfo, ArrayList<DataSpotInfoDetail>, ArrayList<DataSpotInfoDetail>) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to id)

        if (location != null) {
            params.add("latitude" to location.latitude)
            params.add("longitude" to location.longitude)
        }

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(HttpSpotInfo.TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        val url = MyString().my_http_url_app() + "/spot/get_hospital_info.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val obj = datas.getJSONObject("info")
                    val dataSpot = DataSpotInfo(
                        id,
                        3,
                        obj.getString("image"),
                        false,
                        obj.getString("name"),
                        obj.getString("address"),
                        obj.getString("phone"),
                        "",
                        "",
                        obj.getString("simple_detail"),
                        obj.getString("simple_caption"),
                        obj.getDouble("latitude"),
                        obj.getDouble("longitude"),
                        obj.getInt("category_large_id"),
                        datas.getBoolean("checkin"),
                        true,
                        false,
                        obj.getBoolean("wishlist_flag"),
                        obj.getString("sns_share_text"),
                        obj.getString("sns_share_text_long"),
                        obj.getInt("is_free"),
                        false
                    )

                    val basic_info: JSONArray = datas.getJSONArray("basic_info")
                    val dataTopBasic: ArrayList<DataSpotInfoDetail> = arrayListOf()
                    val dataBottomBasic: ArrayList<DataSpotInfoDetail> = arrayListOf()
                    loop@ for (i in 0..basic_info.length() - 1) {
                        val item: JSONArray = basic_info.getJSONArray(i)
                        val tag = item.get(0) as String
                        var title = item.get(1) as String
                        val value = item.get(2) as String
                        var type: Constants.SPOT_BASIC_INFO_TYPE = Constants.SPOT_BASIC_INFO_TYPE.none

                        when (tag) {
                            "map" -> {
                                continue@loop
                            }
                            "name_full", "name_01_kana" -> {
                                dataTopBasic.add(DataSpotInfoDetail(type, title, value))
                                continue@loop
                            }
                            "address" -> {
                                type = Constants.SPOT_BASIC_INFO_TYPE.address
                            }
                            "tel" -> {
                                type = Constants.SPOT_BASIC_INFO_TYPE.phone
                            }
                            "hp_url" -> {
                                type = Constants.SPOT_BASIC_INFO_TYPE.more_detail
                                title = "ホームページをみる"
                            }
                            "tochinavi_url" -> {
                                type = Constants.SPOT_BASIC_INFO_TYPE.more_detail
                                title = "栃ナビ！サイトでみる"
                            }
                            else -> {

                            }
                        }

                        dataBottomBasic.add(
                            DataSpotInfoDetail(type, title, value))
                    }

                    thenPart(dataSpot, dataTopBasic, dataBottomBasic)
                } else {
                    // データなし
                    elsePart(Constants.HTTP_STATUS.nodata)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(HttpSpotInfo.TAG, error.toString())
                elsePart(Constants.HTTP_STATUS.network)
            })
        }
    }

    /**
     * チェックイン
     */
    fun do_checkin(
        id: Int,
        location: Location?,
        thenPart: (ArrayList<DataBadge>) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("spot_id" to id)

        if (location != null) {
            params.add("latitude" to location.latitude)
            params.add("longitude" to location.longitude)
        }

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

        val url = MyString().my_http_url_app() + "/spot/do_hospital_checkin.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    var badge_array: ArrayList<DataBadge> = arrayListOf()
                    if (datas.get("badge_flag") as Boolean) {
                        // 称号を取得
                        val json_array = datas.getJSONArray("badge")
                        for (i in 0..json_array.length() - 1) {
                            val obj = json_array.getJSONObject(i)
                            badge_array.add(DataBadge(
                                0,
                                obj.getString("name"),
                                obj.getString("image"),
                                obj.getString("description"),
                                true
                            ))
                        }
                    }
                    thenPart(badge_array)
                } else {
                    // チェックイン失敗
                    elsePart(Constants.HTTP_STATUS.nodata)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                elsePart(Constants.HTTP_STATUS.network)
            })
        }
    }

    /**
     * お気に入り
     */
    fun do_favorite(
        id: Int,
        thenPart: () -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to id)

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

        val url = MyString().my_http_url_app() + "/spot/do_hospital_wish.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    // 成功
                    thenPart()
                } else {
                    // 失敗
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