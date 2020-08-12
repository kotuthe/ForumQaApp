package net.tochinavi.www.tochinaviapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.andremion.louvre.Louvre
import com.andremion.louvre.home.GalleryActivity
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpUpload
import kotlinx.android.synthetic.main.activity_input_review.*
import net.tochinavi.www.tochinaviapp.entities.DataBadge
import net.tochinavi.www.tochinaviapp.entities.DataMyDraftReview
import net.tochinavi.www.tochinaviapp.entities.DataReviewTag
import net.tochinavi.www.tochinaviapp.entities.DataSpotInfo
import net.tochinavi.www.tochinaviapp.network.TaskDownloadImage
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.convertDpToPx
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.*
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil


/**
 * fuelのデフォルトがUTF-8のため日本語のエンコードは行わない
 *
 * ※後でやること
 * ・写真プレビュー
 */

class ActivityInputReview :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener,
    ModalGetBadge.OnModalGetBadgeClickListener {

    companion object {
        val TAG = "ActivityInputReview"
    }
    // 定数 //
    private val REQUEST_SELECT_IMAGE_PICKER: Int = 1
    private val REQUEST_ALERT_IMAGE_DELETE: Int = 2
    private val REQUEST_SELECT_TAG: Int = 3
    private val REQUEST_ALERT_POST_CONF: Int = 4 // 投稿する
    private val REQUEST_ALERT_SAVE_CONF: Int = 5 // 下書きする
    private val REQUEST_ALERT_DELETE_CONF: Int = 6 // 下書き削除
    private val REQUEST_ALERT_ACTION_FINISH: Int = 7 // 削除・下書き・投稿の終了後
    private val REQUEST_ALERT_BACK_PAGE: Int = 8 // 戻るボタン

    private val MAX_SELECT_IMAGE: Int = 10

    // UI //
    private lateinit var loading: LoadingNormal

    // データ //
    private var mContext: Context? = null
    private var mySP: MySharedPreferences? = null
    private lateinit var dataSpot: DataSpotInfo
    private var isDraft: Boolean = false
    private var dataDraftReview: DataMyDraftReview? = null
    private var reviewType: Int = 1 // 1: All, 2: Image Only
    private var imageAdapter: RecyclerInputReviewAdapter? = null
    private var imageListData: ArrayList<Uri?> = arrayListOf(null) // 最初にnullを１つ入れる
    private var deleteItemIndex: Int = 0

    // Web写真やりとり
    private var taskDLImage: TaskDownloadImage? = null
    private var dlImageIndex: Int = 0
    private var dirImageList: ArrayList<Uri> = arrayListOf() // 内部ストレージURI

    // 送信データ
    private var condVisitDate: Array<Int> = Array(3) {0}
    private var condTags: ArrayList<DataReviewTag> = ArrayList()
    private var condReview: String = "" // サーバーで修正したクチコミ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_review)


        mContext = applicationContext
        mySP = MySharedPreferences(mContext!!)
        loading = LoadingNormal.newInstance(
            message = "", isProgress = true)

        ifNotNull(intent.getBooleanExtra("isDraft", false), {
            isDraft = it
            if (!isDraft) {
                // 新規
                dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
            } else {
                // 下書き
                dataDraftReview = intent.getSerializableExtra("dataReview") as DataMyDraftReview
                dataSpot = DataSpotInfo(
                    dataDraftReview!!.spotId, 1,"", false,
                    dataDraftReview!!.spotName, "", "", "",
                    "", "", "", 0.0,
                    0.0, 0, false,
                    false, true, false,
                    "", "", 0, false
                )
            }
        })

        reviewType = intent.getIntExtra("type", 1)

        if (supportActionBar != null) {
            supportActionBar!!.title = "クチコミ投稿"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        textViewSpotName.text = dataSpot.name

        initLayout()
    }

    override fun onPause() {
        super.onPause()
        Log.i(">> $TAG", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()

        // キャッシュファイルの削除
        // ※アプリの強制終了の時は呼ばれないため注意
        if (!dirImageList.isEmpty()) {
            for (i in 0..dirImageList.size - 1) {
                try {
                    val f = File(dirImageList[i].path!!)
                    f.delete()
                } catch(e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            backPage()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * homeまたはback keyを押した時
     */
    private fun backPage() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_BACK_PAGE,
            title = "入力の途中です。入力を中断して、前のページに戻りますか？",
            msg = null,
            positiveLabel = "戻る",
            negativeLabel = "キャンセル"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    /**
     * Web画像のダウンロード
     */
    private fun downloadImage() {
        if (dlImageIndex < dataDraftReview!!.reviewImageUrls.size) {
            Log.i(">> $TAG", "$dlImageIndex: ${dataDraftReview!!.reviewImageUrls[dlImageIndex]}")
            runOnUiThread {
                loading.updateLayout("写真を読み込み中\n%d%%...",
                    dlImageIndex.toDouble(), dataDraftReview!!.reviewImageUrls.size.toDouble())
            }
            taskDLImage = TaskDownloadImage(mContext!!)
            taskDLImage!!.setListener(downloadImageListener())
            taskDLImage!!.execute(dataDraftReview!!.reviewImageUrls[dlImageIndex])
            dlImageIndex++
        } else {
            loading.onDismiss()
            addImageListData(dirImageList)
        }
    }

    /**
     * Web画像のダウンロードListener
     */
    private fun downloadImageListener(): TaskDownloadImage.Listener? {
        return object : TaskDownloadImage.Listener {

            override fun onSuccess(uri: Uri?) {
                if (uri != null && uri.path != null) {
                    dirImageList.add(uri)
                    downloadImage()
                } else {
                    // URLが写真でないのが含まれている場合は
                    // 写真の表示はしない
                    runOnUiThread {
                        loading.onDismiss()
                    }
                }
            }
        }
    }

    /**
     * ギャラーから追加した画像
     */
    private fun addImageListData(uris: ArrayList<Uri>) {
        println(uris)
        for (i in 0..uris.size - 1) {
            Log.i(">> $TAG", uris[i].path)
            imageListData[imageListData.size - 1] = uris[i]
            if (imageListData.size < MAX_SELECT_IMAGE) {
                // 9アイテム以下の時まではADDを追加
                imageListData.add(null)
            }
        }
        imageAdapter!!.notifyDataSetChanged()
    }

    /**
     * ギャラリー画像を削除
     */
    private fun removeImageListData(index: Int) {
        imageListData.removeAt(index)
        if (imageListData[imageListData.size - 1] != null) {
            imageListData.add(null)
        }
        imageAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_IMAGE_PICKER -> {
                // 写真の選択後
                if (resultCode == Activity.RESULT_OK) {
                    val uris: List<Uri> = GalleryActivity.getSelection(data)
                    if (uris.count() > 0) {
                        addImageListData(ArrayList(uris))
                    }
                }
            }
            REQUEST_SELECT_TAG -> {
                // タグの選択
                if (resultCode == Activity.RESULT_OK) {
                    ifNotNull(data, {
                        condTags = it.getSerializableExtra("selectTags") as ArrayList<DataReviewTag>
                    })
                    updateViewTag()
                }
            }
        }
    }

    /**
     * アラート　ポジティブ
     */
    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_IMAGE_DELETE -> {
                removeImageListData(deleteItemIndex)
            }
            REQUEST_ALERT_POST_CONF -> {
                // 投稿へ(入力チェックへ)
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                checkInputData()
            }
            REQUEST_ALERT_SAVE_CONF -> {
                // 下書きへ(画像のアップロードへ)
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                uploadImage(true)
            }
            REQUEST_ALERT_DELETE_CONF -> {
                // 下書き削除へ
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                deleteDraft()
            }
            REQUEST_ALERT_ACTION_FINISH -> {
                // 削除・下書き・投稿の後
                val intent = Intent()
                setResult(RESULT_OK, intent);
                finish()
            }
            REQUEST_ALERT_BACK_PAGE -> {
                // 前のページに戻る
                finish()
            }

        }
    }

    /**
     * アラート　ネガティブ
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    /**
     * 称号の閉じるボタン
     */
    override fun onModalGetBadgeCloseClick() {
        finish()
    }

    fun initLayout() {

        // 条件に下書きデータを挿入
        if (isDraft) {
            ifNotNull(dataDraftReview) {
                // 訪問日
                if (!it.comingFlg.isEmpty()) {
                    condVisitDate = arrayOf(
                        it.comingFlg[0], it.comingFlg[1], it.comingFlg[2])
                }

                // クチコミ
                if (!it.review.isEmpty()) {
                    editTextReview.setText(it.review)
                }

                // 写真
                if (it.reviewImageUrls.size > 0) {
                    loading.show(supportFragmentManager, LoadingNormal.TAG)
                    downloadImage()
                }

                // タグ
                if (it.tagIds.size > 0) {
                    for (i in 0..it.tagIds.size - 1) {
                        condTags.add(it.tagIds[i])
                    }
                }
            }
        }

        // 訪問日
        setVisitDateTitle()
        layoutVisitValue.setOnClickListener {
            showCalendar()
        }

        // クチコミ
        layoutReview.visibility =
            if (reviewType == 1) View.VISIBLE else View.GONE
        buttonReviewClose.visibility = View.INVISIBLE
        editTextReview.apply {
            setOnFocusChangeListener { view, hasFocus ->
                buttonReviewClose.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                if (!isDraft) {
                    layoutNormalButtons.visibility = if (hasFocus) View.GONE else View.VISIBLE
                } else {
                    layoutDraftButtons.visibility = if (hasFocus) View.GONE else View.VISIBLE
                }
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

        // 写真クチコミができない時
        if (!dataSpot.imageEnable) {
            rcvImage.visibility = View.GONE
            textViewImageHint.text = "※%s様では撮影不可となっており、写真投稿をご遠慮いただいております。".format(dataSpot.name)
        } else {
            textViewImageHint.text = getString(R.string.input_review_image_hint).format(MAX_SELECT_IMAGE)
        }

        // アイテムの並び替え
        val touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN
                        or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                0
            ) {
                override fun isItemViewSwipeEnabled(): Boolean {
                    return false
                }

                // アイテムを選択した時 //
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    ifNotNull(viewHolder, {
                        val from = it.adapterPosition
                        if (imageAdapter!!.items[from] == null) {
                            // 「追加」は動かさない
                            clearView(rcvImage, it)
                        } else {
                            // 振動
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            val pattern = longArrayOf(10)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                            } else {
                                vibrator.vibrate(pattern, -1)
                            }
                            // ちょこっと動く
                            val moveSet = AnimatorSet()
                            val moveX = ObjectAnimator.ofFloat(it.itemView, "translationX", 10f)
                            val moveY = ObjectAnimator.ofFloat(it.itemView, "translationY", -10f)

                            moveX.duration = 100
                            moveY.duration = 40

                            moveSet.playTogether(moveX, moveY)
                            moveSet.start()

                            // トースト
                            val toast = Toast.makeText(applicationContext, "ドラッグする", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.TOP, 0, 100f.convertDpToPx(mContext!!).toInt())
                            toast.show()
                        }
                    })
                }

                // 移動 //
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // Log.i(">> $TAG", "onMove")
                    val from = viewHolder.adapterPosition
                    var to = target.adapterPosition

                    val toItem = imageAdapter!!.items[to]
                    if (toItem == null) {
                        to -= 1
                    }

                    val data = imageAdapter!!.items.removeAt(from)
                    imageAdapter!!.items.add(to, data)
                    imageAdapter!!.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Log.i(">> $TAG", "onSwiped")
                }

                // アイテム離した時 //
                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    Log.i(">> $TAG", "clearView")
                    imageAdapter!!.notifyDataSetChanged()
                }
            })

        touchHelper.attachToRecyclerView(rcvImage)

        // 写真プレビュー
        imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            val index = view.id

            when (view.tag) {
                imageAdapter!!.TAG_VIEW_IMAGE -> {
                    // プレビュー
                    Log.i(">> $TAG", "写真の拡大: $index")
                    val uris: ArrayList<Uri> = arrayListOf()
                    for (i in 0..imageListData.size - 1) {
                        if (imageListData[i] != null) {
                            uris.add(imageListData[i]!!)
                        }
                    }
                    val modal = ModalUriImagePreview.newInstance(uris, index)
                    modal.show(supportFragmentManager, ModalUriImagePreview.TAG)
                }
                imageAdapter!!.TAG_VIEW_BUTTON_DELETE -> {
                    // 写真の削除
                    deleteItemIndex = index
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_IMAGE_DELETE,
                        title = "写真を削除しますか？",
                        msg = null,
                        positiveLabel = "削除",
                        negativeLabel = "キャンセル"
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
                imageAdapter!!.TAG_VIEW_ADD -> {
                    // 写真の追加
                    Louvre.init(this)
                        .setRequestCode(REQUEST_SELECT_IMAGE_PICKER)
                        .setMaxSelection(MAX_SELECT_IMAGE - (imageListData.size - 1))
                        .setSelection(listOf<Uri>())
                        .setMediaTypeFilter(Louvre.IMAGE_TYPE_JPEG, Louvre.IMAGE_TYPE_PNG, Louvre.IMAGE_TYPE_BMP)
                        .open()
                }
            }
        })

        // タグの選択
        updateViewTag()
        layoutTag.setOnClickListener {
            // タグ選択へ
            val intent = Intent(this, ActivityInputReviewTag::class.java)
            intent.putExtra("id", dataSpot.id)
            intent.putExtra("selectTags", condTags)
            startActivityForResult(intent, REQUEST_SELECT_TAG)
        }



        val deleteListener: View.OnClickListener = View.OnClickListener {
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_DELETE_CONF,
                title = "クチコミを削除しますか？",
                msg = null,
                positiveLabel = "削除",
                negativeLabel = "キャンセル"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
        val draftListener: View.OnClickListener = View.OnClickListener {
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_SAVE_CONF,
                title = "クチコミを下書きしますか？",
                msg = null,
                positiveLabel = "下書き",
                negativeLabel = "キャンセル"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
        val saveListener: View.OnClickListener = View.OnClickListener {
            onClickPost()
        }

        var height = 0f
        if (!isDraft) {
            layoutNormalButtons.visibility = View.VISIBLE
            layoutDraftButtons.visibility = View.GONE
            height = 80f

            // 下書き
            buttonSave.setOnClickListener(draftListener)
            // 投稿
            buttonPost.setOnClickListener(saveListener)
        } else {
            layoutNormalButtons.visibility = View.GONE
            layoutDraftButtons.visibility = View.VISIBLE
            height = 134f

            // 下書きを削除
            buttonDraftDelete.setOnClickListener(deleteListener)
            // 下書き
            buttonDraftSave.setOnClickListener(draftListener)
            // 投稿
            buttonDraftPost.setOnClickListener(saveListener)
        }

        // フッターのマージン
        val lpm = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height.convertDpToPx(mContext!!).toInt()
        )
        layoutFooter.layoutParams = lpm
    }

    /**
     * タグのView更新
     */
    private fun updateViewTag() {
        tableLayoutTag.removeAllViews()
        if (condTags.size > 0) {
            // タグの表示
            tableLayoutTag.visibility = View.VISIBLE
            textViewTagHint.visibility = View.GONE
            tableLayoutTag.also {
                val rowSize: Int = ceil(condTags.size / 2.0).toInt()
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
                        if ((i * 2) + 1 < condTags.size) {
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
        } else {
            tableLayoutTag.visibility = View.GONE
            textViewTagHint.visibility = View.VISIBLE
        }

    }

    /**
     * TAGのView作成
     */
    private fun createViewTag(index: Int): TextView {
        val item = condTags[index]
        val textView = TextView(this)
        textView.also {
            it.text = item.name
            it.setBackgroundResource(R.drawable.txt_review_tag)
            it.setTextColor(ContextCompat.getColorStateList(this, R.color.txt_review_tag_color))
            it.textAlignment = View.TEXT_ALIGNMENT_CENTER
            it.gravity = Gravity.CENTER_VERTICAL
            it.isSelected = true
        }
        return textView
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
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                backPage()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
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

    /** クチコミ投稿の処理 **/
    /**
     * 画像の投稿があるかのチェック
     */
    private fun isImages(): Boolean {
        for (i in 0..imageListData.size - 1) {
            if (imageListData[i] != null) {
                return true
            }
        }
        return false
    }
    /**
     * クチコミ投稿をクリック
     */
    private fun onClickPost() {

        val isVisitDate = condVisitDate[0] != 0
        val isReview = editTextReview.length() > 0
        var isImage = isImages()

        var enable = true
        when (reviewType) {
            1 -> {
                // すべて
                if (!isVisitDate || (!isReview && !isImage)) {
                    enable = false
                }
            }
            2 -> {
                // 写真のみ
                if (!isVisitDate || !isImage) {
                    enable = false
                }
            }
        }

        if (enable) {
            // 投稿へ
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_POST_CONF,
                title = "クチコミを投稿しますか？",
                msg = null,
                positiveLabel = "投稿",
                negativeLabel = "キャンセル"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        } else {
            // アラート
            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "正しく入力されていない項目があります",
                msg = "・訪問日を選択してください\n・クチコミまたは写真を投稿してください",
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
    }

    /**
     * 入力データのチェック(投稿のみ)
     */
    private fun checkInputData() {
        loading.updateLayout("入力の確認をしています...", true)

        val url = MyString().my_http_url_app() + "/review/error_check_input.php"
        val params = listOf(
            "comingFlg" to getVisitDate(),
            "review" to editTextReview.text.toString(),
            "image_name" to "")

        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    condReview = datas.getString("review")
                    if (isImages()) {
                        // 画像を先にアップロード
                        uploadImage()
                    } else {
                        // 画像なし投稿
                        insertReviewData()
                    }
                } else {
                    // 入力エラーアラート
                    val errors = json.obj().get("errors") as JSONObject
                    showAlertSetverError(errors)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                showAlertSystemError()
            })
        }
    }

    /**
     * サーバーエラーのメッセージ
     */
    private fun showAlertSetverError(errors: JSONObject) {
        loading.onDismiss()
        ifNotNull(errors.getString("msg")){
            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = null,
                msg = it,
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
    }

    /**
     * 通信エラー
     */
    private fun showAlertSystemError(isDraft: Boolean = false, isDelete: Boolean = false) {
        loading.onDismiss()
        var title = if (isDraft) "下書きを保存できませんでした。" else "クチコミ投稿できませんでした。"
        if (isDelete) {
            title = "下書きを削除できませんでした"
        }

        val alert = AlertNormal.newInstance(
            requestCode = 0,
            title = title,
            msg = getString(R.string.alert_network_error),
            positiveLabel = "OK",
            negativeLabel = null
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    /**
     * 写真のアップロード(isDraft: 下書き投稿)
     */
    private fun uploadImage(isDraft: Boolean = false) {

        loading.updateLayout("写真をアップロードしています...", true)

        val url = MyString().my_http_url_app() + "/review/upload_image.php"

        val files: ArrayList<File> = arrayListOf()
        for (i in 0..imageListData.count() - 1) {
            if (imageListData[i] != null) {
                files.add(File(imageListData[i]!!.path!!))
            }
        }

        url.httpUpload(Method.POST)
            .sources { request, url ->
                files
            }
            .name {
                // $FILES is [ only: image or multi: image1, image2 ...]
                if (files.count() == 1) "image1" else "image"
            }
            .progress { readBytes, totalBytes ->
                // 送信状況を表示
                runOnUiThread {
                    if (readBytes > 0L && totalBytes > 0L) {
                        loading.updateLayout("写真をアップロードしています\n%d%%...",
                            readBytes.toDouble(), totalBytes.toDouble())
                    }
                }
            }
            .responseJson { request, response, result ->
                result.fold(success = { json ->
                    Log.i(">> $TAG", "httpUpload succsess" )

                    val datas = json.obj().get("datas") as JSONObject
                    if (datas.get("result") as Boolean) {
                        val json_array = datas.getJSONArray("image_names")
                        val image_names: ArrayList<String> = ArrayList()
                        for (i in 0..json_array.length() - 1) {
                            image_names.add(json_array[i].toString())
                        }
                        if (isDraft) {
                            // 下書きを保存する
                            insertDraftData(image_names)
                        } else {
                            // クチコミを登録する
                            insertReviewData(image_names)
                        }
                    } else {
                        // エラーメッセージ
                        val errors = json.obj().get("errors") as JSONObject
                        showAlertSetverError(errors)
                    }
                }, failure = { error ->
                    // 通信エラー
                    Log.e(TAG, error.toString())
                    showAlertSystemError(isDraft)
                })
            }
    }

    private fun getVisitDate(): String {
        return "%d-%d-%d".format(
            condVisitDate[0], condVisitDate[1], condVisitDate[2])
    }

    /**
     * クチコミ・下書きの投稿パラメーター
     */
    private fun getInsertParams(imageNames: ArrayList<String>?): List<Pair<String, Any?>>? {
        val params = arrayListOf(
            "spot_id" to dataSpot.id,
            "comingFlg" to getVisitDate())

        // クチコミ
        var review = condReview
        if (review.isEmpty()) {
            review = editTextReview.text.toString()
        }
        params.add("review" to review)

        // ログインID
        if (mySP!!.get_status_login()) {
            val db = DBHelper(mContext!!)
            try {
                val tableUsers = DBTableUsers(mContext!!)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        //　下書きID
        if (isDraft && dataDraftReview!!.id > 0) {
            params.add("draft_id" to dataDraftReview!!.id)
        }

        // 写真
        ifNotNull(imageNames, {
            for (i in 0..it.size - 1) {
                params.add("image_names[$i]" to it[i])
            }
        })

        // タグ
        if (condTags.size > 0) {
            for (i in 0..condTags.size - 1) {
                params.add("tag_ids[]" to condTags[i].id)
            }
        }
        return params
    }

    /**
     * クチコミ投稿
     */
    private fun insertReviewData(imageNames: ArrayList<String>? = null) {
        loading.onDismiss()

        val url = MyString().my_http_url_app() + "/review/insert_review.php"
        val params = getInsertParams(imageNames)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    if (datas.get("badge_flag") as Boolean) {
                        // 称号を取得
                        var badge_array: ArrayList<DataBadge> = arrayListOf()
                        val json_array = datas.getJSONArray("badge")
                        for (i in 0..json_array.length() - 1) {
                            val obj = json_array.getJSONObject(i)
                            badge_array.add(
                                DataBadge(
                                0,
                                obj.getString("name"),
                                obj.getString("image"),
                                obj.getString("description"),
                                true
                            ))
                        }

                        // 称号を表示
                        val modal = ModalGetBadge.newInstance(badge_array)
                        modal.show(supportFragmentManager, ModalGetBadge.TAG)
                    } else {
                        // 完了メッセージ
                        val alert = AlertNormal.newInstance(
                            requestCode = REQUEST_ALERT_ACTION_FINISH,
                            title = "クチコミ投稿を完了しました！",
                            msg = "※投稿されたクチコミはすぐに掲載されません",
                            positiveLabel = "OK",
                            negativeLabel = null
                        )
                        alert.show(supportFragmentManager, AlertNormal.TAG)
                    }
                } else {
                    val errors = json.obj().get("errors") as JSONObject
                    showAlertSetverError(errors)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                showAlertSystemError()
            })
        }
    }

    /**
     * 下書きを保存する
     */
    private fun insertDraftData(imageNames: ArrayList<String>? = null) {
        loading.onDismiss()

        val url = MyString().my_http_url_app() + "/review/v2/insert_draft_review.php"
        val params = getInsertParams(imageNames)

        Log.i(">> $TAG", "insertDraftData params")
        println(params)

        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                Log.i(">> $TAG", "insertDraftData json")
                println(json)
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    // 完了メッセージ
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_ACTION_FINISH,
                        title = "下書きを保存しました",
                        msg = null,
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                } else {
                    val errors = json.obj().get("errors") as JSONObject
                    showAlertSetverError(errors)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                showAlertSystemError(true)
            })
        }
    }

    /**
     * 下書きを削除
     */
    private fun deleteDraft() {
        val url = MyString().my_http_url_app() + "/review/v2/delete_draft_review.php"
        val params = arrayListOf(
            "id" to dataDraftReview!!.id
        )
        // ログインID
        if (mySP!!.get_status_login()) {
            val db = DBHelper(mContext!!)
            try {
                val tableUsers = DBTableUsers(mContext!!)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    params.add("user_id" to it.user_id)
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        }

        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                println(json)
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    // 完了メッセージ
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_ACTION_FINISH,
                        title = "下書きを削除しました",
                        msg = null,
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                } else {
                    val errors = json.obj().get("errors") as JSONObject
                    showAlertSetverError(errors)
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
                showAlertSystemError(true , true)
            })
        }
    }

}
