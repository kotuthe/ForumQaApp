package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.value.convertDpToPx

class RecyclerReviewDetailImageAdapter(private val context: Context, val items: ArrayList<String>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val spanCount = 1

    // 変数 //
    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
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
        return 1
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
        val height = (3 * width) / 4

        // 通常
        val view = inflater.inflate(R.layout.recycler_item_review_detail_image, p0, false)
        view.layoutParams.height = height
        return ViewHolderItem(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val url = items[p1]
        val holder: ViewHolderItem = p0 as ViewHolderItem

        // クリックイベント //
        p0.itemView.id = p0.adapterPosition
        p0.itemView.setOnClickListener{ view ->
            if (isClickable) {
                mOnClicklistener!!.onClick(view)
            }
        }

        holder.imageView.load(url) {
            placeholder(R.drawable.ic_image_placeholder)
            // transformations(RoundedCornersTransformation(25f))
        }
    }
}