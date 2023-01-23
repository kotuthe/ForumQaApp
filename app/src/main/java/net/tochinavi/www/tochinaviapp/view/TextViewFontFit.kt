package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView


class TextViewFontFit : AppCompatTextView {
    /**
     * コンストラクタ
     * @param context
     */
    constructor(context: Context) : super(context) {}

    /**
     * コンストラクタ
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
    }

    override protected fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        resize()
    }

    /**
     * テキストサイズ調整
     */
    private fun resize() {
        val paint = Paint()
        // Viewの幅
        val viewWidth: Int = this.getWidth()
        // テキストサイズ
        var textSize: Float = getTextSize()
        // Paintにテキストサイズ設定
        paint.textSize = textSize
        // テキストの横幅取得
        var textWidth = paint.measureText(this.getText().toString())
        while (viewWidth < textWidth) { // 横幅に収まるまでループ
            if (MIN_TEXT_SIZE >= textSize) { // 最小サイズ以下になる場合は最小サイズ
                textSize = MIN_TEXT_SIZE
                break
            }
            // テキストサイズをデクリメント
            textSize--
            // Paintにテキストサイズ設定
            paint.textSize = textSize
            // テキストの横幅を再取得
            textWidth = paint.measureText(this.getText().toString())
        }
        // テキストサイズ設定
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    companion object {
        /** 最小のテキストサイズ  */
        private const val MIN_TEXT_SIZE = 10f
    }
}