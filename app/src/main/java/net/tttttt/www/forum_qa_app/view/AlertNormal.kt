package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.databinding.AlertNormalBinding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AlertNormal : DialogFragmentFullScreen() {

    companion object {
        const val TAG = "AlertNormal"
        /**
         * requestCodeをクリック時に返します。
         * positiveLabelがnullの場合は「閉じる」ボタンを表示し、他のものがnullの場合は非表示にします。
         */
        @JvmStatic
        fun newInstance(
            requestCode: Int,
            title: String? = null, msg: String? = null,
            positiveLabel: String? = null, negativeLabel: String? = null
        ) = AlertNormal().apply {
            arguments = Bundle().apply {
                putInt(AlertNormal::requestCode.name, requestCode)
                putString(AlertNormal::title.name, title)
                putString(AlertNormal::msg.name, msg)
                putString(AlertNormal::positiveLabel.name, positiveLabel)
                putString(AlertNormal::negativeLabel.name, negativeLabel)
            }
        }
    }

    private lateinit var binding: AlertNormalBinding

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
        fun onSimpleDialogPositiveClick(requestCode: Int)
        fun onSimpleDialogNegativeClick(requestCode: Int)
    }

    // 初期オプション
    var listener: OnSimpleDialogClickListener? = null
    private var requestCode: Int by Arguments()
    private var title: String? by Arguments()
    private var msg: String? by Arguments()
    private var positiveLabel: String? by Arguments()
    private var negativeLabel: String? by Arguments()

    // その他オプション
    private var mFragment: Fragment? = null
    private var is_positive_destructive: Boolean = false
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
    fun setPositiveDestructive() {
        is_positive_destructive = true
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
        // return inflater.inflate(R.layout.alert_normal, container, false)
        binding = AlertNormalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutDialog.setOnTouchListener { _, _ -> true }

        // タイトル
        if (title == null || title!!.isEmpty()) {
            binding.textViewTitle.visibility = View.GONE
        } else {
            binding.textViewTitle.visibility = View.VISIBLE
            binding.textViewTitle.text = title
        }

        // メッセージ
        if (msg == null || msg!!.isEmpty()) {
            binding.textViewMsg.visibility = View.GONE
        } else {
            binding.textViewMsg.visibility = View.VISIBLE
            binding.textViewMsg.text = msg
        }

        // ポジティブボタン
        if (!positiveLabel.isNullOrEmpty()) {
            binding.buttonPositive.text = positiveLabel
        }

        // ネガティブボタン
        if (!negativeLabel.isNullOrEmpty()) {
            binding.viewButtonSeparator.visibility = View.VISIBLE
            binding.buttonNegative.text = negativeLabel
            binding.buttonNegative.visibility = View.VISIBLE
            binding.buttonNegative.setOnClickListener {
                listener?.onSimpleDialogNegativeClick(requestCode)
                dismiss()
            }
        }

        // Destructiveモードのレイアウト
        if (binding.buttonPositive.isVisible && is_positive_destructive) {
            binding.buttonPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorIosPink))
        }

        if (binding.buttonNegative.isVisible && is_negative_destructive) {
            binding.buttonNegative.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorIosPink))
        }

        binding.buttonPositive.setOnClickListener {
            listener?.onSimpleDialogPositiveClick(requestCode)
            dismiss()
        }
    }

}
