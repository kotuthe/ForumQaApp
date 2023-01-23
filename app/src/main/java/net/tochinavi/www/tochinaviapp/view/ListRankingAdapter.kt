package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataRanking

class ListRankingAdapter(context: Context, datas: ArrayList<DataRanking>) : BaseAdapter() {

    private var inflater: LayoutInflater? = null
    private var arrayData: ArrayList<DataRanking> = ArrayList()

    internal class ViewHolder {
        var rank: TextView? = null
        var userIcon: ImageView? = null
        var name: TextView? = null
        var info: TextView? = null
        var number: TextView? = null
    }

    init {
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
            view = inflater!!.inflate(R.layout.cell_ranking, null)

            holder = ViewHolder()
            holder.rank = view.findViewById(R.id.textViewRank)
            holder.userIcon = view.findViewById(R.id.imageViewUser)
            holder.name = view.findViewById(R.id.textViewName)
            holder.info = view.findViewById(R.id.textViewInfo)
            holder.number = view.findViewById(R.id.textViewNumber)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val data = arrayData[position]
        holder.rank!!.text = "%d位".format(data.rank)
        holder.name!!.text = data.name
        holder.info!!.text = data.detail
        holder.number!!.text = data.result

        // 画像
        holder.userIcon!!.tag = data.id // キャッシュ制御の為※これほんとに必要か検討する
        holder.userIcon!!.load(data.iconUrl) {
            placeholder(R.drawable.ic_image_placeholder)
        }

        return view!!
    }

}