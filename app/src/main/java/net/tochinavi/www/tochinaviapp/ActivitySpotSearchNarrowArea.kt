package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_spot_search_narrow_area.*
import net.tochinavi.www.tochinaviapp.entities.DataNarrowMulti
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableArea1
import net.tochinavi.www.tochinaviapp.storage.DBTableArea2
import net.tochinavi.www.tochinaviapp.view.ListNarrowAreaAdapter

class ActivitySpotSearchNarrowArea :
    AppCompatActivity() {

    companion object {
        const val TAG = "ActivitySpotSearchNarrowArea"
        const val TAG_SHORT = "SSNarrowArea"
    }

    // データ //
    private var selectArea: ArrayList<Int> = arrayListOf()
    private lateinit var mAdapter: ListNarrowAreaAdapter
    private val groups: ArrayList<String> = arrayListOf()
    private val childes: ArrayList<ArrayList<DataNarrowMulti>> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_search_narrow_area)

        selectArea = intent.getIntegerArrayListExtra("area")!!

        // エリア１、２を取得して
        val db = DBHelper(this)
        try {
            val array_area1 = DBTableArea1(this).getAll(db)
            for (i in 0..array_area1.size - 1) {
                val area1 = array_area1[i]
                groups.add(area1.name)

                val tmp_childes: ArrayList<DataNarrowMulti> = arrayListOf()
                val array_area2 = DBTableArea2(this).getTargetChild(db, area1.id)
                if (array_area2.size > 0) {
                    for (j in 0..array_area2.size - 1) {
                        val area2 = array_area2[j]
                        tmp_childes.add(DataNarrowMulti(
                            area2.id,
                            area2.name,
                            area2.parent_id,
                            checkSelect(area2.id)
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
            supportActionBar!!.title = "エリア"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // listView //
        mAdapter = ListNarrowAreaAdapter(this, groups,childes)

        listView.apply {

            setAdapter(mAdapter)
            // セクション(true: 折りたたまれない)
            setOnGroupClickListener { expandableListView, view, i, l ->
                true
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
                expandGroup(i)
            }
        }

        // 条件をクリア
        buttonClear.setOnClickListener {
            val tmp_childes: ArrayList<ArrayList<DataNarrowMulti>> = arrayListOf()
            for (i in 0..groups.size - 1) {
                tmp_childes.add(childes[i])
                for (j in 0..childes[i].size - 1) {
                    tmp_childes[i][j].checked = false
                }
            }

            // クリアして、更新
            childes.clear()
            childes.addAll(tmp_childes)
            mAdapter.notifyDataSetChanged()
        }

        // 決定
        buttonSearch.setOnClickListener {
            val intent = Intent()
            val array_child: ArrayList<Int> = arrayListOf()
            for (i in 0..groups.size - 1) {
                for (j in 0..childes[i].size - 1) {
                    if (childes[i][j].checked) {
                        array_child.add(childes[i][j].id)
                    }
                }
            }
            intent.putExtra("area", array_child)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun checkSelect(id: Int): Boolean {
        if (selectArea.size > 0) {
            for (i in 0..selectArea.size - 1) {
                if (selectArea[i] == id) {
                    return true
                }
            }
        }
        return false
    }
}
