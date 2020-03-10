package net.tochinavi.www.tochinaviapp.value

import android.graphics.Color

class MyColor() {
    init {
    }

    /** トップメニューの色 **/
    fun top_menu_category(id: Int): Int {
        var hex: String = "#FFFFFF"
        when(id) {
            1 -> { hex = "#EB5170" }
            2 -> { hex = "#F3BB25" }
            3 -> { hex = "#4A8FFF" }
            4 -> { hex = "#61DCFF" }
            5 -> { hex = "#F190C3" }
            6 -> { hex = "#AF81E0" }
            7 -> { hex = "#72BF91" }
        }
        return Color.parseColor(hex)
    }

    fun top_menu_dis_category(id: Int): Int {
        var hex: String = "#FFFFFF"
        when(id) {
            1 -> { hex = "#F0D3D9" }
            2 -> { hex = "#F5E6BF" }
            3 -> { hex = "#C7DCFF" }
            4 -> { hex = "#D7F2FA" }
            5 -> { hex = "#FFE0F0" }
            6 -> { hex = "#F2E6FF" }
            7 -> { hex = "#DEFFEB" }
        }
        return Color.parseColor(hex)
    }

}