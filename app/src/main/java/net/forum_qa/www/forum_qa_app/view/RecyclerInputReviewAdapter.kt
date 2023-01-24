package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.value.convertDpToPx

class RecyclerInputReviewAdapter(private val context: Context, val items: ArrayList<Uri?>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val TAG_VIEW_IMAGE: Int = 1
    val TAG_VIEW_BUTTON_DELETE: Int = 2
    val TAG_VIEW_ADD: Int = 3

    val spanCount = 3
    private val ITEM_TYPE_NORMAL = 1
    private val ITEM_TYPE_ADD = 2

    // 変数 //
    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    companion object {
        private class ViewHolderItemNormal(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
            val buttonDelete: Button = view!!.findViewById(R.id.buttonDelete)
            val viewMain: View = view!!.findViewById(R.id.viewMain)
        }

        private class ViewHolderItemAdd(view: View?) : RecyclerView.ViewHolder(view!!) {

        }
    }

    override fun getItemCount() = items.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return 1
            // return getItemViewType(position)
        }
    }

    val mItemDecoration = object : RecyclerView.ItemDecoration() {
        private var space: Int = 8f.convertDpToPx(context).toInt()

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = 0
            outRect.left = space / 2
            outRect.right = space / 2
            outRect.bottom = space
        }
    }



    /** クリックイベントの設定  */
    fun setOnItemClickListener(listener: View.OnClickListener) {
        mOnClicklistener = listener
    }

    /** クリックイベントの有効・無効  */
    fun setIsClickable(enable: Boolean) {
        isClickable = enable
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        if (item == null) {
            return ITEM_TYPE_ADD
        }
        return ITEM_TYPE_NORMAL
    }

    /** parent: ViewGroup?, viewType: Int **/
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // 端末のサイズ
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val disp_size = Point()
        wm.defaultDisplay.getSize(disp_size)
        val space: Int = 8f.convertDpToPx(context).toInt()
        val width = (disp_size.x - (space * (spanCount + 1))) / spanCount
        val height = width + space + 40f.convertDpToPx(context).toInt()

        if (p1 == ITEM_TYPE_ADD) {
            // 追加
            val view = inflater.inflate(R.layout.recycler_item_input_review_image_add, p0, false)
            view.layoutParams.height = height
            return ViewHolderItemAdd(view)
        }

        // 通常
        val view = inflater.inflate(R.layout.recycler_item_input_review_image, p0, false)
        view.layoutParams.height = height
        return ViewHolderItemNormal(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val uri = items[p1]
        val isMain = (p1 == 0 && uri != null)
        val viewId = p0.adapterPosition

        when (getItemViewType(p1)) {
            ITEM_TYPE_NORMAL -> {
                val holder: ViewHolderItemNormal = p0 as ViewHolderItemNormal
                holder.viewMain.visibility = if (isMain) View.VISIBLE else View.INVISIBLE

                holder.imageView.load(uri) {
                    placeholder(R.drawable.ic_image_placeholder)
                }

                holder.imageView.id = viewId
                holder.imageView.tag = TAG_VIEW_IMAGE
                holder.imageView.setOnClickListener{ view ->
                    if (isClickable) {
                        mOnClicklistener!!.onClick(view)
                    }
                }

                holder.buttonDelete.id = viewId
                holder.buttonDelete.tag = TAG_VIEW_BUTTON_DELETE
                holder.buttonDelete.setOnClickListener{ view ->
                    if (isClickable) {
                        mOnClicklistener!!.onClick(view)
                    }
                }
            }
            ITEM_TYPE_ADD -> {
                val holder: ViewHolderItemAdd = p0 as ViewHolderItemAdd
                holder.itemView.id = viewId
                holder.itemView.tag = TAG_VIEW_ADD
                holder.itemView.setOnClickListener{ view ->
                    if (isClickable) {
                        mOnClicklistener!!.onClick(view)
                    }
                }
            }
        }
    }
}