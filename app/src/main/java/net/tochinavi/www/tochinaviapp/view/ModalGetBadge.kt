package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.modal_get_badge.*
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataBadge
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ModalGetBadge : DialogFragmentFullScreen() {

    companion object {
        const val TAG = "ModalGetBadge"

        @JvmStatic
        fun newInstance(
            items: ArrayList<DataBadge>
        ) = ModalGetBadge().apply {
            arguments = Bundle().apply {
                putSerializable(ModalGetBadge::items.name, items)
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

    interface OnModalGetBadgeClickListener {
        fun onModalGetBadgeCloseClick()
    }

    // 初期オプション
    var listener: OnModalGetBadgeClickListener? = null
    private var dismissFlag: Boolean = false
    private var items: ArrayList<DataBadge> by Arguments()

    // その他オプション
    private var mFragment: Fragment? = null
    private var mAdapter: RecyclerGetBadgeAdapter? = null

    /**
     * フラグメントでイベントを取得したいとき
     */
    fun setFragment(f: Fragment) {
        mFragment = f
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 背景クリックで閉じないようにする
        this.isCancelable = false

        // Fragmentの場合
        var fragment = targetFragment
        if (fragment == null) {
            fragment = mFragment
        }

        // Activityの場合
        if (fragment is OnModalGetBadgeClickListener) {
            listener = fragment
        } else if (context is OnModalGetBadgeClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.modal_get_badge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 多分、背景をタッチしても消えない設定をするようにしている
        // layoutDialog.setOnTouchListener { _, _ -> true }

        buttonClose.setOnClickListener {
            listener?.onModalGetBadgeCloseClick()
            onDismiss(250)
        }
        initLayout()
    }

    private fun initLayout() {

        mAdapter = RecyclerGetBadgeAdapter(context!!, items)

        // ページコントロール
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // recyclerViewの設定
        recyclerView.apply {
            // 表示計算の最適化（推奨）
            setHasFixedSize(true)

            var manager = GridLayoutManager(context, mAdapter!!.spanCount)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            manager.spanSizeLookup = mAdapter!!.spanSizeLookup
            layoutManager = manager
            addItemDecoration(mAdapter!!.mItemDecoration)
            adapter = mAdapter

        }
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
