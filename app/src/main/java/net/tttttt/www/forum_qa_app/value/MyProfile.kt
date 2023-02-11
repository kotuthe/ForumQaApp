package net.tttttt.www.forum_qa_app.value

class MyProfile() {

    // 性別 //
    private val genders: HashMap<Int, String> = linkedMapOf(
        0 to "非公開",
        1 to "男性",
        2 to "女性",
        3 to "その他",
    )

    init {
    }

    /** 性別 **/
    fun getGenders(): HashMap<Int, String> {
        return genders
    }

    fun getGenderTexts(): Array<String> {
        val rArray: ArrayList<String> = arrayListOf()
        for (v in genders.values) {
            rArray.add(v)
        }
        return rArray.toTypedArray()
    }

    fun getGenderId(text: String): Int {
        return getMapKey(genders, text)
    }

    fun getGenderText(id: Int): String {
        return genders.get(id)!!
    }


}