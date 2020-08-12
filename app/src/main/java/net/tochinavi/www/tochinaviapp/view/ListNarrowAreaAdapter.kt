package net.tochinavi.www.tochinaviapp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.tochinavi.www.tochinaviapp.R
import net.tochinavi.www.tochinaviapp.entities.DataNarrowMulti
import net.tochinavi.www.tochinaviapp.value.convertDpToPx

class ListNarrowAreaAdapter(private val context: Context, val datas1: ArrayList<String>, val datas2: ArrayList<ArrayList<DataNarrowMulti>>):
    BaseExpandableListAdapter() {

    private var mContext: Context
    private var inflater: LayoutInflater? = null
    private var groups: ArrayList<String>
    private var childes: ArrayList<ArrayList<DataNarrowMulti>>


    internal class ViewHolderGroup {
        var contentView: View? = null
        var title: TextView? = null
    }

    internal class ViewHolder {
        var contentView: View? = null
        var groupLine: View? = null
        var iconGroup: ImageView? = null
        var title: TextView? = null
        var checkbox: CheckBox? = null
    }

    init {
        this.mContext = context
        this.groups = datas1
        this.childes = datas2
        this.inflater = LayoutInflater.from(context)
    }

    override fun getGroup(p0: Int): String {
        return groups[p0]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(p0: Int, p1: Boolean, convertView: View?, p3: ViewGroup?): View {
        var view: View? = convertView
        val holder: ViewHolderGroup
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.cell_narrow_area_group, null)

            holder = ViewHolderGroup()
            holder.contentView = view.findViewById(R.id.contentView)
            holder.title = view.findViewById(R.id.textViewTitle)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolderGroup
        }

        holder.title!!.text = getGroup(p0)

        return view!!
    }

    override fun getChildrenCount(p0: Int): Int {
        return childes[p0].size
    }

    override fun getChild(p0: Int, p1: Int): DataNarrowMulti {
        return childes[p0][p1]
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getChildView(p0: Int, p1: Int, p2: Boolean, convertView: View?, parent: ViewGroup?): View {

        var view: View? = convertView
        val holder: ViewHolder
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.cell_narrow_category, null)

            holder = ViewHolder()
            holder.contentView = view.findViewById(R.id.contentView)
            holder.groupLine = view.findViewById(R.id.layoutGroupLine)
            holder.iconGroup = view.findViewById(R.id.imageViewGroup)
            holder.title = view.findViewById(R.id.textViewTitle)
            holder.checkbox = view.findViewById(R.id.checkbox)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        holder.groupLine!!.visibility = View.GONE
        holder.iconGroup!!.visibility = View.GONE
        holder.title!!.text = getChild(p0, p1).name

        val fp0 = p0; val fp1 = p1
        holder.checkbox!!.apply {
            setOnCheckedChangeListener{
                    _, isChecked ->
                childes[fp0][fp1].checked = isChecked
            }
            isChecked = getChild(p0, p1).checked
        }

        return view!!
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return p1.toLong()
    }

    override fun getGroupCount(): Int {
        return groups.count()
    }

}