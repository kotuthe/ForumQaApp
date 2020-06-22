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
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoBasic
import net.tochinavi.www.tochinaviapp.value.Constants


class ListSpotInfoBasicOldAdapter(context: Context, datas: ArrayList<DataSpotInfoBasic>) : BaseAdapter() {

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
            view = inflater!!.inflate(R.layout.cell_spot_info_basic_old, null)
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
        var textAlign = View.TEXT_ALIGNMENT_TEXT_START
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
                icon = R.drawable.img_spot_info_coupon_red
                color = ContextCompat.getColor(mContext, R.color.colorCoupon)
            }
            Constants.SPOT_BASIC_INFO_TYPE.more_detail -> {
                textAlign = View.TEXT_ALIGNMENT_TEXT_END
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            else -> {
            }
        }

        holder.title!!.apply {
            textAlignment = textAlign
            text = item.title
            setTextColor(color)
        }

        holder.icon!!.apply {
            if (icon > 0) {
                visibility = View.VISIBLE
                setImageDrawable(ContextCompat.getDrawable(mContext, icon))
            } else {
                visibility = View.INVISIBLE
            }
        }


        return view!!
    }

}