package net.tttttt.www.forum_qa_app.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataCardTopics
import net.tttttt.www.forum_qa_app.value.convertString


// クリックリスナー
// https://qiita.com/ymshun/items/f54bed772d502c5c06f0
// itemsはそれぞれの数が入る
class CardTopicsAdapter(val items: Array<DataCardTopics>): RecyclerView.Adapter<CardTopicsAdapter.ViewHolder>() {

    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_topics, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textDate.text = items[position].date.convertString("YYYY-MM-dd")
        holder.textTitle.text = items[position].title
        holder.imgCaution.visibility = if (items[position].isCaution) View.VISIBLE else View.GONE
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
        var textDate: TextView
        var textTitle: TextView
        var imgCaution: ImageView
        init {
            textDate = itemView.findViewById(R.id.textDate)
            textTitle = itemView.findViewById(R.id.textTitle)
            imgCaution = itemView.findViewById(R.id.imgCaution)
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