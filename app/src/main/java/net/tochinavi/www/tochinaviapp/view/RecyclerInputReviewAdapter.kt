package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.value.convertDpToPx

class RecyclerInputReviewAdapter(private val context: Context, val items: ArrayList<String?>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
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
        val imageUrl = items[position]
        if (imageUrl == null) {
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
        val height = width

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

        val imageUrl = items[p1]
        val isMain = if (p1 == 0 && imageUrl != null) true else false

        // クリックイベント //
        p0.itemView.id = p0.adapterPosition
        p0.itemView.setOnClickListener{ view ->
            if (isClickable) {
                mOnClicklistener!!.onClick(view)
            }
        }

        when (getItemViewType(p1)) {
            ITEM_TYPE_NORMAL -> {
                val holder: ViewHolderItemNormal = p0 as ViewHolderItemNormal
                holder.viewMain.visibility = if (isMain) View.VISIBLE else View.INVISIBLE
                holder.imageView.load(imageUrl) {
                    placeholder(R.drawable.ic_image_placeholder)
                }
                // buttonDeleteが動くようにする
                holder.buttonDelete.setOnClickListener {
                    Log.i(">> image delete", "$p1")
                }
            }
            ITEM_TYPE_ADD -> {
                // val holder: ViewHolderItemAdd = p0 as ViewHolderItemAdd
            }
        }
    }
}