package net.tochinavi.www.tochinaviapp.view


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.tochinavi.www.tochinaviapp.R


class ListMyPageAdapter(context: Context, icons: ArrayList<Int>, titles: ArrayList<String>) : BaseAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var arrayIcon: ArrayList<Int> = ArrayList()
    private var arrayTitle: ArrayList<String> = ArrayList()

    internal class ViewHolder {
        var icon: ImageView? = null
        var title: TextView? = null
    }

    init {
        this.mContext = context
        inflater = LayoutInflater.from(context)
        arrayIcon = icons
        arrayTitle = titles
    }

    /*constructor(context: Context, icons: ArrayList<Int>, titles: ArrayList<String>) {
        inflater = LayoutInflater.from(context)
        arrayIcon = icons
        arrayTitle = titles
    }*/

    override fun getCount(): Int {
        return arrayIcon.size
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
            view = inflater!!.inflate(R.layout.cell_my_page, null)
            holder = ViewHolder()
            holder.icon = view.findViewById(R.id.imageViewIcon)
            holder.title = view.findViewById(R.id.textViewTitle)
            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val icon = arrayIcon[position]
        val title = arrayTitle[position]
        holder.icon!!.setImageDrawable(ContextCompat.getDrawable(mContext, icon))
        holder.title!!.text = title
        return view!!
    }

}