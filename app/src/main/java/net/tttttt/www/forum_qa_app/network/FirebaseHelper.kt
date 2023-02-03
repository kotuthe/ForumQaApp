package net.tttttt.www.forum_qa_app.network

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*

class FirebaseHelper(private val context: Context) {


    // アナリティクスタイプ
    enum class Type {
        Screen, Event
    }

    // スクリーン名
    enum class screenName {
        Top,
        Search_Near_List,
        Search_List,
        Ranking,
        Mypage,
        Mypage_Badge_List,
        Mypage_Checkin_List,
        Mypage_Kuchikomi_List,
        Mypage_Draft_List,
        Mypage_Favorite_List,
        Mypage_Setting,
        Spot_Info,
        Hospital_Info,
        Spot_Info_Detail,
        Spot_Info_Kuchikomi_List,
        Spot_Info_Kuchikomi_Detail,
        Spot_Info_Map,
        Hospital_Info_Map,
        Kuchikomi_Image_List,
        Kuchikomi_Image,  // new 2020
        IS_Spot_Info,
        IS_Spot_Info_Kuchikomi_Detail
    }

    // イベントカテゴリー
    enum class eventCategory {
        Button, Image, Cell, Label
    }

    // イベントアクション
    enum class eventAction {
        Tap
    }

    companion object {
        var TAG = "FirebaseHelper"
    }

    /**
     * スクリーンを送信
     * @param screen
     * @param args
     */
    fun sendScreen(
        screen: screenName,
        args: ArrayList<Pair<String, Any>>?
    ) {
        val fa = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        if (args != null && args.size > 0) {
            for (i in args.indices) {
                val arg: Pair<String, Any> = args[i]
                val key: String = arg.first
                val value: Any = arg.second
                // 数値の場合はint,それ以外はstring
                if (value is Int) {
                    params.putInt(key, value)
                } else {
                    params.putString(key, value as String)
                }
            }
        }
        fa.logEvent(screen.name, params)
    }

    /**
     * イベントを送信
     * @param screen
     * @param category
     * @param action
     * @param label
     */
    fun sendEvent(
        screen: screenName,
        category: eventCategory,
        action: eventAction?,
        label: String
    ) {
        val fa = FirebaseAnalytics.getInstance(context)
        val params = Bundle()
        params.putString("screen", screen.name)
        params.putString("layout", category.name + ": " + label)
        fa.logEvent(Type.Event.name, params)
    }

    /**
     * スポットページの遷移元を送信
     * @param fromScreen
     * @param type
     * @param spotId
     */
    fun sendSpotInfo(fromScreen: screenName, type: Int, spotId: Int) {
        val args: ArrayList<Pair<String, Any>> = ArrayList<Pair<String, Any>>(
            listOf<Pair<String, Any>>(
                "id" to spotId.toString(),
                "trans_screen" to fromScreen.name
            )
        )
        if (type == 1) {
            // スポット
            sendScreen(screenName.Spot_Info, args)
        } else {
            // 病院
            sendScreen(screenName.Hospital_Info, args)
        }
    }

    /**
     * ImageSearch ver: スポットページの遷移元を送信
     * @param fromScreen
     * @param type
     * @param spotId
     */
    fun sendISSpotInfo(fromScreen: screenName, type: Int, spotId: Int) {
        val args: ArrayList<Pair<String, Any>> = ArrayList<Pair<String, Any>>(
            listOf<Pair<String, Any>>(
                "id" to spotId.toString(),
                "trans_screen" to fromScreen.name
            )
        )
        if (type == 1) {
            // スポット
            sendScreen(screenName.IS_Spot_Info, args)
        } else {
            /*
            // 病院
            sendScreen(screenName.IS_Hospital_Info, new ArrayList<DataAnalyticsParam>(Arrays.<DataAnalyticsParam>asList(
                    new DataAnalyticsParam("id", String.valueOf(spotId)),
                    new DataAnalyticsParam("trans_screen", fromScreen.name())
            )));
             */
        }
    }


}