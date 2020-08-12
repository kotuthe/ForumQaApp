package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ExpandableListView

class ExpandableListViewDyamicFit @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ExpandableListView(context, attrs) {

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpec_custom = View.MeasureSpec.makeMeasureSpec(
            Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom)
        val params = layoutParams
        params.height = measuredHeight
    }
}