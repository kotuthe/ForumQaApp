package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Checkable
import android.widget.FrameLayout
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.databinding.ListviewItemCheckableBinding

class ListViewItemCheckable : FrameLayout, Checkable {
    // private var mRadioButton: RadioButton? = null

    private val binding = ListviewItemCheckableBinding.inflate(LayoutInflater.from(context), this, true)

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
        return binding.radioButton.isChecked
    }

    override fun setChecked(checked: Boolean) { // RadioButton の表示を切り替える
        // mRadioButton!!.isChecked = checked
        binding.radioButton.isChecked = checked
    }

    override fun toggle() {}
}