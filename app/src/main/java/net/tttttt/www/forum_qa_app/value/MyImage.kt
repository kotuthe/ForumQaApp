package net.tttttt.www.forum_qa_app.value

import net.tttttt.www.forum_qa_app.R

class MyImage() {
    init {
    }

    /** カテゴリーアイコン **/
    fun icon_category(id: Int): Int {
        when(id) {
            1 -> { return R.drawable.img_category_01 }
            2 -> { return R.drawable.img_category_02 }
            3 -> { return R.drawable.img_category_03 }
            4 -> { return R.drawable.img_category_04 }
            5 -> { return R.drawable.img_category_05 }
            6 -> { return R.drawable.img_category_06 }
            7 -> { return R.drawable.img_category_07 }
        }
        return 0
    }

    fun icon_category_mini(id: Int): Int {
        when(id) {
            1 -> { return R.drawable.img_category_01_mini }
            2 -> { return R.drawable.img_category_02_mini }
            3 -> { return R.drawable.img_category_03_mini }
            4 -> { return R.drawable.img_category_04_mini }
            5 -> { return R.drawable.img_category_05_mini }
            6 -> { return R.drawable.img_category_06_mini }
            7 -> { return R.drawable.img_category_07_mini }
        }
        return 0
    }

    fun icon_category_pin(id: Int): Int {
        when(id) {
            1 -> { return R.drawable.img_category_01_pin }
            2 -> { return R.drawable.img_category_02_pin }
            3 -> { return R.drawable.img_category_03_pin }
            4 -> { return R.drawable.img_category_04_pin }
            5 -> { return R.drawable.img_category_05_pin }
            6 -> { return R.drawable.img_category_06_pin }
            7 -> { return R.drawable.img_category_07_pin }
        }
        return 0
    }
}