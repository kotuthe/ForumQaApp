package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.databinding.ModalUriImagePreviewBinding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * input :URI(内部ストレージ)
 * ・スクロールできるようにする
 * ・写真の更新がある
 * output: close
 */
class ModalUriImagePreview : DialogFragmentFullScreen() {

    companion object {
        const val TAG = "ModalUriImagePreview"

        @JvmStatic
        fun newInstance(
            items: ArrayList<Uri>,
            selectIndex: Int,
            title: String? = null
        ) = ModalUriImagePreview().apply {
            arguments = Bundle().apply {
                putSerializable(ModalUriImagePreview::items.name, items)
                putSerializable(ModalUriImagePreview::selectIndex.name, selectIndex)
                putSerializable(ModalUriImagePreview::fixTitle.name, title)
            }
        }
    }

    private lateinit var binding: ModalUriImagePreviewBinding

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

    interface OnModalUriImagePreviewClickListener {
        fun onModalUriImagePreviewCloseClick()
    }

    // 初期オプション
    var listener: OnModalUriImagePreviewClickListener? = null
    private var dismissFlag: Boolean = false
    private var items: ArrayList<Uri> by Arguments()
    private var selectIndex: Int by Arguments()
    private var fixTitle: String? by Arguments()
    private var isOnStart = false

    // その他オプション
    private var mFragment: Fragment? = null
    private var mAdapter: RecyclerUriImagePreviewAdapter? = null

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
        if (fragment is OnModalUriImagePreviewClickListener) {
            listener = fragment
        } else if (context is OnModalUriImagePreviewClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // return inflater.inflate(R.layout.modal_uri_image_preview, container, false)
        binding = ModalUriImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 多分、背景をタッチしても消えない設定をするようにしている
        // layoutDialog.setOnTouchListener { _, _ -> true }

        binding.viewPageClose.setOnClickListener {
            listener?.onModalUriImagePreviewCloseClick()
            onDismiss(250)
        }
        initLayout()
    }

    override fun onStart() {
        super.onStart()
        isOnStart = true
    }

    /**
     * UIの初期設定
     */
    private fun initLayout() {

        mAdapter = RecyclerUriImagePreviewAdapter(requireContext(), items)

        // ページコントロール
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        // recyclerViewの設定
        binding.recyclerView.apply {
            // 表示計算の最適化（推奨）
            setHasFixedSize(true)

            val manager = GridLayoutManager(context, mAdapter!!.spanCount)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            manager.spanSizeLookup = mAdapter!!.spanSizeLookup
            layoutManager = manager
            addItemDecoration(mAdapter!!.mItemDecoration)
            adapter = mAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val manager = recyclerView.layoutManager as LinearLayoutManager?
                    val last = manager!!.findLastVisibleItemPosition()
                    updateTitle(last)
                }
            })

            scrollToPosition(selectIndex)
            updateTitle(selectIndex)
        }
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

    /** ページ更新  */
    private fun updateTitle(index: Int) {
        selectIndex = index

        var title = "プレビュー"
        if (items.count() >= 2) {
            // 2以上
            title += " ( %d / %d )".format(index + 1, items.count())
        }
        binding.textViewTitle.text = if (fixTitle != null) fixTitle!! else title
    }

    /** タイトル更新 **/
    fun setNotChangeTitle(title: String) {
        binding.textViewTitle.text = title
    }

}
