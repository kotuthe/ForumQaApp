package net.tttttt.www.forum_qa_app.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataCardFollow

class CardMyFollowAdapter(val items: Array<DataCardFollow>): RecyclerView.Adapter<CardMyFollowAdapter.ViewHolder>() {

    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_follow, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textInfo.text = items[position].info
        holder.textName.text = items[position].name
        // holder.imgCaution.visibility = if (items[position].isCaution) View.VISIBLE else View.GONE
        holder.itemView.id = holder.bindingAdapterPosition
        holder.itemView.setOnClickListener {
            if (isClickable) {
                mOnClicklistener!!.onClick(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageUser: ImageView
        var textInfo: TextView
        var textName: TextView
        var buttonFollow: Button
        init {
            imageUser = itemView.findViewById(R.id.imageUser)
            textInfo = itemView.findViewById(R.id.textInfo)
            textName = itemView.findViewById(R.id.textName)
            buttonFollow = itemView.findViewById(R.id.buttonFollow)
        }
    }

    /** クリックイベントの設定  */
    fun setOnItemClickListener(listener: View.OnClickListener) {
        mOnClicklistener = listener
    }

    /** クリックイベントの有効・無効  */
    fun setIsClickable(enable: Boolean) {
        isClickable = enable
    }

}