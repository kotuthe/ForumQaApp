package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataISReviewDetailImage
import net.tochinavi.www.tochinaviapp.value.convertDpToPx


class RecyclerISReviewDetailImagesAdapter(private val context: Context, val items: ArrayList<DataISReviewDetailImage>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 2
    private val ITEM_TYPE_FULL = spanCount  // 縦フル
    private val ITEM_TYPE_HALF = 1 // 縦半分

    // 変数 //
    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
            val textViewNumber: TextView = view!!.findViewById(R.id.textViewNumber)
        }
    }

    override fun getItemCount() = items.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            // return spanCount
            return getItemViewType(position)
        }
    }

    val mItemDecoration = object : RecyclerView.ItemDecoration() {
        private var space: Int = 4f.convertDpToPx(context).toInt()

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {

            outRect.top = space / 2
            outRect.bottom = space / 2

            // ※まだこの仕組みは未完成
            var left = space / 2
            var right = space / 2
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                // 最初
                left = 8f.convertDpToPx(context).toInt()
                right = space / 2
            } else if (position == state.itemCount - 2) {
                if (getItemViewType(position) == ITEM_TYPE_HALF &&
                        getItemViewType(state.itemCount - 1) == ITEM_TYPE_HALF) {
                    left = space / 2
                    right = 8f.convertDpToPx(context).toInt()
                }
            } else if (position == state.itemCount - 1) {
                left = space / 2
                right = 8f.convertDpToPx(context).toInt()
            }

            outRect.left = left
            outRect.right = right
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

    /** ディスプレイサイズ **/
    private fun getDisplaySize(): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = Point()
        wm.defaultDisplay.getSize(p)
        return p
    }

    override fun getItemViewType(position: Int): Int {
        // 3以上で有効
        return if (position % 5 == 0) {
            // 1列(index 0,5,10 ...)
            ITEM_TYPE_FULL
        } else {
            // 2列(1~4, 6~9 ...)
            ITEM_TYPE_HALF
        }
    }

    /** parent: ViewGroup?, viewType: Int **/
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ディスプレイサイズ
        val disp_size = getDisplaySize()
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // セルサイズ
        var width: Int = 0
        var height: Int = 0

        // spaceと同じ値にする
        val margin = 4f.convertDpToPx(context).toInt()
        val side_margin = 8f.convertDpToPx(context).toInt()
        val cell_height = 250f.convertDpToPx(context).toInt() - margin

        when (itemCount) {
            1 -> {
                // 1つのときは横いっぱい //
                width = disp_size.x - (side_margin * 2)
                height = cell_height
            }
            2 -> {
                // 半分 //
                width = (disp_size.x - (side_margin * 3)) / 2
                height = cell_height
            }
            else -> {
                // 3以上
                val one_width = (disp_size.x - (side_margin * 3)) / 3
                if (viewType == ITEM_TYPE_FULL) {
                    // 1列(index 0,5,10 ...)
                    width = one_width * 2
                    height = cell_height
                } else {
                    // 2列(1~4, 6~9 ...)
                    width = one_width
                    height = (cell_height - margin) / 2
                }
            }
        }

        val view: View = inflater.inflate(
            R.layout.recycler_item_is_review_images, viewGroup, false)
        view.layoutParams.width = width
        view.layoutParams.height = height
        return ViewHolderItem(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val item = items[p1]

        // クリックイベント //
        p0.itemView.id = p0.adapterPosition
        p0.itemView.setOnClickListener{ view ->
            if (isClickable) {
                mOnClicklistener!!.onClick(view)
            }
        }

        val holder: ViewHolderItem = p0 as ViewHolderItem
        holder.imageView.load(item.url) {
            placeholder(R.drawable.ic_image_placeholder)
        }

        // その他の枚数
        if (item.number > 0) {
            holder.textViewNumber.visibility = View.VISIBLE
            holder.textViewNumber.text = "他%d枚".format(item.number)
        } else {
            holder.textViewNumber.visibility = View.GONE;
        }
    }
}