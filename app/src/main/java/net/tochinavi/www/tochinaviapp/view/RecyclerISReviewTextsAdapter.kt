package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview
import net.tochinavi.www.tochinaviapp.value.convertDpToPx


// AdapterImageSearchReviewTextsを参考にする
class RecyclerISReviewTextsAdapter(private val context: Context, val items: ArrayList<DataSpotReview>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 1
    private val ITEM_TYPE_NORMAL = spanCount

    // 変数 //
    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val review: TextView = view!!.findViewById(R.id.textViewReview)
            val name: TextView = view!!.findViewById(R.id.textViewName)
            val date: TextView = view!!.findViewById(R.id.textViewDate)
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
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = 0
            outRect.bottom = 0

            val position = parent.getChildAdapterPosition(view)
            when (position) {
                0 -> {
                    // 最初
                    outRect.left = 16f.convertDpToPx(context).toInt()
                    outRect.right = 14f.convertDpToPx(context).toInt()
                }
                (state.itemCount - 1) -> {
                    // 最後
                    outRect.left = 8f.convertDpToPx(context).toInt()
                    outRect.right = 16f.convertDpToPx(context).toInt()
                }
                else -> {
                    // 通常
                    outRect.left = 8f.convertDpToPx(context).toInt()
                    outRect.right = 14f.convertDpToPx(context).toInt()
                }
            }


            /* いい感じのページャー（まだ使えない）
            val edgeMargin = (parent.width - view.layoutParams.width) / 2
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.left = edgeMargin
            }
            if (position == state.itemCount - 1) {
                outRect.right = edgeMargin
            }
             */
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
        return ITEM_TYPE_NORMAL
    }

    /** parent: ViewGroup?, viewType: Int **/
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ディスプレイサイズ
        val disp_size = getDisplaySize()
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // セルサイズ
        var width: Int = (disp_size.x * 0.8).toInt()

        val view: View = inflater.inflate(
            R.layout.recycler_item_is_review_texts, viewGroup, false)
        view.layoutParams.width = width
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
        holder.review.text = item.review
        holder.name.text = item.userName
        holder.date.text = item.reviewDate
    }
}