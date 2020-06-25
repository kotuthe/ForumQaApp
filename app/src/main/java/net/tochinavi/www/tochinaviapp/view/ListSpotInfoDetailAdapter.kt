package net.tochinavi.www.tochinaviapp.view


import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfoDetail
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.value.convertDpToPx


class ListSpotInfoDetailAdapter(context: Context, datas: ArrayList<DataSpotInfoDetail>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataSpotInfoDetail> = ArrayList()

    internal class ViewHolder {
        var contentView: LinearLayout? = null
        var title: TextView? = null
        var value: TextView? = null
        var arrow: ImageView? = null
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
            view = inflater!!.inflate(R.layout.cell_spot_info_detail, null)
            holder = ViewHolder()
            holder.contentView = view.findViewById(R.id.contentView)
            holder.title = view.findViewById(R.id.textViewTitle)
            holder.value = view.findViewById(R.id.textViewValue)
            holder.arrow = view.findViewById(R.id.imageViewArrow)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val item = arrayData[position]
        var color: Int = Color.BLACK
        var valueGravity = Gravity.START
        var isTitle: Boolean = true
        var isArrow: Boolean = true
        when (item.type) {
            Constants.SPOT_BASIC_INFO_TYPE.address -> {
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
            }
            Constants.SPOT_BASIC_INFO_TYPE.more_detail -> {
                valueGravity = Gravity.END
                color = ContextCompat.getColor(mContext, R.color.colorLinkBlue)
                isTitle = false
            }
            else -> {
                isArrow = false
            }
        }

        holder.title!!.apply {
            visibility = if (isTitle) View.VISIBLE else View.GONE
            text = item.title
            setTextColor(color)
        }

        holder.value!!.apply {
            text = if (isTitle) item.value else item.title
            setTextColor(color)
            gravity = valueGravity
        }

        holder.arrow!!.visibility = if (isArrow) View.VISIBLE else View.GONE

        // ※まだ上の境界線がない、下のマージン後に境界線が入るなどある
        // 上下にマージンをいれる
        var marginTop: Int = 0
        var marginBottom: Int = 0
        if (position == 0) {
            marginTop = 20f.convertDpToPx(mContext).toInt()
        }
        if (position == (arrayData.size - 1)) {
            marginBottom = 20f.convertDpToPx(mContext).toInt()
        }
        val mlp = holder.contentView!!.layoutParams as MarginLayoutParams
        mlp.setMargins(mlp.leftMargin, marginTop, mlp.rightMargin, marginBottom)
        holder.contentView!!.layoutParams = mlp

        return view!!
    }

}