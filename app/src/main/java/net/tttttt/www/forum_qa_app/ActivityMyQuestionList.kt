package net.tttttt.www.forum_qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import net.tttttt.www.forum_qa_app.databinding.ActivityMyQuestionListBinding
import net.tttttt.www.forum_qa_app.entities.DataCardQuestions
import net.tttttt.www.forum_qa_app.value.convertString
import net.tttttt.www.forum_qa_app.view.CardMyQuestionAdapter
import java.text.SimpleDateFormat
import java.util.*

class ActivityMyQuestionList : AppCompatActivity() {

    private lateinit var binding: ActivityMyQuestionListBinding

    // データ //
    private var listData: Array<DataCardQuestions> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyQuestionListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.title = "My質問一覧"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        listData = arrayOf(
            DataCardQuestions(convertStrToDate("2022-01-16"),"パソコンのデータ消去はしてもらえますか？", "", arrayOf(1,2), 2),
            DataCardQuestions(convertStrToDate("2022-01-15"),"季節やブランドによって買い取りの可否に影響はありますか？", "", arrayOf(1,2), 4),
            DataCardQuestions(convertStrToDate("2022-01-14"),"エアコンなどの家電製品は設置したままの状態で引き取ってもらえますか？", "", arrayOf(1,2), 5),
            DataCardQuestions(convertStrToDate("2022-01-13"),"買取りの際に住所や氏名を書かなくてはいけないのはなぜですか?", "", arrayOf(1,2), 2),
            DataCardQuestions(convertStrToDate("2022-01-12"),"引っ越し時に発生するダンボールや不用品などの処分をお願いしたいのですが？", "", arrayOf(1,2), 2),
        )
        // CardMyQuestionAdapter
        initLayout()
    }

    private fun convertStrToDate(str: String): Date {
        val df = SimpleDateFormat("yyyy-MM-dd")
        val date = df.parse(str)
        return date!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initLayout() {
        var adapter = CardMyQuestionAdapter(listData)
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener {
            val item: DataCardQuestions = listData[it.id]
            // Toast.makeText(context, "「${item.title}」", Toast.LENGTH_SHORT).show()

            // 詳細へ
            /*val intent = Intent(this, ActivityTopicsDetail::class.java)
            intent.putExtra("date", item.date.convertString("YYYY-MM-dd"))
            intent.putExtra("title", item.title)
            intent.putExtra("note", item.note)
            startActivity(intent)*/
        }
    }
}