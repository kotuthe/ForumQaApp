package net.tochinavi.www.tochinaviapp.network

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import net.tochinavi.www.tochinaviapp.entities.DataBadge
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoBasic
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.*
import org.json.JSONObject

/**
 * お店の情報ページで使用する通信処理 + 共通で使える処理も
 */
class HttpSpotInfo(context: Context) {

    companion object {
        val TAG = "HttpSpotInfo"
    }

    private var mySP: MySharedPreferences = MySharedPreferences(context)
    private var mContext: Context = context

    /**
     * スポット情報取得
     */
    fun get_spot_info(
        id: Int,
        location: Location?,
        thenPart: (DataSpotInfo) -> Unit,
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
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        val url = MyString().my_http_url_app() + "/spot/get_spot_info.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val obj = datas.getJSONObject("info")
                    val dataSpot = DataSpotInfo(
                        id,
                        obj.getString("image"),
                        obj.getBoolean("more_image"),
                        obj.getString("name"),
                        obj.getString("address"),
                        obj.getString("phone"),
                        obj.getString("hour"),
                        obj.getString("holiday"),
                        obj.getString("simple_detail"),
                        obj.getString("simple_caption"),
                        obj.getDouble("latitude"),
                        obj.getDouble("longitude"),
                        obj.getInt("category_large_id"),
                        datas.getBoolean("checkin"),
                        true,
                        obj.getBoolean("review_photo_flag"),
                        obj.getBoolean("wishlist_flag"),
                        obj.getString("sns_share_text"),
                        obj.getString("sns_share_text_long"),
                        obj.getInt("is_free"),
                        obj.getBoolean("coupon_enable")
                    )
                    thenPart(dataSpot)
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

    /**
     * クチコミ画像を取得
     */
    fun get_review_images(
        spot: DataSpotInfo,
        page: Int,
        thenPart: (ArrayList<DataSpotReview>, Int) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to spot.id)
        params.add("page" to page)
        val url = MyString().my_http_url_app() + "/search_spot/get_spot_review_images.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val rArray: ArrayList<DataSpotReview> = ArrayList()
                    val json_array = datas.getJSONArray("review")
                    val all_number = datas.getInt("all_number")

                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        val js_review_images = obj.getJSONArray("review_images")
                        val review_image_array: ArrayList<String> = arrayListOf()
                        if (js_review_images.length() > 0) {
                            for (j in 0..js_review_images.length() - 1) {
                                review_image_array.add(js_review_images.getString(j))
                            }
                        }

                        if (obj.getBoolean("user_enable")) {
                            // クチコミの写真
                            val dataReview = DataSpotReview(
                                obj.getInt("review_id"),
                                spot.id,
                                spot.name,
                                obj.getInt("user_id"),
                                obj.getString("user_name"),
                                obj.getString("user_icon"),
                                obj.getString("user_detail"),
                                obj.getString("review_date"),
                                obj.getString("user_review"),
                                review_image_array,
                                "",
                                obj.getInt("good_num"),
                                obj.getBoolean("enable_good")
                            )
                            rArray.add(dataReview)

                        } else {
                            // スポットの写真
                            val dataReview = DataSpotReview(
                                obj.getInt("review_id"),
                                spot.id,
                                spot.name,
                                0,
                                "",
                                "",
                                "",
                                "",
                                "",
                                review_image_array,
                                "",
                                0,
                                false
                            )
                            rArray.add(dataReview)
                        }
                    }
                    thenPart(rArray, all_number)

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

    /**
     * クチコミテキストを取得
     */
    fun get_review_texts(
        spot: DataSpotInfo,
        page: Int,
        thenPart: (ArrayList<DataSpotReview>) -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("id" to spot.id)
        params.add("page" to page)
        val url = MyString().my_http_url_app() + "/search_spot/get_spot_review_texts.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val rArray: ArrayList<DataSpotReview> = ArrayList()
                    val json_array = datas.getJSONArray("review")
                    for (i in 0..json_array.length() - 1) {
                        val obj = json_array.getJSONObject(i)
                        val js_review_images = obj.getJSONArray("review_images")
                        val review_image_array: ArrayList<String> = arrayListOf()
                        if (js_review_images.length() > 0) {
                            for (j in 0..js_review_images.length() - 1) {
                                review_image_array.add(js_review_images.getString(j))
                            }
                        }

                        val dataReview = DataSpotReview(
                            obj.getInt("review_id"),
                            spot.id,
                            spot.name,
                            obj.getInt("user_id"),
                            obj.getString("user_name"),
                            obj.getString("user_icon"),
                            obj.getString("user_detail"),
                            obj.getString("review_date"),
                            obj.getString("user_review"),
                            review_image_array,
                            "",
                            obj.getInt("good_num"),
                            obj.getBoolean("enable_good")
                        )
                        rArray.add(dataReview)
                    }
                    thenPart(rArray)
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

        val url = MyString().my_http_url_app() + "/spot/do_spot_checkin.php"
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

        val url = MyString().my_http_url_app() + "/spot/do_spot_wish.php"
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

    /**
     *  クチコミ投稿が可能か判定
     */
    fun check_input_review(
        thenPart: () -> Unit,
        elsePart: (Constants.HTTP_STATUS) -> Unit) {

        val params: ArrayList<Pair<String, Any>> = ArrayList()

        // ログインID
        if (mySP.get_status_login()) {
            val db = DBHelper(mContext)
            try {
                val tableUsers = DBTableUsers(mContext)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        val url = MyString().my_http_url_app() + "/review/check_input_enable.php"
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


    /**
     * 以下共通レイアウトの設定
     */
    fun init_map_position(googleMap: GoogleMap) {
        val sydney = LatLng(-34.0, 151.0) // 宇都宮駅
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    /**
     * 地図の描画更新
     */
    fun update_map_position(googleMap: GoogleMap, lat_lng: LatLng, category_id: Int) {
        var bmpPin = BitmapFactory.decodeResource(
            mContext.resources, MyImage().icon_category_pin(1))
        if (category_id > 0) {
            bmpPin = BitmapFactory.decodeResource(
                mContext.resources,
                MyImage().icon_category_pin(category_id)
            )
        }
        googleMap.addMarker(
            MarkerOptions()
                .position(lat_lng)
                .title("Spot")
                .icon(BitmapDescriptorFactory.fromBitmap(bmpPin))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lat_lng, 15f))
    }

    /**
     * ImageSearch　基本情報アイテム
     */
    fun get_is_basic_list_data(dataSpot: DataSpotInfo): ArrayList<DataSpotInfoBasic> {
        val array: ArrayList<DataSpotInfoBasic> = arrayListOf()
        if (!dataSpot.address.isEmpty()) {
            array.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.address, dataSpot.address))
        }
        if (!dataSpot.phone.isEmpty()) {
            array.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.phone, dataSpot.phone))
        }
        if (!dataSpot.hour.isEmpty()) {
            array.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.hour, dataSpot.hour))
        }
        if (!dataSpot.holiday.isEmpty()) {
            array.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.holiday, dataSpot.holiday))
        }
        if (dataSpot.coupon_enable) {
            array.add(DataSpotInfoBasic(Constants.SPOT_BASIC_INFO_TYPE.coupon, "クーポン情報はこちら"))
        }
        return array
    }



}