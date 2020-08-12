package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataListSimple

class ListSimpleAdapter : ArrayAdapter<DataListSimple> {

    private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    constructor(context : Context, resource : Int) : super(context, resource) {
    }

    data class ViewHolder(
        val textViewTitle: TextView,
        val imageViewArrow: ImageView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder : ViewHolder? = null
        var view = convertView

        // 再利用の設定
        if (view == null) {
            view = inflater!!.inflate(R.layout.cell_simple, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.textViewTitle),
                view.findViewById(R.id.imageViewArrow)
            )
            view.tag = holder

        } else {
            holder = view.tag as ViewHolder
        }

        // 項目の情報を設定
        val item = getItem(position)
        holder.textViewTitle.text = item!!.title
        holder.imageViewArrow.visibility = if (item!!.isArrow) View.VISIBLE else View.GONE

        holder.textViewTitle.setTextColor(item!!.titleColor)
        holder.textViewTitle.typeface = item!!.titleStyle
        holder.textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, item!!.titleSize)

        return view!!
    }

}