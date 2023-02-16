package net.tttttt.www.forum_qa_app.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataCardAnswers
import net.tttttt.www.forum_qa_app.value.convertString

class CardMyAnswerAdapter(val items: Array<DataCardAnswers>): RecyclerView.Adapter<CardMyAnswerAdapter.ViewHolder>() {

    private var mOnClicklistener: View.OnClickListener? = null
    private var isClickable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_my_answer, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textDate.text = items[position].date.convertString("YYYY-MM-dd")
        holder.textTitle.text = items[position].title
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
        init {
            textDate = itemView.findViewById(R.id.textDate)
            textTitle = itemView.findViewById(R.id.textTitle)
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