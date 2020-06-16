package net.tochinavi.www.tochinaviapp.view


import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import net.tochinavi.www.tochinaviapp.R


class ButtonWhiteGradient(context: Context, attr: AttributeSet) : androidx.appcompat.widget.AppCompatButton(context, attr, R.attr.borderlessButtonStyle) {

    init {
        this.setBackgroundResource(R.drawable.btn_white_gradient)
        this.setTextColor(Color.BLACK)
    }

    override fun setPressed(pressed: Boolean) {
        val diffScale: Float = 0.95f
        if (pressed) {
            /* 押してる時 */
            val scaleAnimation = ScaleAnimation(
                1.0f, diffScale / 1.0f, 1.0f, diffScale / 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.duration = 50
            scaleAnimation.repeatCount = 0
            scaleAnimation.fillAfter = true

            this.startAnimation(scaleAnimation)
        } else {
            /* 放した時 */
            val scaleAnimation = ScaleAnimation(
                diffScale / 1.0f, 1.0f, diffScale / 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation.duration = 100
            scaleAnimation.repeatCount = 0
            scaleAnimation.fillAfter = true

            this.startAnimation(scaleAnimation)
        }

        super.setPressed(pressed)
    }
}