package net.tttttt.www.forum_qa_app.view

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
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataSpotList
import net.tttttt.www.forum_qa_app.value.convertDpToPx
import kotlin.random.Random

class RecyclerTopAdapter(private val context: Context, val items: ArrayList<DataSpotList>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定数 //
    val spanCount = 2
    private val PICKUP_NUMBER = 3;
    private val ITEM_TYPE_PICKUP = spanCount
    private val ITEM_TYPE_NORMAL = 1

    // 変数 //
    private var randomNumber: Int = 0
    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    companion object {
        private class ViewHolderItemPickup(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
            val viewReviewInfo: View = view!!.findViewById(R.id.viewReviewInfo)
            val textViewReviewCount: TextView = view!!.findViewById(R.id.textViewReviewCount)
            val viewDistance: View = view!!.findViewById(R.id.viewDistance)
            val textViewDistance: TextView = view!!.findViewById(R.id.textViewDistance)
            val textViewSpotName: TextView = view!!.findViewById(R.id.textViewSpotName)
            val textViewSpotInfo: TextView = view!!.findViewById(R.id.textViewSpotInfo)
        }

        private class ViewHolderItemNormal(view: View?) : RecyclerView.ViewHolder(view!!) {
            val imageView: ImageView = view!!.findViewById(R.id.imageView)
            val viewDistance: View = view!!.findViewById(R.id.viewDistance)
            val textViewDistance: TextView = view!!.findViewById(R.id.textViewDistance)
            val textViewSpotName: TextView = view!!.findViewById(R.id.textViewSpotName)
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

    /** 乱数 **/
    fun setRandomNumber() {
        randomNumber = Random.nextInt(2)
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
        if (position < PICKUP_NUMBER) {
            // ピックアップ
            return ITEM_TYPE_PICKUP
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

        if (p1 == ITEM_TYPE_PICKUP) {
            // ピックアップ
            val height = disp_size.x - (space * 2)
            val view = inflater.inflate(R.layout.recycler_item_top_pickup, p0, false)
            view.layoutParams.height = height
            return ViewHolderItemPickup(view)
        }

        // 通常
        val width = (disp_size.x - (space * 3)) / 2
        val height = (4 * width) / 3
        val view = inflater.inflate(R.layout.recycler_item_top, p0, false)
        view.layoutParams.height = height
        return ViewHolderItemNormal(view)
    }

    /** holder: RecyclerViewHolder?, position: Int **/
    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {

        val item = items[p1]

        // 写真はランダムで表示
        var imageUrl = item.image_url
        if (item.reviewImageList.size > 0) {
            if (randomNumber >= item.reviewImageList.size) {
                imageUrl = item.reviewImageList[0]
            } else {
                imageUrl = item.reviewImageList[randomNumber]
            }
        }

        // クリックイベント

        // クリックイベント //
        p0.itemView.id = p0.adapterPosition
        p0.itemView.setOnClickListener{ view ->
            if (isClickable) {
                mOnClicklistener!!.onClick(view)
            }
        }

        when (getItemViewType(p1)) {
            ITEM_TYPE_PICKUP -> {
                // val item = items[p1]
                val holder: ViewHolderItemPickup = p0 as ViewHolderItemPickup
                holder.imageView.load(imageUrl) {
                    placeholder(R.drawable.ic_image_placeholder)
                }

                holder.textViewSpotName.text = item.name

                val info_array: ArrayList<String> = ArrayList()
                if (!item.address.isEmpty()) {info_array.add(item.address)}
                if (item.review_num > 0) {info_array.add("クチコミ ${item.review_num}件")}
                if (item.favoriteNum > 0) {info_array.add("お気入り ${item.favoriteNum}件")}
                holder.textViewSpotInfo.text = info_array.joinToString("/")

                // 枚数
                holder.viewReviewInfo.visibility = View.GONE
                if (item.reviewImageNum > 0) {
                    holder.viewReviewInfo.visibility = View.VISIBLE
                    holder.textViewReviewCount.text = item.reviewImageNum.toString()
                }

                // 距離
                holder.viewDistance.visibility = View.GONE
                if (!item.distance.isEmpty()) {
                    holder.viewDistance.visibility = View.VISIBLE
                    holder.textViewDistance.text = item.distance
                }
            }
            ITEM_TYPE_NORMAL -> {
                // val item = items[p1]
                val holder: ViewHolderItemNormal = p0 as ViewHolderItemNormal
                holder.imageView.load(imageUrl) {
                    placeholder(R.drawable.ic_image_placeholder)
                }

                holder.textViewSpotName.text = item.name

                // 距離
                holder.viewDistance.visibility = View.GONE
                if (!item.distance.isEmpty()) {
                    holder.viewDistance.visibility = View.VISIBLE
                    holder.textViewDistance.text = item.distance
                }
            }
        }
    }
}