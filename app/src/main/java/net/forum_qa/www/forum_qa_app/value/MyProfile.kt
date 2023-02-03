package net.tttttt.www.forum_qa_app.value

import net.tttttt.www.forum_qa_app.R
import java.util.HashMap

class MyProfile() {

    // 性別 //
    private val genders: HashMap<Int, String> = hashMapOf(
        0 to "非公開",
        1 to "男性",
        2 to "女性",
        3 to "その他",
    )

    init {
    }

    /** 県 **/
    fun getGenders(): HashMap<Int, String> {
        return genders
    }

    fun getGenderId(text: String): Int {
        return getMapKey(genders, text)
    }

    fun getGenderText(id: Int): String {
        return genders.get(id)!!
    }


}