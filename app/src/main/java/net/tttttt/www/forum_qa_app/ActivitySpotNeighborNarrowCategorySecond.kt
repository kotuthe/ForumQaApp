package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_spot_neighbor_narrow_category.*
import net.tttttt.www.forum_qa_app.entities.DataCategory2
import net.tttttt.www.forum_qa_app.entities.DataCategory3
import net.tttttt.www.forum_qa_app.entities.DataListSimple
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableCategory2
import net.tttttt.www.forum_qa_app.storage.DBTableCategory3
import net.tttttt.www.forum_qa_app.value.ifNotNull
import net.tttttt.www.forum_qa_app.view.ListSimpleAdapter


/** カテゴリー2 **/
class ActivitySpotNeighborNarrowCategorySecond : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotNeighborNarrowCategorySecond"
        val TAG_SHORT = "SNNCategory2"
    }

    // 変数 //
    private var parentId: Int = 0
    private var parentName: String = ""
    private var dataArray: ArrayList<DataCategory2> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_neighbor_narrow_category)

        val intent = intent
        parentId = intent.getIntExtra("id", 0)
        ifNotNull(intent.getStringExtra("name"), {
            parentName = it
        })
        val db = DBHelper(this)
        try {
            dataArray = DBTableCategory2(this).getTargetChild(db, parentId)
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = parentName
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        setListView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setListView() {
        val mAdapter = ListSimpleAdapter(this, 0)
        mAdapter.add(setListData("${parentName}の全て"))
        for (i in 0..dataArray.size - 1) {
            mAdapter.add(setListData(dataArray[i].name))
        }
        listView.apply {
            adapter = mAdapter
            setOnItemClickListener{ parent, view, position, id ->
                if (position == 0) {
                    // 全て
                    ActivitySpotNeighborNarrow.selectFinalCategory(1, parentId)
                    finish()
                } else {
                    val index = position - 1
                    // Log.i(">> $TAG_SHORT", "category: ${dataArray[index].name}")
                    val intent = Intent(context, ActivitySpotNeighborNarrowCategoryThird::class.java)
                    intent.putExtra("id", dataArray[index].id)
                    intent.putExtra("name", dataArray[index].name)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setListData(title: String) : DataListSimple {
        val data = DataListSimple(title, true)
        data.titleColor = Color.BLACK
        data.titleSize = 14f
        return data
    }
}

/** カテゴリー3 **/
class ActivitySpotNeighborNarrowCategoryThird : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotNeighborNarrowCategoryThird"
        val TAG_SHORT = "SNNCategory3"
    }

    // 変数 //
    private var parentId: Int = 0
    private var parentName: String = ""
    private var dataArray: ArrayList<DataCategory3> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_neighbor_narrow_category)

        val intent = intent
        parentId = intent.getIntExtra("id", 0)
        ifNotNull(intent.getStringExtra("name"), {
            parentName = it
        })

        val db = DBHelper(this)
        try {
            dataArray = DBTableCategory3(this).getTargetChild(db, parentId)
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = parentName
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        setListView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setListView() {
        val mAdapter = ListSimpleAdapter(this, 0)
        mAdapter.add(setListData("${parentName}の全て"))
        for (i in 0..dataArray.size - 1) {
            mAdapter.add(setListData(dataArray[i].name))
        }
        listView.apply {
            adapter = mAdapter
            setOnItemClickListener{ parent, view, position, id ->
                if (position == 0) {
                    // 全て
                    doFinish(2, parentId)
                } else {
                    val index = position - 1
                    doFinish(3, dataArray[index].id)
                }
            }
        }
    }

    private fun setListData(title: String) : DataListSimple {
        val data = DataListSimple(title, true)
        data.titleColor = Color.BLACK
        data.titleSize = 14f
        return data
    }

    private fun doFinish(type: Int, id: Int) {
        ActivitySpotNeighborNarrow.selectFinalCategory(type, id)
        // 最初のActivityに戻る
        val intent = Intent(this, ActivitySpotNeighborNarrow::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, ActivitySpotNeighborNarrow.REQUEST_CATEGORY)
    }
}
