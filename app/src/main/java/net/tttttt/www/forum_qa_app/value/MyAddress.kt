package net.tttttt.www.forum_qa_app.value

import net.tttttt.www.forum_qa_app.R
import java.util.HashMap

class MyAddress() {

    // 県 //
    private val prefectures: HashMap<Int, String> = linkedMapOf(
        1 to "北海道",
        2 to "青森県",
        3 to "岩手県",
        4 to "宮城県",
        5 to "秋田県",
        6 to "山形県",
        7 to "福島県",
        8 to "茨城県",
        9 to "栃木県",
        10 to "群馬県",
        11 to "埼玉県",
        12 to "千葉県",
        13 to "東京都",
        14 to "神奈川県",
        15 to "新潟県",
        16 to "富山県",
        17 to "石川県",
        18 to "福井県",
        19 to "山梨県",
        20 to "長野県",
        21 to "岐阜県",
        22 to "静岡県",
        23 to "愛知県",
        24 to "三重県",
        25 to "滋賀県",
        26 to "京都府",
        27 to "大阪府",
        28 to "兵庫県",
        29 to "奈良県",
        30 to "和歌山県",
        31 to "鳥取県",
        32 to "島根県",
        33 to "岡山県",
        34 to "広島県",
        35 to "山口県",
        36 to "徳島県",
        37 to "香川県",
        38 to "愛媛県",
        39 to "高知県",
        40 to "福岡県",
        41 to "佐賀県",
        42 to "長崎県",
        43 to "熊本県",
        44 to "大分県",
        45 to "宮崎県",
        46 to "鹿児島県",
        47 to "沖縄県"
    )

    init {
    }

    /** 県 **/
    fun getPrefectures(): HashMap<Int, String> {
        return prefectures
    }

    fun getPrefectureTexts(): Array<String> {
        val rArray: ArrayList<String> = arrayListOf()
        for (v in prefectures.values) {
            rArray.add(v)
        }
        return rArray.toTypedArray()
    }

    fun getPrefId(text: String): Int {
        return getMapKey(prefectures, text)
    }

    fun getPrefText(id: Int): String {
        return prefectures.get(id)!!
    }


}