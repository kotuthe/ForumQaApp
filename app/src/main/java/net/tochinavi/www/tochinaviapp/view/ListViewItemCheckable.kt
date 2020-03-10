package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.RadioButton
import kotlinx.android.synthetic.main.listview_item_checkable.view.*
import net.tochinavi.www.tochinaviapp.R

class ListViewItemCheckable : FrameLayout, Checkable {
    // private var mRadioButton: RadioButton? = null

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initialize()
    }

    private fun initialize() {
        // レイアウトを追加する
        addView(View.inflate(context, R.layout.listview_item_checkable, null))
        // mRadioButton = radioButton // findViewById<View>(R.id.radio_button) as RadioButton
    }

    override fun isChecked(): Boolean {
        // return mRadioButton!!.isChecked
        return radioButton.isChecked
    }

    override fun setChecked(checked: Boolean) { // RadioButton の表示を切り替える
        // mRadioButton!!.isChecked = checked
        radioButton.isChecked = checked
    }

    override fun toggle() {}
}