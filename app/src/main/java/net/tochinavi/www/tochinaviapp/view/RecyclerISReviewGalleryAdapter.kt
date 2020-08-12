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
import coil.api.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview


class RecyclerISReviewGalleryAdapter(private val context: Context, val items: ArrayList<DataSpotReview>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 1
    private val ITEM_TYPE_NORMAL = spanCount

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val image: ImageView = view!!.findViewById(R.id.imageView)
            val number: TextView = view!!.findViewById(R.id.textViewNumber)
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
        private var space: Int = 0

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = space
            outRect.left = space
            outRect.right = space
            outRect.bottom = space
        }
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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.recycler_item_is_review_gallery, viewGroup, false)
        return ViewHolderItem(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val item = items[p1]
        p0.itemView.id = p0.adapterPosition

        val holder: ViewHolderItem = p0 as ViewHolderItem
        holder.image.load(item.reviewImageUrls[0]) {
            placeholder(R.drawable.ic_image_placeholder)
        }

        // 枚数
        val all = item.reviewImageUrls.size
        if (all >= 2) {
            holder.number.visibility = View.VISIBLE
            holder.number.text = "他%d枚".format(all - 1)
        } else {
            holder.number.visibility = View.GONE
        }
    }
}