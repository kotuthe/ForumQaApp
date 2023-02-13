package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotSearchNarrowCategoryMultiBinding
import net.tttttt.www.forum_qa_app.entities.DataNarrowMulti
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableCategory1
import net.tttttt.www.forum_qa_app.storage.DBTableCategory2
import net.tttttt.www.forum_qa_app.storage.DBTableCategory3
import net.tttttt.www.forum_qa_app.view.ListNarrowCategoryAdapter

class ActivitySpotSearchNarrowCategoryMulti :
    AppCompatActivity() {

    companion object {
        const val TAG = "ActivitySpotSearchNarrowCategoryMulti"
        const val TAG_SHORT = "SSNarrowCaM"
    }

    private lateinit var binding: ActivitySpotSearchNarrowCategoryMultiBinding

    // データ //
    private var selectCategory: ArrayList<Pair<Int, Int>> = arrayListOf()
    private var parentId: Int = 0
    private var parentName: String = ""
    private lateinit var mAdapter: ListNarrowCategoryAdapter
    private val groups: ArrayList<DataNarrowMulti> = arrayListOf()
    private val childes: ArrayList<ArrayList<DataNarrowMulti>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_search_narrow_category_multi)
        binding = ActivitySpotSearchNarrowCategoryMultiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        selectCategory = intent.getSerializableExtra("category") as ArrayList<Pair<Int, Int>>
        parentId = selectCategory[0].second

        // データの取得
        // カテゴリー２、３を取得して
        val db = DBHelper(this)
        try {
            val ca1 = DBTableCategory1(this).getData(db, parentId)
            parentName = ca1.name
            val array_ca2 = DBTableCategory2(this).getTargetChild(db, parentId)
            for (i in 0..array_ca2.size - 1) {
                val ca2 = array_ca2[i]
                groups.add(DataNarrowMulti(
                    ca2.id,
                    ca2.name,
                    ca2.parent_id,
                    checkSelect(2, ca2.id)
                ))

                val tmp_childes: ArrayList<DataNarrowMulti> = arrayListOf()
                val array_ca3 = DBTableCategory3(this).getTargetChild(db, ca2.id)
                if (array_ca3.size > 0) {
                    for (j in 0..array_ca3.size - 1) {
                        val ca3 = array_ca3[j]
                        tmp_childes.add(DataNarrowMulti(
                            ca3.id,
                            ca3.name,
                            ca3.parent_id,
                            checkSelect(3, ca3.id)
                        ))
                    }
                }
                childes.add(tmp_childes)
            }
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        initLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initLayout() {

        // タイトルはデータ取得後になる
        if (supportActionBar != null) {
            supportActionBar!!.title = parentName
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        binding.textViewCategory1Title.text = "%s の全て".format(parentName)

        // カテゴリー１ //
        binding.layoutCategory1.setOnClickListener {
            ActivitySpotSearchNarrow.selectCategory1(parentId)
            val intent = Intent(this, ActivitySpotSearchNarrow::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            setResult(RESULT_OK, intent)
            startActivity(intent)
        }

        // listView //
        mAdapter = ListNarrowCategoryAdapter(this, groups,childes)

        binding.listView.apply {

            setAdapter(mAdapter)
            // セクション
            setOnGroupClickListener { expandableListView, view, i, l ->
                val checkbox: CheckBox = view.findViewById(R.id.checkbox)
                // close -> open or open -> close
                checkbox.isChecked = isGroupExpanded(i)

                // 第２カテゴリー選択した場合は子３カテゴリーのチェックを外す
                if (checkbox.isChecked) {
                    for (j in 0..childes[i].size - 1) {
                        childes[i][j].checked = false
                    }
                }
                false
            }

            // アイテム
            // checkbox: focusable=falseにすること
            setOnChildClickListener { expandableListView, view, i, i2, l ->
                val checkbox: CheckBox = view.findViewById(R.id.checkbox)
                checkbox.isChecked = !checkbox.isChecked
                true
            }

            // 初回のセクション開閉を設定する
            for (i in 0..groups.size - 1) {
                if (!groups[i].checked) {
                    expandGroup(i)
                }
            }
        }

        // 条件をクリア
        binding.buttonClear.setOnClickListener {
            val tmp_groups: ArrayList<DataNarrowMulti> = arrayListOf()
            val tmp_childes: ArrayList<ArrayList<DataNarrowMulti>> = arrayListOf()
            for (i in 0..groups.size - 1) {
                tmp_groups.add(groups[i]); tmp_childes.add(childes[i])
                tmp_groups[i].checked = false
                for (j in 0..childes[i].size - 1) {
                    tmp_childes[i][j].checked = false
                }
            }

            // クリアして、更新
            groups.clear(); childes.clear()
            groups.addAll(tmp_groups); childes.addAll(tmp_childes)
            mAdapter.notifyDataSetChanged()

            // 全て開く
            binding.listView.apply {
                for (i in 0..groups.size - 1) {
                    if (!groups[i].checked) {
                        expandGroup(i)
                    }
                }
            }
        }

        // 決定
        binding.buttonSearch.setOnClickListener {
            // 2つ前に戻る場合があるため
            ActivitySpotSearchNarrow.selectFinalCategory(parentId, groups, childes)
            val intent = Intent(this, ActivitySpotSearchNarrow::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            setResult(RESULT_OK, intent)
            startActivity(intent)
        }
    }

    private fun checkSelect(level: Int, id: Int): Boolean {
        for (i in 0..selectCategory.size - 1) {
            if (selectCategory[i].first == level) {
                if (selectCategory[i].second == id) {
                    return true
                }
            }
        }
        return false
    }


}
