package net.tochinavi.www.tochinaviapp.view

import android.view.MotionEvent

/**
 * ListViewなどのTouchイベントの速度変更
 */
class TouchListenerSetSpeed {
    private var _horizontalSpeedRate = 0.7f
    private var _verticalSpeedRate = 0.7f
    private var _downX = 0f
    private var _downY = 0f

    fun setOnTouch(ev: MotionEvent) {
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE
            || action == MotionEvent.ACTION_UP
        ) {
            val x = (ev.x - _downX) * _horizontalSpeedRate + _downX
            val y = (ev.y - _downY) * _verticalSpeedRate + _downY
            ev.setLocation(x, y)
        } else if (action == MotionEvent.ACTION_DOWN) {
            _downX = ev.x
            _downY = ev.y
        }
    }

    fun setSpeed(speed: Float) {
        this.setSpeed(speed, speed)
    }

    fun setSpeed(xs: Float, ys: Float) {
        _horizontalSpeedRate = xs
        _verticalSpeedRate = ys
    }
}