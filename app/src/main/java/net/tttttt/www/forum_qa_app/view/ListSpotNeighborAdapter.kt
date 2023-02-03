package net.tttttt.www.forum_qa_app.view


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataSpotList

// 周辺検索との組み合わせ
class ListSpotNeighborAdapter(context: Context, datas: ArrayList<DataSpotList>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataSpotList> = ArrayList()

    internal class ViewHolder {
        var imageSpot: ImageView? = null
        var name: TextView? = null
        var address: TextView? = null
        var category: TextView? = null
        var layoutDistance: View? = null
        var distance: TextView? = null
        var layoutReviewNum: View? = null
        var reviewNum: TextView? = null
        var layoutCoupon: View? = null
        var layoutCheckinEnable: View? = null
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
            view = inflater!!.inflate(R.layout.cell_spot_neighbor, null)

            holder = ViewHolder()
            holder.imageSpot = view.findViewById(R.id.imageViewSpot)
            holder.name = view.findViewById(R.id.textViewName)
            holder.address = view.findViewById(R.id.textViewAddress)
            holder.category = view.findViewById(R.id.textViewCategory)
            holder.layoutDistance = view.findViewById(R.id.layoutDistance)
            holder.distance = view.findViewById(R.id.textViewDistance)
            holder.layoutReviewNum = view.findViewById(R.id.layoutReviewNum)
            holder.reviewNum = view.findViewById(R.id.textReviewNum)
            holder.layoutCoupon = view.findViewById(R.id.layoutCoupon)
            holder.layoutCheckinEnable = view.findViewById(R.id.layoutCheckinEnable)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val data = arrayData[position]
        holder.name!!.text = data.name
        holder.address!!.text = data.address
        holder.category!!.text = data.category
        holder.distance!!.text = data.distance

        // 画像
        holder.imageSpot!!.tag = data.id // キャッシュ制御の為※これほんとに必要か検討する
        holder.imageSpot!!.load(data.image_url) {
            placeholder(R.drawable.ic_image_placeholder)
            holder.imageSpot!!.setBackgroundColor(Color.TRANSPARENT)
        }

        // チェックイン数、クチコミ数
        if (data.review_num > 0) {
            holder.layoutReviewNum!!.visibility = View.VISIBLE
            holder.reviewNum!!.text = "%d件".format(data.review_num)
        } else {
            holder.layoutReviewNum!!.visibility = View.INVISIBLE
        }

        // クーポン
        holder.layoutCoupon!!.visibility =
            if (data.coupon_enable) View.VISIBLE else View.INVISIBLE

        // チェックイン可能
        holder.layoutCheckinEnable!!.visibility =
            if (data.checkin_enable) View.VISIBLE else View.INVISIBLE

        return view!!
    }

}