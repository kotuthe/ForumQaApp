package net.tochinavi.www.tochinaviapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_spot_search_narrow_category1.*
import net.tochinavi.www.tochinaviapp.entities.DataCategory1
import net.tochinavi.www.tochinaviapp.entities.DataListSimple
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableCategory1
import net.tochinavi.www.tochinaviapp.view.ListSimpleAdapter

class ActivitySpotSearchNarrowCategory1 : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotSearchNarrowCategory1"
        val TAG_SHORT = "SNNCategory1"
    }

    // データ //
    private lateinit var mContext: Context
    private var dataCategory: ArrayList<DataCategory1> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_search_narrow_category1)

        if (supportActionBar != null) {
            supportActionBar!!.title = "カテゴリー"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mContext = applicationContext
        val db = DBHelper(mContext)
        try {
            dataCategory = DBTableCategory1(mContext).getAll(db)
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
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
        for (i in 0..dataCategory.size - 1) {
            mAdapter.add(DataListSimple(dataCategory[i].name, true))
        }
        listView.apply {
            adapter = mAdapter
            setOnItemClickListener{ parent, view, position, id ->
                // multiへ
                val intent = Intent(context, ActivitySpotSearchNarrowCategoryMulti::class.java)
                intent.putExtra("category", arrayListOf(Pair(1, dataCategory[position].id)))
                startActivity(intent)
            }
        }
    }
}
