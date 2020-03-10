package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewSlowScroll: RecyclerView {
    /**
     * コンストラクタ
     * @param context
     */
    constructor(context: Context?) : super(context!!) {}

    /**
     * コンストラクタ
     * @param context
     * @param attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {}

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {}


    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        // double weight = 0.6; // 1.0(fast) ~ 0.0(slow)
        var vx = 0
        var vy = 0
        if (velocityX != 0) { vx = (velocityX * 0.4).toInt() }
        if (velocityY != 0) { vy = (velocityY * 0.6).toInt() }
        return super.fling(vx, vy);
    }
}