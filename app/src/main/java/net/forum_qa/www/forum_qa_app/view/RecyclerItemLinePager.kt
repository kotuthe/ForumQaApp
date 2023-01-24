package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.value.convertDpToPx

class RecyclerItemLinePager(context: Context) : ItemDecoration() {

    companion object {}

    private val colorActive = ContextCompat.getColor(context, R.color.colorPagerItemActive)
    private val colorInactive = ContextCompat.getColor(context, R.color.colorPagerItem)

    /**
     * Height of the space the indicator takes up at the bottom of the view.
     */
    private val mIndicatorHeight = 20f.convertDpToPx(context).toInt()

    /**
     * Indicator stroke width.
     */
    private val mIndicatorStrokeWidth = 2f.convertDpToPx(context)

    /**
     * Indicator width.
     */
    private val mIndicatorItemLength = 10f.convertDpToPx(context)
    /**
     * Padding between indicators.
     */
    private val mIndicatorItemPadding = 8f.convertDpToPx(context)

    /**
     * Some more natural animation interpolation
     */
    private val mInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val mPaint: Paint = Paint()

    init {
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = mIndicatorStrokeWidth
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = mIndicatorHeight
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter!!.itemCount
        if (itemCount == 1) { return }

        val totalLength = mIndicatorItemLength * itemCount
        val paddingBetweenItems =
            Math.max(0, itemCount - 1) * mIndicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - mIndicatorHeight / 2f
        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)


        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager?
        val activePosition = layoutManager!!.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition)
        val left = activeChild!!.left
        val width = activeChild!!.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress: Float =
            mInterpolator.getInterpolation(left * -1 / width.toFloat())
        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount)
    }

    /**
     * アイテムの描画（丸）
     */
    private fun drawItem(c: Canvas, l: Float ,t: Float, r: Float, b: Float) {
        val rectf = RectF(l, t, r, b + mIndicatorItemLength)
        c.drawArc(rectf, 0f, 360f, false, mPaint)
    }

    /**
     * インジケーター
     */
    private fun drawInactiveIndicators(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        mPaint.color = colorInactive

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding
        var start = indicatorStartX
        for (i in 0 until itemCount) {
            drawItem(c, start, indicatorPosY, start + mIndicatorItemLength, indicatorPosY)
            start += itemWidth
        }
    }

    /**
     * ハイライト
     */
    private fun drawHighlights(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int, progress: Float, itemCount: Int
    ) {
        mPaint.color = colorActive

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding
        if (progress == 0f) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            drawItem(c,
                highlightStart, indicatorPosY,
                highlightStart + mIndicatorItemLength, indicatorPosY
            )
        } else {
            var highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = mIndicatorItemLength * progress

            // draw the cut off highlight
            drawItem(c,
                highlightStart + partialLength, indicatorPosY,
                highlightStart + mIndicatorItemLength, indicatorPosY
            )

            // draw the highlight overlapping to the next item as well
            if (highlightPosition < itemCount - 1) {
                highlightStart += itemWidth
                drawItem(c,
                    highlightStart, indicatorPosY,
                    highlightStart + partialLength, indicatorPosY
                )
            }
        }
    }
}