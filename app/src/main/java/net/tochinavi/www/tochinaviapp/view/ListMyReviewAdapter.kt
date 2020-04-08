package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.api.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataMyReview
import net.tochinavi.www.tochinaviapp.value.convertDpToPx

// Myクチコミや下書きなどで使う
class ListMyReviewAdapter(context: Context, datas: ArrayList<DataMyReview>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataMyReview> = ArrayList()

    internal class ViewHolder {
        var icon: ImageView? = null
        var type: TextView? = null
        var name: TextView? = null
        var info: TextView? = null
        var review: TextView? = null
        var layoutMain: View? = null
        var imageMain: ImageView? = null
        var imageMainNumber: TextView? = null
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
            view = inflater!!.inflate(R.layout.cell_my_review, null)

            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.imageViewIcon)
            holder.type = view.findViewById(R.id.textViewType)
            holder.name = view.findViewById(R.id.textViewName)
            holder.info = view.findViewById(R.id.textViewInfo)
            holder.review = view.findViewById(R.id.textViewReview)
            holder.layoutMain = view.findViewById(R.id.layoutMain)
            holder.imageMain = view.findViewById(R.id.imageViewMain)
            holder.imageMainNumber = view.findViewById(R.id.textViewMainNumber)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val data = arrayData[position]
        holder.name!!.text = data.spotName
        // クチコミ //
        holder.review!!.apply {
            setTextColor(if (data.isDraft)
                ContextCompat.getColor(mContext, R.color.colorLinkBlue) else Color.BLACK)
            if (!data.review.isEmpty()) {
                visibility = View.VISIBLE
                text = data.review
            } else {
                visibility = View.GONE
            }
        }

        // 写真 //
        if (!data.reviewImageUrls.isEmpty()) {
            holder.layoutMain!!.visibility = View.VISIBLE
            holder.imageMain!!.load(data.reviewImageUrls[0]) {
                placeholder(R.drawable.ic_image_placeholder)
            }
            holder.imageMainNumber!!.apply {
                // 写真が2件以上の時
                if (data.reviewImageUrls.size >= 2) {
                    visibility = View.VISIBLE
                    text = "他%d枚".format(data.reviewImageUrls.size - 1)
                } else {
                    visibility = View.GONE
                }
            }
        } else {
            // 写真 GONE
            holder.layoutMain!!.visibility = View.GONE
        }

        if (!data.isDraft) {
            // 投稿済み
            holder.icon!!.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_cmn_review_yellow))
            holder.type!!.visibility = View.VISIBLE
            setType(holder.type!!, data.type)
            holder.info!!.text = "投稿日: %s".format(data.reviewDate)
        } else {
            // 下書き
            holder.icon!!.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_cmn_draft))
            holder.type!!.visibility = View.GONE // アプリの下書き一覧には他のタイプは表示されてない
            holder.info!!.text = "編集日: %s".format(data.reviewDate)
        }

        return view!!
    }

    private fun setType(v: TextView, type: Int) {
        var name = ""
        var color: Int = Color.rgb(200, 200, 200)
        when (type) {
            1 -> {
                name = "スポット"
                color = Color.rgb(255, 180, 0)
            }
            2 -> {
                name = "イベント"
                color = Color.rgb(40, 136, 199)
            }
            4 -> {
                name = "特集"
                color = Color.rgb(140, 198, 63)
            }
            5 -> {
                name = "エリア"
                color = Color.rgb(241, 91, 97)
            }
        }
        v.text = name

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(color)
        shape.cornerRadius = 5f.convertDpToPx(mContext)
        v.background = shape
    }
}