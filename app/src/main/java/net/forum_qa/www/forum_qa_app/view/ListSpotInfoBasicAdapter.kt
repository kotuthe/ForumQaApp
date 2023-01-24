package net.tttttt.www.forum_qa_app.view


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataSpotInfoBasic
import net.tttttt.www.forum_qa_app.value.Constants


class ListSpotInfoBasicAdapter(context: Context, datas: ArrayList<DataSpotInfoBasic>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataSpotInfoBasic> = ArrayList()

    internal class ViewHolder {
        var icon: ImageView? = null
        var title: TextView? = null
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
            view = inflater!!.inflate(R.layout.cell_spot_info_basic, null)
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.imageViewIcon)
            holder.title = view.findViewById(R.id.textViewTitle)
            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }


        val item = arrayData[position]
        var icon: Int = 0
        var color: Int = Color.BLACK
        when (item.type) {
            Constants.SPOT_BASIC_INFO_TYPE.address -> {
                icon = R.drawable.img_spot_info_map
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                icon = R.drawable.img_spot_info_phone
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            Constants.SPOT_BASIC_INFO_TYPE.hour -> {
                icon = R.drawable.img_spot_info_hour
            }
            Constants.SPOT_BASIC_INFO_TYPE.holiday -> {
                icon = R.drawable.img_spot_info_holiday
            }
            Constants.SPOT_BASIC_INFO_TYPE.coupon -> {
                icon = R.drawable.img_spot_info_coupon_blue
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            else -> {
            }
        }

        holder.title!!.apply {
            text = item.title
            setTextColor(color)
        }

        holder.icon!!.apply {
            if (icon > 0) {
                visibility = View.VISIBLE
                setImageDrawable(ContextCompat.getDrawable(mContext, icon))
            } else {
                visibility = View.GONE
            }
        }
        return view!!
    }

}