package net.tochinavi.www.tochinaviapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_input_review.*
import net.tochinavi.www.tochinaviapp.view.RecyclerInputReviewAdapter
import java.util.*
import kotlin.collections.ArrayList

class ActivityInputReview : AppCompatActivity() {

    companion object {
        val TAG = "ActivityInputReview"
    }

    // データ
    private var mContext: Context? = null
    private var imageAdapter: RecyclerInputReviewAdapter? = null
    private var imageListData: ArrayList<String?> = arrayListOf(null) // 最初にnullを１つ入れる
    private var condVisitDate: Array<Int> = Array(3) {0}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_review)


        mContext = applicationContext

        // 予め写真があればimageListDataにインプットする(１０より少ない場合は最後にnullする)
        imageListData = arrayListOf(
            "http://placehold.jp/24/DDFF22/000000/150x150.png",
            "http://placehold.jp/24/55BBCC/000000/150x150.png",
            "http://placehold.jp/24/9955CC/000000/150x150.png",
            "http://placehold.jp/24/55BBCC/000000/150x150.png",
            "http://placehold.jp/24/9955CC/000000/150x150.png",
            "http://placehold.jp/24/DDFF22/000000/150x150.png",
            "http://placehold.jp/24/55BBCC/000000/150x150.png",
            "http://placehold.jp/24/9955CC/000000/150x150.png",
            "http://placehold.jp/24/DDFF22/000000/150x150.png",
            null
        )

        if (supportActionBar != null) {
            supportActionBar!!.title = "クチコミ投稿"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        initLayout()
    }
    // buttonReviewClose

    fun initLayout() {

        // 訪問日
        setVisitDateTitle()
        layoutVisitValue.setOnClickListener {
            showCalendar()
        }

        // クチコミ
        buttonReviewClose.visibility = View.INVISIBLE

        editTextReview.apply {
            setOnFocusChangeListener { view, hasFocus ->
                buttonReviewClose.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
            }
        }

        buttonReviewClose.setOnClickListener {
            hideKeyboard()
        }

        // 写真
        imageAdapter = RecyclerInputReviewAdapter(mContext!!, imageListData)
        rcvImage.apply {
            setHasFixedSize(true)

            val lmg = GridLayoutManager(context, imageAdapter!!.spanCount)
            lmg.spanSizeLookup = imageAdapter!!.spanSizeLookup
            layoutManager = lmg

            addItemDecoration(imageAdapter!!.mItemDecoration)
            adapter = imageAdapter
        }

        imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            val index = view.id
            val item = imageListData[index]
            Log.i(">> $TAG",  if (item != null) "写真拡大 : $index" else "追加" )
        })
    }

    /**
     * キーボード閉じる
     */
    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        layoutContent.requestFocus()
    }

    // 戻るボタンを押した時しか取得できない
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.i(">> $TAG", "KB ${KeyEvent.KEYCODE_BACK} == KC $keyCode")
        return super.onKeyDown(keyCode, event)
        /*if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        } else {
            // Backキーを押したとき
            // backPage();
            return false
        }*/
    }

    /**
     * 訪問日の表示
     */
    private fun setVisitDateTitle() {
        var title = "選択してください"
        if (condVisitDate[0] != 0) {
            title = "%d年 %d月 %d日".format(condVisitDate[0], condVisitDate[1], condVisitDate[2])
        }
        textViewVisitDate.text = title
    }

    /**
     * 訪問日カレンダー
     */
    private fun showCalendar() {

        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)

        var initYear: Int = year
        var initMonth: Int = month
        var initDay: Int = day

        if (condVisitDate[0] != 0) {
            initYear = condVisitDate[0]
            initMonth = condVisitDate[1] - 1
            initDay = condVisitDate[2]
        }

        val maxDate = GregorianCalendar(year, 11, 31)
        val minDate = GregorianCalendar(year - 1, 0, 1)

        val datePicker = DatePicker(this)
        datePicker.also {
            it.init(initYear, initMonth, initDay, null)
            it.minDate = minDate.timeInMillis
            it.maxDate = maxDate.timeInMillis
        }

        AlertDialog.Builder(this)
            .setView(datePicker)
            .setTitle("訪問日")
            .setPositiveButton("選択", DialogInterface.OnClickListener { dialog, which ->
                val y = datePicker.year
                val m = datePicker.month + 1
                val d = datePicker.dayOfMonth
                // Log.i(">> $TAG", "%d-%d-%d".format(y, m ,d))
                condVisitDate = arrayOf(y, m, d)
                setVisitDateTitle()
            })
            .setNegativeButton("キャンセル", null)
            .show()
    }
}
