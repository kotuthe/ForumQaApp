package net.tochinavi.www.tochinaviapp.view


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataMySpotList

// Myページ：チェックイン履歴、お気に入りで使用
class ListMySpotAdapter(context: Context, datas: ArrayList<DataMySpotList>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataMySpotList> = ArrayList()

    internal class ViewHolder {
        var imageSpot: ImageView? = null
        var name: TextView? = null
        var address: TextView? = null
        var category: TextView? = null
        var date: TextView? = null
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
            view = inflater!!.inflate(R.layout.cell_my_spot_info, null)

            holder = ViewHolder()
            holder.imageSpot = view.findViewById(R.id.imageViewSpot)
            holder.name = view.findViewById(R.id.textViewName)
            holder.address = view.findViewById(R.id.textViewAddress)
            holder.category = view.findViewById(R.id.textViewCategory)
            holder.date = view.findViewById(R.id.textViewDate)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val data = arrayData[position]
        holder.name!!.text = data.name
        holder.address!!.text = data.address
        holder.category!!.text = data.category

        // 画像
        holder.imageSpot!!.tag = data.id // キャッシュ制御の為
        holder.imageSpot!!.load(data.image_url) {
            placeholder(R.drawable.ic_image_placeholder)
            holder.imageSpot!!.setBackgroundColor(Color.TRANSPARENT)
        }

        // 日付
        holder.date!!.apply {
            if (data.date.isEmpty()) {
                visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                text = data.date
            }
        }

        return view!!
    }

}