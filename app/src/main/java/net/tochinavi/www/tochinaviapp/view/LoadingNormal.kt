package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.loading_normal.*
import net.tochinavi.www.tochinaviapp.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class LoadingNormal : DialogFragmentFullScreen() {

    companion object {
        const val TAG = "LoadingNormal"

        @JvmStatic
        fun newInstance(
            message: String? = null,
            isProgress: Boolean
        ) = LoadingNormal().apply {
            arguments = Bundle().apply {
                putString(LoadingNormal::message.name, message)
                putBoolean(LoadingNormal::isProgress.name, isProgress)
            }
        }
    }

    private object UNINITIALIZED_VALUE_FOR_ARGMENTS
    @Suppress("UNCHECKED_CAST")
    class Arguments<T> : ReadWriteProperty<Fragment, T> {

        var field: Any? = UNINITIALIZED_VALUE_FOR_ARGMENTS

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            if (field == UNINITIALIZED_VALUE_FOR_ARGMENTS) {
                field = thisRef.arguments?.get(property.name)
            }
            return field as T
        }

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
            field = value
        }
    }

    private var dismissFlag: Boolean = false

    // 初期オプション
    private var message: String? by Arguments()
    private var isProgress: Boolean by Arguments()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 背景クリックで閉じないようにする
        this.isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.loading_normal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutDialog.setOnTouchListener { _, _ -> true }
        setLayout()
    }

    /*
    override fun onResume() {
        if (dismissFlag) { // dismiss a dialog
            Log.i(">> $TAG", "onResume is dismissFlag")
            val f = fragmentManager!!.findFragmentByTag(TAG)
            if (f is DialogFragmentFullScreen) {
                val df: DialogFragmentFullScreen? = f as DialogFragmentFullScreen?
                df!!.dismiss()
                fragmentManager!!.beginTransaction().remove(f!!).commit()
                dismissFlag = false
            }
        } else {
            Log.i(">> $TAG", "onResume is not dismissFlag")
        }
        super.onResume()
    }

    override fun dismiss() {
        if (isResumed()) super.dismiss() // dismiss now
        else dismissFlag = true // dismiss on onResume
    }
    */

    private fun setLayout() {
        if (textViewMsg == null || progressBar == null) { return }

        // メッセージ
        if (message == null || message!!.isEmpty()) {
            textViewMsg.visibility = View.GONE
        } else {
            textViewMsg.visibility = View.VISIBLE
            textViewMsg.text = message
        }

        // プログレス
        progressBar.visibility = if (isProgress) View.VISIBLE else View.GONE
    }

    /**
     * プログレスのレイアウト更新
     */
    fun updateLayout(_message: String?, _isProgress: Boolean) {
        message = _message
        isProgress = _isProgress
        setLayout()
    }

    /**
     * プログレスのレイアウト更新
     * _message: フォーマット
     * readNum: 現状(Double)
     * totalNum: 総数(Double)
     */
    fun updateLayout(_message: String, readNum: Double, totalNum: Double) {
        val decimal = readNum / totalNum
        val status = ("%,.2f".format(decimal)).toDouble() * 100
        message = _message.format(status.toInt())
        isProgress = true
        setLayout()
    }

    fun onDismiss(delay: Long = 0) {
        if (delay > 0) {
            Handler().postDelayed(Runnable {
                dismiss()
            }, delay)
        } else {
            dismiss()
        }
    }

}
