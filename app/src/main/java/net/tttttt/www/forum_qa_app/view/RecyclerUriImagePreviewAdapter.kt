package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import net.tttttt.www.forum_qa_app.R


// AdapterImageSearchReviewImagesを参考にする
class RecyclerUriImagePreviewAdapter(private val context: Context, val items: ArrayList<Uri>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 1
    private val ITEM_TYPE_NORMAL = spanCount

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val image: ImageView = view!!.findViewById(R.id.imageView)
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
        val view: View = inflater.inflate(R.layout.recycler_item_uri_image_preview, viewGroup, false)
        return ViewHolderItem(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val item = items[p1]
        p0.itemView.id = p0.adapterPosition

        val holder: ViewHolderItem = p0 as ViewHolderItem
        holder.image.load(item) {
            placeholder(R.drawable.ic_image_placeholder)
        }
    }
}