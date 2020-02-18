package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataListSearch

class ListSearchAdapter : ArrayAdapter<DataListSearch> {

    private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    constructor(context : Context, resource : Int) : super(context, resource) {
    }

    data class ViewHolder(
        val imageViewIcon: ImageView,
        val textViewTitle: TextView,
        val textViewSubTitle: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder : ViewHolder? = null
        var view = convertView

        // 再利用の設定
        if (view == null) {
            view = inflater!!.inflate(R.layout.cell_search, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.imageViewIcon),
                view.findViewById(R.id.textViewTitle),
                view.findViewById(R.id.textViewSubTitle)
            )
            view.tag = holder

        } else {
            holder = view.tag as ViewHolder
        }

        // 項目の情報を設定
        val item = getItem(position)
        holder.imageViewIcon.setImageDrawable(ContextCompat.getDrawable(context, item!!.icon))
        holder.textViewTitle.text = item.title
        holder.textViewSubTitle.text = item.subTitle

        return view!!
    }

}