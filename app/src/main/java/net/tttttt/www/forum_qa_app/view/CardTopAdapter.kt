package net.tttttt.www.forum_qa_app.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataSpotList

// itemsはそれぞれの数が入る
class CardTopAdapter(val items: Array<Int>): RecyclerView.Adapter<CardTopAdapter.ViewHolder>() {

    val titles = arrayOf(
        "あなたが質問に対する回答を見る",
        "あなたが興味がありそうな質問新着を見る",
        "フォロアーの質問新着を見る",
        "フォロイーの質問新着を見る")

    val subTitles = arrayOf(
        "募集中の質問がありません",
        "興味のあるカテゴリが設定されてません",
        "フォロアーがいません",
        "フォロイーがいません")

    val btnTexts = arrayOf(
        "質問してみる",
        "興味のあるカテゴリが設定する",
        "フォロアーをつくるには？",
        "フォロイーがつくるには？")

    val images = arrayOf(
        R.drawable.ic_card_groups,
        R.drawable.ic_card_search,
        R.drawable.ic_card_add,
        R.drawable.ic_card_add_alt)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_top, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textTitle.text = titles[position]
        holder.textSubTitle.text = subTitles[position]
        holder.button.text = btnTexts[position]
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView
        var textSubTitle: TextView
        var button: Button
        var imageView: ImageView
        init {
            textTitle = itemView.findViewById(R.id.textTitle)
            textSubTitle = itemView.findViewById(R.id.textSubTitle)
            button = itemView.findViewById(R.id.button)
            imageView = itemView.findViewById(R.id.imageView)
        }
    }
}