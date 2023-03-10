package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import net.tttttt.www.forum_qa_app.databinding.ActivityInputReviewTagBinding
import net.tttttt.www.forum_qa_app.entities.DataReviewTag
import net.tttttt.www.forum_qa_app.value.MyString
import net.tttttt.www.forum_qa_app.value.convertDpToPx
import org.json.JSONObject
import java.lang.Math.ceil

class ActivityInputReviewTag : AppCompatActivity() {

    companion object {
        val TAG = "ActivityInputReviewTag"
        val TAG_SHORT = "InputReviewTag"
    }

    private lateinit var binding: ActivityInputReviewTagBinding

    // データ
    private var spotId: Int = 0
    private var dataTags: ArrayList<DataReviewTag> = ArrayList()
    private var selectTags: ArrayList<DataReviewTag> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_input_review_tag)
        binding = ActivityInputReviewTagBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.title = "タグを追加"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        spotId = intent.getIntExtra("id", 0)
        selectTags = intent.getSerializableExtra("selectTags") as ArrayList<DataReviewTag>

        getData()

        binding.buttonClear.setOnClickListener {
            // データの更新
            for (i in 0..dataTags.size - 1) {
                dataTags[i].enable = false
            }
            // Viewの更新
            for (i in 0 until binding.tableLayout.childCount) {
                val tr = binding.tableLayout.getChildAt(i) as TableRow
                for (j in 0 until tr.childCount) {
                    if (tr.getChildAt(j) is TextView) {
                        (tr.getChildAt(j) as TextView).isSelected = false
                    }
                }
            }
        }

        binding.buttonAdd.setOnClickListener {
            var tags: ArrayList<DataReviewTag> = ArrayList()
            for (i in 0..dataTags.size - 1) {
                if (dataTags[i].enable) {
                    tags.add(dataTags[i])
                }
            }
            val intent = Intent()
            intent.putExtra("selectTags", tags)
            setResult(RESULT_OK, intent);
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateViewTag() {
        // タグの表示
        binding.tableLayout.also {
            val rowSize: Int = ceil(dataTags.size / 2.0).toInt()
            for (i in 0..rowSize - 1) {
                val tr = TableRow(this).also {
                    val lp = TableRow.LayoutParams(
                        0, 40f.convertDpToPx(this).toInt(), 1f)

                    lp.setMargins(
                        4f.convertDpToPx(this).toInt())

                    // 1列目
                    val text1 = createViewTag(i * 2)
                    text1.layoutParams = lp
                    it.addView(text1)

                    // 2列目
                    if ((i * 2) + 1 < dataTags.size) {
                        val text2 = createViewTag((i * 2) + 1)
                        text2.layoutParams = lp
                        it.addView(text2)
                    } else {
                        // 空白のViewを入れる
                        val view = View(this)
                        view.layoutParams = lp
                        it.addView(view)
                    }

                }
                it.addView(tr)
            }
        }
    }

    /**
     * TAGのView作成
     */
    private fun createViewTag(index: Int): TextView {
        val item = dataTags[index]
        val textView = TextView(this)
        textView.also {
            it.text = item.name
            it.setBackgroundResource(R.drawable.txt_review_tag)
            it.setTextColor(ContextCompat.getColorStateList(this, R.color.txt_review_tag_color))
            it.textAlignment = View.TEXT_ALIGNMENT_CENTER
            it.gravity = Gravity.CENTER_VERTICAL

            // クリック
            it.isClickable = true
            it.isSelected = item.enable
            it.setOnClickListener {
                val enable = !item.enable
                dataTags[index].enable = enable
                it.isSelected = enable
            }
        }

        return textView
    }

    /**
     * タグの取得
     */
    private fun getData() {
        val url = MyString().my_http_url_app() + "/review/get_review_tags.php"
        val params = listOf("flag" to true, "spot_id" to spotId)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {

                    val tag_array = datas.getJSONArray("review_tag_array")
                    for (i in 0..tag_array.length() - 1) {
                        val obj = tag_array.getJSONObject(i)
                        val id = obj.getInt("id")
                        // 選択したタグ
                        var enable = false
                        for (j in 0..selectTags.size - 1) {
                            if (selectTags[j].id == id) {
                                enable = true
                                break
                            }
                        }
                        dataTags.add(DataReviewTag(
                            obj.getInt("id"),
                            obj.getString("name"),
                            enable
                        ))
                    }
                    updateViewTag()
                }

            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
            })
        }
    }
}
