package net.tochinavi.www.tochinaviapp.entities

import android.graphics.Color
import android.graphics.Typeface

class DataListSimple(
    title: String,
    isArrow: Boolean = false
) {
    var title: String = ""
    var isArrow: Boolean = false
    var titleColor: Int = Color.BLACK
    var titleStyle: Typeface = Typeface.DEFAULT
    var titleSize: Float = 14f // sp

    companion object {
        val TAG = "DataListSimple"
    }

    init {
        this.title = title
        this.isArrow = isArrow
    }
}