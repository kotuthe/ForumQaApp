package net.tttttt.www.forum_qa_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_spot_search_narrow.*
import net.tttttt.www.forum_qa_app.entities.DataCategory1
import net.tttttt.www.forum_qa_app.entities.DataCategory2
import net.tttttt.www.forum_qa_app.entities.DataCategory3
import net.tttttt.www.forum_qa_app.entities.DataNarrowMulti
import net.tttttt.www.forum_qa_app.storage.*
import net.tttttt.www.forum_qa_app.view.LoadingNormal


/*
  条件：カテゴリー（第１ and 2 ~ 3）: 複数、エリア（第２のみ）: 複数、クーポン
  ワードあり：クリア後にカテゴリー１から選択、なし：クリア後にカテゴリー２から選択
 */
class ActivitySpotSearchNarrow : AppCompatActivity() {

    companion object {
        val TAG = "ActivitySpotSearchNarrow"
        val TAG_SHORT = "SpotSearchNarrow"

        // onActivityResultでしか使用できない //
        var IS_ACTIVITY_RESULT: Boolean = false
        var FINAL_CATEGORY_1: Int = 0
        var finalMultiGroups: ArrayList<DataNarrowMulti>? = null
        var finalMultiChildes: ArrayList<ArrayList<DataNarrowMulti>>? = null

        // 選択：第１カテゴリー
        fun selectCategory1(parent_id: Int) {
            IS_ACTIVITY_RESULT = true
            FINAL_CATEGORY_1 = parent_id
            finalMultiGroups = null
            finalMultiChildes = null
        }

        // 選択：第２、３カテゴリー
        fun selectFinalCategory(parent_id: Int, groups: ArrayList<DataNarrowMulti>, childes: ArrayList<ArrayList<DataNarrowMulti>>) {
            IS_ACTIVITY_RESULT = true
            FINAL_CATEGORY_1 = parent_id
            finalMultiGroups = groups
            finalMultiChildes = childes
        }

        // リクエスト
        val REQUEST_CATEGORY_MULTI: Int = 0x1
        val REQUEST_AREA: Int = 0x2
    }

    // UI //
    private lateinit var loading: LoadingNormal

    // 変数 //
    private lateinit var mContext: Context
    private var isFirstCategory: Boolean = false

    // 条件
    private var condWord: String = ""
    // Pair < first: level, second: id >
    private var condCategoryArray: ArrayList<Pair<Int, Int>> = arrayListOf()
    private var condAreaArray: ArrayList<Int> = arrayListOf()
    private var condCoupon: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_search_narrow)

        mContext = applicationContext

        condWord = intent.getStringExtra("word")!!
        condCategoryArray = intent.getSerializableExtra("category") as ArrayList<Pair<Int, Int>>
        condAreaArray = intent.getIntegerArrayListExtra("area")!!
        condCoupon = intent.getBooleanExtra("coupon", false)

        // 検索ワードがある場合は第１カテゴリーを選択させる
        if (!condWord.isEmpty()) {
            isFirstCategory = true
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.spot_neighbor_narrow_title)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            // first -> multi
            REQUEST_CATEGORY_MULTI -> {
                if (IS_ACTIVITY_RESULT) {
                    IS_ACTIVITY_RESULT = false
                    isFirstCategory = false
                    condCategoryArray.clear()
                    condCategoryArray.add(Pair(1, FINAL_CATEGORY_1))
                    if (finalMultiGroups != null && finalMultiChildes != null) {
                        for (i in 0..finalMultiGroups!!.size - 1) {
                            val group = finalMultiGroups!![i]
                            val childe = finalMultiChildes!![i]
                            if (group.checked) {
                                condCategoryArray.add(Pair(2, group.id))
                            }
                            for (j in 0..childe.size - 1) {
                                if (childe[j].checked) {
                                    condCategoryArray.add(Pair(3, childe[j].id))
                                }
                            }
                        }
                    }
                    setViewCategory()
                }

            }
            REQUEST_AREA -> {
                if (resultCode == Activity.RESULT_OK) {
                    condAreaArray = data!!.getIntegerArrayListExtra("area")!!
                    setViewArea()
                }
            }
        }
    }

    private fun initLayout() {

        // ローディング
        loading = LoadingNormal.newInstance( message = "", isProgress = true )

        // カテゴリー
        setViewCategory()
        layoutCategory.setOnClickListener {
            loading.show(supportFragmentManager, LoadingNormal.TAG)
            loading.onDismiss(500)

            Handler().postDelayed(Runnable {
                if (isFirstCategory) {
                    // 第 1 カテゴリー一覧へ
                    val intent = Intent(this@ActivitySpotSearchNarrow, ActivitySpotSearchNarrowCategory1::class.java)
                    startActivityForResult(intent, REQUEST_CATEGORY_MULTI)
                } else {
                    // 第 2 or 3 カテゴリー一覧へ
                    val intent = Intent(this@ActivitySpotSearchNarrow, ActivitySpotSearchNarrowCategoryMulti::class.java)
                    intent.putExtra("category", condCategoryArray)
                    startActivityForResult(intent, REQUEST_CATEGORY_MULTI)
                }
            }, 300)

        }

        // エリア
        setViewArea()
        textViewAreaTitle.text = "エリア"
        layoutArea.setOnClickListener {
            val intent = Intent(this@ActivitySpotSearchNarrow, ActivitySpotSearchNarrowArea::class.java)
            intent.putExtra("area", condAreaArray)
            startActivityForResult(intent, REQUEST_AREA)
        }

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

            // カテゴリーのクリア
            val parent_ca_id = if (condCategoryArray.size > 0) condCategoryArray[0].second else 0
            condCategoryArray.clear()
            if (!condWord.isEmpty()) {
                isFirstCategory = true
            } else {
                if (parent_ca_id > 0) {
                    condCategoryArray.add(Pair(1, parent_ca_id))
                }
            }
            setViewCategory()

            // エリアのクリア
            condAreaArray.clear()
            setViewArea()

            // クーポンクリア
            condCoupon = false
            checkboxCoupon.isChecked = false
        }

        // 検索
        buttonSearch.setOnClickListener {
            val intent = Intent()
            intent.putExtra("word", condWord)
            intent.putExtra("category", condCategoryArray)
            intent.putExtra("area", condAreaArray)
            intent.putExtra("coupon", condCoupon)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // 条件からレイアウトの更新
        checkboxCoupon.isChecked = condCoupon
    }

    private fun setViewCategory() {
        var title: String? = null
        var value: String? = null
        val size = condCategoryArray.size
        val db = DBHelper(mContext)
        try {
            // 第 1 カテゴリー
            if (size > 0) {
                title = getCategoryName(db, condCategoryArray[0])
            }
            // 第 2 or 3 カテゴリー
            if (size > 1) {
                // ※ [size - 1] はランダムにする
                value = getCategoryName(db, condCategoryArray[size - 1])
            }

        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        // 第 1 カテゴリー
        textViewCategoryTitle.apply {
            if (title == null) {
                text = "カテゴリー"
            } else  {
                text = title
            }
        }

        // 第 2 or 3 カテゴリー
        textViewCategoryValue.apply {
            if (value == null) {
                text = "指定なし"
                setTextColor(ContextCompat.getColor(mContext, R.color.colorTextGray))
            } else  {
                // 3つ以上ある時は「ほか」をつける
                text = if (size > 2) "%s、ほか%d".format(value, size - 2) else value
                setTextColor(Color.BLACK)
            }
        }
    }

    // カテゴリーの名前を取得
    // Pair < first: level, second: id >
    private fun getCategoryName(db: DBHelper, item: Pair<Int, Int>): String {
        var title = ""
        val id = item.second
        when (item.first) {
            1 -> {
                val data1: DataCategory1 =
                    DBTableCategory1(mContext).getData(db, id)
                title = data1.name
            }
            2 -> {
                val data2: DataCategory2 =
                    DBTableCategory2(mContext).getData(db, id)
                title = data2.name
            }
            3 -> {
                val data3: DataCategory3 =
                    DBTableCategory3(mContext).getData(db, id)
                title = data3.name
            }
            else -> {
            }
        }
        return title
    }

    private fun setViewArea() {
        var value: String? = null
        val size = condAreaArray.size
        val db = DBHelper(mContext)
        try {
            // 第 2 エリア
            if (size > 0) {
                // ※ [size - 1] はランダムにする
                value = DBTableArea2(mContext).getData(db, condAreaArray[size - 1]).name
            }

        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }

        // 第 2 エリア
        textViewAreaValue.apply {
            if (value == null) {
                text = "指定なし"
                setTextColor(ContextCompat.getColor(mContext, R.color.colorTextGray))
            } else  {
                // 3つ以上ある時は「ほか」をつける
                text = if (size > 1) "%s、ほか%d".format(value, size - 1) else value
                setTextColor(Color.BLACK)
            }
        }
    }


}
