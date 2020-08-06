package net.tochinavi.www.tochinaviapp.view

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment

open class DialogFragmentFullScreen : DialogFragment() {

    private var isOnDestroy: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener { _, _ ->
            if (isCancelable) dismiss()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        // ダイアログを全画面にする
        dialog?.window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isOnDestroy = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        // DestroyするまではDismissできる
        if (!isOnDestroy) {
            super.onDismiss(dialog)
        }
    }
}