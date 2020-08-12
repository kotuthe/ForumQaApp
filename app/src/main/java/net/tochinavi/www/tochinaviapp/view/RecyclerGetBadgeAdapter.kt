package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Point
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
import net.tochinavi.www.tochinaviapp.entities.DataBadge


// AdapterImageSearchReviewImagesを参考にする
class RecyclerGetBadgeAdapter(private val context: Context, val items: ArrayList<DataBadge>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 1
    private val ITEM_TYPE_NORMAL = spanCount

    companion object {
        private class ViewHolderItem(view: View?) : RecyclerView.ViewHolder(view!!) {
            val icon: ImageView = view!!.findViewById(R.id.imageView)
            val title: TextView = view!!.findViewById(R.id.textViewTitle)
            val detail: TextView = view!!.findViewById(R.id.textViewDetail)
        }
    }

    override fun getItemCount() = items.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            // return spanCount
            return getItemViewType(position)
        }
    }

    val mItemDecoration = RecyclerItemLinePager(context)

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
        val view: View = inflater.inflate(R.layout.recycler_item_get_badge, viewGroup, false)
        return ViewHolderItem(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val item = items[p1]
        p0.itemView.id = p0.adapterPosition

        val holder: ViewHolderItem = p0 as ViewHolderItem
        holder.icon.load(item.imageUrl) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        holder.title.text = item.name
        holder.detail.text = item.detail
    }
}