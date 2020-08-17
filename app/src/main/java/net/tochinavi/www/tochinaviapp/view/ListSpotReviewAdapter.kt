package net.tochinavi.www.tochinaviapp.view


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.api.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataSpotReview

// スポットのクチコミ
class ListSpotReviewAdapter(context: Context, datas: ArrayList<DataSpotReview>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataSpotReview> = ArrayList()

    internal class ViewHolder {
        var imageUserIcon: ImageView? = null
        var userName: TextView? = null
        var userDetail: TextView? = null
        var review: TextView? = null
        var date: TextView? = null
        var layoutImage: View? = null
        var imageSpot: ImageView? = null
        var otherNumber: TextView? = null
    }

    init {
        this.mContext = context
        inflater = LayoutInflater.from(context)
        arrayData = datas
    }

    override fun getCount(): Int {
        return arrayData.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.cell_spot_review, null)

            holder = ViewHolder()
            holder.imageUserIcon = view.findViewById(R.id.imageViewUserIcon)
            holder.userName = view.findViewById(R.id.textViewUserName)
            holder.userDetail = view.findViewById(R.id.textViewUserDetail)
            holder.review = view.findViewById(R.id.textViewReview)
            holder.date = view.findViewById(R.id.textViewDate)
            holder.layoutImage = view.findViewById(R.id.layoutImage)
            holder.imageSpot = view.findViewById(R.id.imageViewSpot)
            holder.otherNumber = view.findViewById(R.id.textViewOtherNumber)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val data = arrayData[position]
        // ユーザー情報
        holder.imageUserIcon!!.load(data.userImage) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        holder.userName!!.text = data.userName
        holder.userDetail!!.text = data.userInfo

        // クチコミ
        holder.review!!.apply {
            if (data.review.isEmpty()) {
                visibility = View.INVISIBLE
                text = "\n\n"
            } else {
                visibility = View.VISIBLE
                text = data.review
            }
        }
        holder.date!!.text = data.reviewDate

        // 画像
        if (data.reviewImageUrls.size > 0) {
            holder.layoutImage!!.visibility = View.VISIBLE
            holder.imageSpot!!.tag = data.id // キャッシュ制御の為

            // holder.imageSpot!!.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorImageViewBg))
            holder.imageSpot!!.load(data.reviewImageUrls[0]) {
                placeholder(R.drawable.ic_image_placeholder)
                // transformations(RoundedCornersTransformation(25f))
                // holder.imageSpot!!.setBackgroundColor(Color.TRANSPARENT)
            }
        } else {
            holder.layoutImage!!.visibility = View.GONE
        }

        holder.otherNumber!!.visibility =
            if (data.reviewImageUrls.size >= 2) View.VISIBLE else View.GONE
        holder.otherNumber!!.text = "他%d件".format(data.reviewImageUrls.size - 1)

        return view!!
    }

}