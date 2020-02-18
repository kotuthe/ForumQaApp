package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.alert_action_sheet.*
import net.tochinavi.www.tochinaviapp.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AlertActionSheet : DialogFragmentFullScreen() {

    companion object {
        const val TAG = "AlertActionSheet"
        /**
         * requestCodeをクリック時に返します。
         * positiveLabelがnullの場合は「閉じる」ボタンを表示し、他のものがnullの場合は非表示にします。
         */
        @JvmStatic
        fun newInstance(
            requestCode: Int,
            title: String? = null, msg: String? = null,
            actionLabels: Array<String>, negativeLabel: String? = null
        ) = AlertActionSheet().apply {
            arguments = Bundle().apply {
                putInt(AlertActionSheet::requestCode.name, requestCode)
                putString(AlertActionSheet::title.name, title)
                putString(AlertActionSheet::msg.name, msg)
                putStringArray(AlertActionSheet::actionLabels.name, actionLabels)
                putString(AlertActionSheet::negativeLabel.name, negativeLabel)
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

    interface OnSimpleDialogClickListener {
        fun onSimpleDialogActionClick(requestCode: Int, index: Int)
        fun onSimpleDialogNegativeClick(requestCode: Int)
    }

    // ActionButtonの最大数
    private val MAX_ACTION_SIZE = 5

    // 初期オプション
    var listener: OnSimpleDialogClickListener? = null
    private var requestCode: Int by Arguments()
    private var title: String? by Arguments()
    private var msg: String? by Arguments()
    private var actionLabels: Array<String> by Arguments()
    private var negativeLabel: String? by Arguments()

    // その他オプション
    private var mFragment: Fragment? = null
    private var is_action_destructive_01: Boolean = false
    private var is_action_destructive_02: Boolean = false
    private var is_action_destructive_03: Boolean = false
    private var is_action_destructive_04: Boolean = false
    private var is_action_destructive_05: Boolean = false
    private var is_negative_destructive: Boolean = false

    /***  その他オプション ***/
    /**
     * フラグメントでイベントを取得したいとき
     */
    fun setFragment(f: Fragment) {
        mFragment = f
    }

    /**
     * Destructiveモード
     * iOS版テキストが赤になるだけ
     */
    fun setActionDestructive(i: Int) {
        when(i) {
            0 -> {
                is_action_destructive_01 = true
            }
            1 -> {
                is_action_destructive_02 = true
            }
            2 -> {
                is_action_destructive_03 = true
            }
            3 -> {
                is_action_destructive_04 = true
            }
            4 -> {
                is_action_destructive_05 = true
            }
        }
    }

    private fun getActionDestructive(i: Int): Boolean {
        when(i) {
            0 -> {
                return is_action_destructive_01
            }
            1 -> {
                return is_action_destructive_02
            }
            2 -> {
                return is_action_destructive_03
            }
            3 -> {
                return is_action_destructive_04
            }
            4 -> {
                return is_action_destructive_05
            }
        }
        return false
    }

    fun setNegativeDestructive() {
        is_negative_destructive = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Fragmentの場合
        var fragment = targetFragment
        if (fragment == null) {
            fragment = mFragment
        }

        // Activityの場合
        if (fragment is OnSimpleDialogClickListener) {
            listener = fragment
        } else if (context is OnSimpleDialogClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.alert_action_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutDialog.setOnTouchListener { _, _ -> true }

        // タイトル
        if (title == null || title!!.isEmpty()) {
            textViewTitle.visibility = View.GONE
        } else {
            textViewTitle.visibility = View.VISIBLE
            textViewTitle.text = title
        }

        // メッセージ
        if (msg == null || msg!!.isEmpty()) {
            textViewMsg.visibility = View.GONE
        } else {
            textViewMsg.visibility = View.VISIBLE
            textViewMsg.text = msg
        }

        // アクションボタン ※Action1は必須
        if (actionLabels.count() > 0) {
            for (i in 0..actionLabels.count() - 1) {
                val title = actionLabels.get(i)
                var button: TextView? = null
                var separator: View? = null
                when(i) {
                    0 -> {
                        button = buttonAction01
                    }
                    1 -> {
                        separator = viewSeparator02
                        button = buttonAction02
                    }
                    2 -> {
                        separator = viewSeparator03
                        button = buttonAction03
                    }
                    3 -> {
                        separator = viewSeparator04
                        button = buttonAction04
                    }
                    4 -> {
                        separator = viewSeparator05
                        button = buttonAction05
                    }
                }

                if (separator != null) {
                    separator.visibility = View.VISIBLE
                }

                if (button != null) {
                    button.visibility = View.VISIBLE
                    button.text = title
                    // Destructiveモードのレイアウト
                    if (getActionDestructive(i)) {
                        button.setTextColor(ContextCompat.getColor(context!!, R.color.colorIosPink))
                    }
                    button.setOnClickListener {
                        listener?.onSimpleDialogActionClick(requestCode, i)
                        dismiss()
                    }
                }

            }
        }


        // ネガティブボタン
        if (!negativeLabel.isNullOrEmpty()) {
            buttonNegative.text = negativeLabel
            buttonNegative.visibility = View.VISIBLE
            buttonNegative.setOnClickListener {
                listener?.onSimpleDialogNegativeClick(requestCode)
                dismiss()
            }
        }

        // Destructiveモードのレイアウト
        if (buttonNegative.isVisible && is_negative_destructive) {
            buttonNegative.setTextColor(ContextCompat.getColor(context!!, R.color.colorIosPink))
        }
    }

}
