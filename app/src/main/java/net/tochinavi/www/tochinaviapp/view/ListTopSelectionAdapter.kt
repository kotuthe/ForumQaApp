package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataTopSelection

class ListTopSelectionAdapter : ArrayAdapter<DataTopSelection> {

    private var inflater : LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    constructor(context : Context, resource : Int) : super(context, resource) {
    }

    data class ViewHolder(
        val imageViewCheck: ImageView,
        val textViewTitle: TextView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder : ViewHolder? = null
        var view = convertView

        // 再利用の設定
        if (view == null) {
            view = inflater!!.inflate(R.layout.cell_top_selection, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.imageViewCheck),
                view.findViewById(R.id.textViewTitle)
            )
            view.tag = holder

        } else {
            holder = view.tag as ViewHolder
        }

        // 項目の情報を設定
        val item = getItem(position)
        holder.textViewTitle.text = item!!.value

        // チェック状態
        holder.imageViewCheck.visibility = if (item.checke) View.VISIBLE else View.INVISIBLE
        view!!.setBackgroundColor(if (item.checke) ContextCompat.getColor(context, R.color.colorGroupTableViewBackground) else Color.WHITE)

        return view
    }

}