package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.value.convertDpToPx

class RecyclerInitDescriptionAdapter(private val context: Context, val items: MutableList<Int>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 1
    private val ITEM_TYPE_NORMAL = 1
    private val ITEM_TYPE_FOOTER = 2

    // 変数 //
    private val enableFooter = false

    companion object {
        private class ViewHolderFooter(view: View?) : RecyclerView.ViewHolder(view!!) {
        }

        private class ViewHolderItemNormal(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
        }

    }

    override fun getItemCount() = items.size

    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return spanCount
        }
    }

    val mItemDecoration = object : RecyclerView.ItemDecoration() {
        private var space: Int = 0f.convertDpToPx(context).toInt()

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


    override fun getItemViewType(position: Int): Int {
        // フッター
        if (position == items.size - 1 && enableFooter) {
            return ITEM_TYPE_FOOTER
        }
        // アイテム
        return ITEM_TYPE_NORMAL
    }

    /** parent: ViewGroup?, viewType: Int **/
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        when (p1) {
            ITEM_TYPE_NORMAL -> {
                // アイテム
                val view = inflater!!.inflate(R.layout.recycler_item_init_description, p0, false)
                return ViewHolderItemNormal(view)
            }
            ITEM_TYPE_FOOTER -> {
                // フッター
                val view = inflater!!.inflate(R.layout.recycler_footer_normal, p0, false)
                return ViewHolderFooter(view)
            }
        }
        return ViewHolderFooter(View(context))
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        when (getItemViewType(p1)) {
            ITEM_TYPE_NORMAL -> {
                // square
                val image: Int = items.get(p1) as Int
                val holder: ViewHolderItemNormal = p0 as ViewHolderItemNormal
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, image))
            }
            ITEM_TYPE_FOOTER -> {
                // フッター
            }
        }
    }
}