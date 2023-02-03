package net.tttttt.www.forum_qa_app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_spot_neighbor_narrow.*
import net.tttttt.www.forum_qa_app.entities.DataCategory1
import net.tttttt.www.forum_qa_app.entities.DataListSimple
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableCategory1
import net.tttttt.www.forum_qa_app.storage.DBTableCategory2
import net.tttttt.www.forum_qa_app.storage.DBTableCategory3
import net.tttttt.www.forum_qa_app.view.ListSimpleAdapter


class ActivitySpotNeighborNarrow : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotNeighborNarrow"
        val TAG_SHORT = "SpotNeighborNarrow"

        // カテゴリー
        val REQUEST_CATEGORY: Int = 0x1
        var FINAL_CATEGORY_TYPE: Int = 0
        var FINAL_CATEGORY_ID: Int = 0
        fun selectFinalCategory(type: Int, id: Int) {
            FINAL_CATEGORY_TYPE = type
            FINAL_CATEGORY_ID = id
        }

        // 距離
        val dataDistanceArray: ArrayList<Double> = arrayListOf(0.5, 2.0, 5.0)
    }

    // 変数 //
    private var dataCategoryArray: ArrayList<DataCategory1> = ArrayList()
    // private val dataDistanceArray: ArrayList<Double> = arrayListOf(0.5, 2.0, 5.0)
    // 条件
    private var condCategory: Int = 0
    private var condCategoryType: Int = 1 // 1,2,3
    private var condDistance: Double = dataDistanceArray[0]
    private var condCoupon: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_neighbor_narrow)

        condCategory = intent.getIntExtra("category_id", 0)
        condCategoryType = intent.getIntExtra("category_type", 0)
        condDistance = intent.getDoubleExtra("distance", 0.0)
        condCoupon = intent.getBooleanExtra("coupon", false)

        val db = DBHelper(this)
        try {
            dataCategoryArray = DBTableCategory1(this).getAll(db)
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.spot_neighbor_narrow_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Log.i("$TAG_SHORT", "req: $requestCode, res: $resultCode")
        when(requestCode) {
            // カテゴリー
            REQUEST_CATEGORY -> {
                if (FINAL_CATEGORY_ID == 0) { return }
                condCategoryType = FINAL_CATEGORY_TYPE
                condCategory = FINAL_CATEGORY_ID
                selectFinalCategory(0, 0) // Final値をzeroにする

                // レイアウト
                listViewCategory.visibility = View.GONE
                textViewCategoryTitle.text = getCategoryTitle()
                imageViewCategoryAnchor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_plus))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 初回レイアウト設定
     */
    private fun initLayout() {

        setListViewCategory()
        setListViewDistance()

        // クーポン
        layoutCoupon.setOnClickListener {
            checkboxCoupon.isChecked = !checkboxCoupon.isChecked
            condCoupon = checkboxCoupon.isChecked
        }
        checkboxCoupon.setOnClickListener {
            condCoupon = checkboxCoupon.isChecked
        }

        // クリア
        buttonClear.setOnClickListener {
            condCategory = 0
            condCategoryType = 1
            condDistance = dataDistanceArray[0]
            condCoupon = false

            updateLayoutCategory(true)
            updateLayoutDistance(true)
            checkboxCoupon.isChecked = false
        }

        // 検索
        buttonSearch.setOnClickListener {
            val intent = Intent()
            intent.putExtra("category_id", condCategory)
            intent.putExtra("category_type", condCategoryType)
            intent.putExtra("distance", condDistance)
            intent.putExtra("coupon", condCoupon)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // 条件からレイアウトの更新
        checkboxCoupon.isChecked = condCoupon
    }

    /**
     * カテゴリーの一覧設定
     */
    private fun setListViewCategory() {
        val mAdapter = ListSimpleAdapter(this, 0)
        for (i in 0..dataCategoryArray.size - 1) {
            mAdapter.add(setListData(dataCategoryArray[i].name))
        }
        listViewCategory.apply {
            adapter = mAdapter
            setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(context, ActivitySpotNeighborNarrowCategorySecond::class.java)
                intent.putExtra("id", dataCategoryArray[position].id)
                intent.putExtra("name", dataCategoryArray[position].name)
                startActivityForResult(intent, REQUEST_CATEGORY)
            }
        }
        listViewCategory.visibility = View.GONE
        textViewCategoryTitle.text = getCategoryTitle()
        layoutCategory.setOnClickListener{
            if (listViewCategory.isVisible) {
                // 閉じる
                updateLayoutCategory(true)
            } else {
                // 開く
                updateLayoutCategory(false)
            }
        }
    }

    private fun updateLayoutCategory(close: Boolean) {
        if (close) {
            // 閉じる
            listViewCategory.visibility = View.GONE
            textViewCategoryTitle.text = getCategoryTitle()
            imageViewCategoryAnchor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_plus))
        } else {
            // 開く
            listViewCategory.visibility = View.VISIBLE
            textViewCategoryTitle.text = getString(R.string.spot_neighbor_narrow_select)
            imageViewCategoryAnchor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_minus))
        }
    }

    private fun setListData(title: String) : DataListSimple {
        val data = DataListSimple(title, true)
        data.titleColor = Color.BLACK
        data.titleSize = 14f
        return data
    }

    /**
     * 距離の一覧設定
     */
    private fun setListViewDistance() {
        val fromKey = "title"
        val listItem = ArrayList<HashMap<String, String>>()
        var selectIndex = 0
        for (i in 0..dataDistanceArray.size - 1) {
            val item = dataDistanceArray[i]
            if (item == condDistance) {
                selectIndex = i
            }
            val value = getDistanceTitle(item)
            listItem.add(hashMapOf(fromKey to value))
        }
        val mAdapter = SimpleAdapter(
            applicationContext,
            listItem,
            R.layout.cell_narrow_distance,
            arrayOf(fromKey),
            intArrayOf(R.id.textViewTitle)
        )
        listViewDistance.apply {
            adapter = mAdapter
            choiceMode = AbsListView.CHOICE_MODE_SINGLE
            setItemChecked(selectIndex, true)
            setOnItemClickListener { parent, view, position, id ->
                condDistance = getSelectDistance()
            }
        }

        // 最初から開いている場合は下２行を消すだけ
        listViewDistance.visibility = View.GONE
        textViewDistanceTitle.text = getDistanceTitle(condDistance)

        // イベント
        layoutDistance.setOnClickListener{
            if (listViewDistance.isVisible) {
                // 閉じる
                updateLayoutDistance(true)
            } else {
                // 開く
                updateLayoutDistance(false)
            }
        }
    }

    private fun updateLayoutDistance(close: Boolean) {
        if (close) {
            // 閉じる
            listViewDistance.visibility = View.GONE
            textViewDistanceTitle.text = getDistanceTitle(condDistance)
            imageViewDistanceAnchor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_plus))
        } else {
            // 開く
            listViewDistance.visibility = View.VISIBLE
            textViewDistanceTitle.text = getString(R.string.spot_neighbor_narrow_select)
            imageViewDistanceAnchor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_minus))
        }
    }

    /**
     * カテゴリーの選択値を取得
     */
    private fun getCategoryTitle(): String {
        if (condCategoryType == 1) {
            for (i in 0..dataCategoryArray.size - 1) {
                if (dataCategoryArray[i].id == condCategory) {
                    return dataCategoryArray[i].name
                }
            }
        } else {
            var name: String = ""
            val db = DBHelper(this)
            try {
                when (condCategoryType) {
                    2 -> {
                        val data = DBTableCategory2(this).getData(db, condCategory)
                        name = data.name
                    }
                    3 -> {
                        val data = DBTableCategory3(this).getData(db, condCategory)
                        name = data.name
                    }
                }
                // dataCategoryArray = DBTableCategory1(this).getAll(db)
            } catch (e: Exception) {
                Log.e(TAG_SHORT, "" + e.message)
            } finally {
                db.cleanup()
            }
            return name
        }
        return getString(R.string.spot_neighbor_narrow_no_select)
    }

    /**
     * 距離の選択値を取得
     */
    private fun getSelectDistance(): Double {
        var rVal: Double = 0.0
        val checked = listViewDistance.checkedItemPositions
        for (i in 0..checked.size() - 1) {
            // checked: checked.valueAt(i), index: checked.keyAt(i)
            if (checked.valueAt(i)) {
                rVal = dataDistanceArray[checked.keyAt(i)]
                break
            }
        }
        return rVal
    }

    /**
     * 距離のタイトル取得
     */
    private fun getDistanceTitle(v: Double): String {
        var rVal = "${v} km"
        if (v >= 1.0) {
            // km
            rVal = "${v.toInt()} km"
        }
        return rVal
    }
}
