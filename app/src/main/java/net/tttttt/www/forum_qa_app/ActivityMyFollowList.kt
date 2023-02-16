package net.tttttt.www.forum_qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import net.tttttt.www.forum_qa_app.databinding.ActivityMyFollowListBinding
import net.tttttt.www.forum_qa_app.entities.DataCardFollow
import net.tttttt.www.forum_qa_app.view.CardMyFollowAdapter

class ActivityMyFollowList : AppCompatActivity() {

    private lateinit var binding: ActivityMyFollowListBinding

    // データ //
    private var listData: Array<DataCardFollow> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyFollowListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.title = "フォロワー＆フォロー中"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        listData = arrayOf(
            DataCardFollow("", "30歳/茨城県/男性", "チャマ", true),
            DataCardFollow("", "31歳/茨城県/女性", "ミポリン", true),
            DataCardFollow("", "32歳/茨城県/男性", "ガイチ", true),
            DataCardFollow("", "33歳/茨城県/女性", "しょこたん", true),
            DataCardFollow("", "34歳/茨城県/男性", "ケンティー", true),
            DataCardFollow("", "35歳/茨城県/女性", "ナッキー", true),
            DataCardFollow("", "36歳/茨城県/男性", "チョーさん", true),
        )

        initLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initLayout() {
        var adapter = CardMyFollowAdapter(listData)
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener {
            val item: DataCardFollow = listData[it.id]
            // Toast.makeText(context, "「${item.title}」", Toast.LENGTH_SHORT).show()

            // プロフィールへ //
            val intent = Intent(this, ActivityUserProfile::class.java)
            /*intent.putExtra("date", item.date.convertString("YYYY-MM-dd"))
            intent.putExtra("title", item.title)
            intent.putExtra("note", item.note)*/
            startActivity(intent)
        }
    }
}