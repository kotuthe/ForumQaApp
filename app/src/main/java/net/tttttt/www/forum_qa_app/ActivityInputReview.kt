package net.tttttt.www.forum_qa_app

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
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
import net.tttttt.www.forum_qa_app.databinding.ActivityInputReviewBinding
import net.tttttt.www.forum_qa_app.databinding.ActivityMainBinding
import net.tttttt.www.forum_qa_app.entities.DataBadge
import net.tttttt.www.forum_qa_app.entities.DataMyDraftReview
import net.tttttt.www.forum_qa_app.entities.DataReviewTag
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.network.TaskDownloadImage
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableUsers
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.*
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class ActivityInputReview :
    AppCompatActivity(),
    AlertNormal.OnSimpleDialogClickListener,
    ModalGetBadge.OnModalGetBadgeClickListener {

    companion object {
        val TAG = "ActivityInputReview"
    }
    // ?????? //
    private lateinit var binding: ActivityInputReviewBinding
    private val REQUEST_SELECT_IMAGE_PICKER: Int = 1
    private val REQUEST_SELECT_IMAGE_PICKER_IRREGULAR: Int = 101
    private val REQUEST_PERMISSION_STORAGE: Int = 102
    private val REQUEST_ALERT_IMAGE_DELETE: Int = 2
    private val REQUEST_SELECT_TAG: Int = 3
    private val REQUEST_ALERT_POST_CONF: Int = 4 // ????????????
    private val REQUEST_ALERT_SAVE_CONF: Int = 5 // ???????????????
    private val REQUEST_ALERT_DELETE_CONF: Int = 6 // ???????????????
    private val REQUEST_ALERT_ACTION_FINISH: Int = 7 // ???????????????????????????????????????
    private val REQUEST_ALERT_BACK_PAGE: Int = 8 // ???????????????
    private val REQUEST_ALERT_TOCHIGI_ALE: Int = 9 // ?????????????????????

    private val MAX_SELECT_IMAGE: Int = 10

    // UI //
    private lateinit var loading: LoadingNormal

    // ????????? //
    private var mContext: Context? = null
    private var mySP: MySharedPreferences? = null
    private lateinit var dataSpot: DataSpotInfo
    private var isDraft: Boolean = false
    private var dataDraftReview: DataMyDraftReview? = null
    private var reviewType: Int = 1 // 1: All, 2: Image Only
    private var imageAdapter: RecyclerInputReviewAdapter? = null
    private var imageListData: ArrayList<Uri?> = arrayListOf(null) // ?????????null??????????????????
    private var deleteItemIndex: Int = 0
    private var isSelectImageIrregular: Boolean = false

    // Web??????????????????
    private var taskDLImage: TaskDownloadImage? = null
    private var dlImageIndex: Int = 0
    private var dirImageList: ArrayList<Uri> = arrayListOf() // ?????????????????????URI

    // ???????????????
    private var condVisitDate: Array<Int> = Array(3) {0}
    private var condTags: ArrayList<DataReviewTag> = ArrayList()
    private var condReview: String = "" // ???????????????????????????????????????

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_input_review)
        binding = ActivityInputReviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        isSelectImageIrregular = Build.VERSION.SDK_INT >= 29

        mContext = applicationContext
        mySP = MySharedPreferences(mContext!!)
        loading = LoadingNormal.newInstance(
            message = "", isProgress = true)

        ifNotNull(intent.getBooleanExtra("isDraft", false), {
            isDraft = it
            if (!isDraft) {
                // ??????
                dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
            } else {
                // ?????????
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
            supportActionBar!!.title = "??????????????????"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.textViewSpotName.text = dataSpot.name

        initLayout()
        requestStoragePermission()
    }

    override fun onDestroy() {
        super.onDestroy()

        // ????????????????????????????????????
        // ???????????????????????????????????????????????????????????????
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
     * home?????????back key???????????????
     */
    private fun backPage() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_BACK_PAGE,
            title = "????????????????????????????????????????????????????????????????????????????????????",
            msg = null,
            positiveLabel = "??????",
            negativeLabel = "???????????????"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_STORAGE -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // ??????????????????
                    // ??????????????????????????????????????????
                    val alert = AlertNormal.newInstance(
                        requestCode = 0,
                        title = "???????????????????????????????????????????????????",
                        msg = "????????????????????????????????????????????????",
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)

                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (isSelectImageIrregular && Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_STORAGE)
            }
        }
    }

    /**
     * Web???????????????????????????
     */
    private fun downloadImage() {
        if (dlImageIndex < dataDraftReview!!.reviewImageUrls.size) {
            runOnUiThread {
                loading.updateLayout("????????????????????????\n%d%%...",
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
     * Web???????????????????????????Listener
     */
    private fun downloadImageListener(): TaskDownloadImage.Listener? {
        return object : TaskDownloadImage.Listener {

            override fun onSuccess(uri: Uri?) {
                if (uri != null && uri.path != null) {
                    dirImageList.add(uri)
                    downloadImage()
                } else {
                    // URL???????????????????????????????????????????????????
                    // ???????????????????????????
                    runOnUiThread {
                        loading.onDismiss()
                    }
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    private fun addImageListData(uris: ArrayList<Uri>) {
        for (i in 0..uris.size - 1) {
            imageListData[imageListData.size - 1] = uris[i]
            if (imageListData.size < MAX_SELECT_IMAGE) {
                // 9?????????????????????????????????ADD?????????
                imageListData.add(null)
            }
        }
        imageAdapter!!.notifyDataSetChanged()
    }

    /**
     * ??????????????????????????????
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
                // ??????????????????
                if (resultCode == Activity.RESULT_OK) {
                    val uris: List<Uri> = GalleryActivity.getSelection(data)
                    if (uris.count() > 0) {
                        addImageListData(ArrayList(uris))
                    }
                }
            }
            REQUEST_SELECT_IMAGE_PICKER_IRREGULAR -> {
                // 1?????????
                try {
                    data!!.data.also { uri ->
                        ifNotNull(uri, {
                            addImageListData(ArrayList(arrayListOf(it)))
                        })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
            REQUEST_SELECT_TAG -> {
                // ???????????????
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
     * ??????????????????????????????
     */
    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_IMAGE_DELETE -> {
                removeImageListData(deleteItemIndex)
            }
            REQUEST_ALERT_POST_CONF -> {
                // ?????????(?????????????????????)
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                checkInputData()
            }
            REQUEST_ALERT_SAVE_CONF -> {
                // ????????????(??????????????????????????????)
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                uploadImage(true)
            }
            REQUEST_ALERT_DELETE_CONF -> {
                // ??????????????????
                loading.show(supportFragmentManager, LoadingNormal.TAG)
                deleteDraft()
            }
            REQUEST_ALERT_ACTION_FINISH -> {
                // ?????????????????????????????????
                val intent = Intent()
                setResult(RESULT_OK, intent);
                finish()
            }
            REQUEST_ALERT_BACK_PAGE -> {
                // ????????????????????????
                finish()
            }
            REQUEST_ALERT_TOCHIGI_ALE -> {
                // ????????????WEB
                startActivity(
                    MyIntent().web_browser(MyString().my_http_url_tochigi_ale()))
            }

        }
    }

    /**
     * ??????????????????????????????
     */
    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    /**
     * ???????????????????????????
     */
    override fun onModalGetBadgeCloseClick() {
        finish()
    }

    fun initLayout() {

        // ????????????????????????????????????
        if (isDraft) {
            ifNotNull(dataDraftReview) {
                // ?????????
                if (!it.comingFlg.isEmpty()) {
                    condVisitDate = arrayOf(
                        it.comingFlg[0], it.comingFlg[1], it.comingFlg[2])
                }

                // ????????????
                if (!it.review.isEmpty()) {
                    binding.editTextReview.setText(it.review)
                }

                // ??????
                if (it.reviewImageUrls.size > 0) {
                    loading.show(supportFragmentManager, LoadingNormal.TAG)
                    downloadImage()
                }

                // ??????
                if (it.tagIds.size > 0) {
                    for (i in 0..it.tagIds.size - 1) {
                        condTags.add(it.tagIds[i])
                    }
                }
            }
        }

        // ?????????
        setVisitDateTitle()
        binding.layoutVisitValue.setOnClickListener {
            showCalendar()
        }

        // ????????????
        binding.layoutReview.visibility =
            if (reviewType == 1) View.VISIBLE else View.GONE
        binding.buttonReviewClose.alpha = 0f
        binding.editTextReview.apply {
            setOnFocusChangeListener { view, hasFocus ->
                // buttonReviewClose.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
                binding.buttonReviewClose.alpha = if (hasFocus) 1f else 0f
                if (!isDraft) {
                    binding.layoutNormalButtons.visibility = if (hasFocus) View.GONE else View.VISIBLE
                } else {
                    binding.layoutDraftButtons.visibility = if (hasFocus) View.GONE else View.VISIBLE
                }
            }
        }

        binding.buttonReviewClose.setOnClickListener {
            hideKeyboard()
        }

        // ?????? //
        // ?????????????????????
        binding.buttonTochigiAle.setOnClickListener {
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_TOCHIGI_ALE,
                title = null,
                msg = "????????????????????????????????????????????????????????????????????????\n??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????",
                positiveLabel = "????????????",
                negativeLabel = "???????????????"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }

        imageAdapter = RecyclerInputReviewAdapter(mContext!!, imageListData)
        binding.rcvImage.apply {
            setHasFixedSize(true)

            val lmg = GridLayoutManager(context, imageAdapter!!.spanCount)
            lmg.spanSizeLookup = imageAdapter!!.spanSizeLookup
            layoutManager = lmg

            addItemDecoration(imageAdapter!!.mItemDecoration)
            adapter = imageAdapter
        }

        // ????????????????????????????????????
        if (!dataSpot.imageEnable) {
            binding.rcvImage.visibility = View.GONE
            binding.textViewImageHint.text = "???%s????????????????????????????????????????????????????????????????????????????????????????????????".format(dataSpot.name)
        } else {
            binding.textViewImageHint.text = getString(R.string.input_review_image_hint).format(MAX_SELECT_IMAGE)
        }

        // ???????????????????????????
        val touchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN
                        or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                0
            ) {
                override fun isItemViewSwipeEnabled(): Boolean {
                    return false
                }

                // ?????????????????????????????? //
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)

                    ifNotNull(viewHolder, {
                        val from = it.adapterPosition
                        if (imageAdapter!!.items[from] == null) {
                            // ??????????????????????????????
                            clearView(binding.rcvImage, it)
                        } else {
                            // ??????
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            val pattern = longArrayOf(10)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                            } else {
                                vibrator.vibrate(pattern, -1)
                            }
                            // ?????????????????????
                            val moveSet = AnimatorSet()
                            val moveX = ObjectAnimator.ofFloat(it.itemView, "translationX", 10f)
                            val moveY = ObjectAnimator.ofFloat(it.itemView, "translationY", -10f)

                            moveX.duration = 100
                            moveY.duration = 40

                            moveSet.playTogether(moveX, moveY)
                            moveSet.start()

                            // ????????????
                            val toast = Toast.makeText(applicationContext, "??????????????????", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.TOP, 0, 100f.convertDpToPx(mContext!!).toInt())
                            toast.show()
                        }
                    })
                }

                // ?????? //
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

                // ???????????????????????? //
                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    imageAdapter!!.notifyDataSetChanged()
                }
            })

        touchHelper.attachToRecyclerView(binding.rcvImage)

        // ?????????????????????
        imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            val index = view.id

            when (view.tag) {
                imageAdapter!!.TAG_VIEW_IMAGE -> {
                    // ???????????????
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
                    // ???????????????
                    deleteItemIndex = index
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_IMAGE_DELETE,
                        title = "??????????????????????????????",
                        msg = null,
                        positiveLabel = "??????",
                        negativeLabel = "???????????????"
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                }
                imageAdapter!!.TAG_VIEW_ADD -> {
                    // ???????????????
                    // ??????????????????android10???Louvre??????????????????????????????GligarPicker???????????????????????????
                    if (isSelectImageIrregular) { // isSelectImageIrregular
                        // Android 9?????????1?????????
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        }
                        startActivityForResult(intent, REQUEST_SELECT_IMAGE_PICKER_IRREGULAR)


                    } else {
                        Louvre.init(this)
                            .setRequestCode(REQUEST_SELECT_IMAGE_PICKER)
                            .setMaxSelection(MAX_SELECT_IMAGE - (imageListData.size - 1))
                            .setSelection(listOf<Uri>())
                            .setMediaTypeFilter(Louvre.IMAGE_TYPE_JPEG, Louvre.IMAGE_TYPE_PNG, Louvre.IMAGE_TYPE_BMP)
                            .open()
                    }

                }
            }
        })

        // ???????????????
        updateViewTag()
        binding.layoutTag.setOnClickListener {
            // ???????????????
            val intent = Intent(this, ActivityInputReviewTag::class.java)
            intent.putExtra("id", dataSpot.id)
            intent.putExtra("selectTags", condTags)
            startActivityForResult(intent, REQUEST_SELECT_TAG)
        }



        val deleteListener: View.OnClickListener = View.OnClickListener {
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_DELETE_CONF,
                title = "????????????????????????????????????",
                msg = null,
                positiveLabel = "??????",
                negativeLabel = "???????????????"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
        val draftListener: View.OnClickListener = View.OnClickListener {
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_SAVE_CONF,
                title = "???????????????????????????????????????",
                msg = null,
                positiveLabel = "?????????",
                negativeLabel = "???????????????"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
        val saveListener: View.OnClickListener = View.OnClickListener {
            onClickPost()
        }

        var height = 0f
        if (!isDraft) {
            binding.layoutNormalButtons.visibility = View.VISIBLE
            binding.layoutDraftButtons.visibility = View.GONE
            height = 80f

            // ?????????
            binding.buttonSave.setOnClickListener(draftListener)
            // ??????
            binding.buttonPost.setOnClickListener(saveListener)
        } else {
            binding.layoutNormalButtons.visibility = View.GONE
            binding.layoutDraftButtons.visibility = View.VISIBLE
            height = 134f

            // ??????????????????
            binding.buttonDraftDelete.setOnClickListener(deleteListener)
            // ?????????
            binding.buttonDraftSave.setOnClickListener(draftListener)
            // ??????
            binding.buttonDraftPost.setOnClickListener(saveListener)
        }

        // ???????????????????????????
        val lpm = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height.convertDpToPx(mContext!!).toInt()
        )
        binding.layoutFooter.layoutParams = lpm
    }

    /**
     * ?????????View??????
     */
    private fun updateViewTag() {
        binding.tableLayoutTag.removeAllViews()
        if (condTags.size > 0) {
            // ???????????????
            binding.tableLayoutTag.visibility = View.VISIBLE
            binding.textViewTagHint.visibility = View.GONE
            binding.tableLayoutTag.also {
                val rowSize: Int = ceil(condTags.size / 2.0).toInt()
                for (i in 0..rowSize - 1) {
                    val tr = TableRow(this).also {
                        val lp = TableRow.LayoutParams(
                            0, 40f.convertDpToPx(this).toInt(), 1f)

                        lp.setMargins(
                            4f.convertDpToPx(this).toInt())

                        // 1??????
                        val text1 = createViewTag(i * 2)
                        text1.layoutParams = lp
                        it.addView(text1)

                        // 2??????
                        if ((i * 2) + 1 < condTags.size) {
                            val text2 = createViewTag((i * 2) + 1)
                            text2.layoutParams = lp
                            it.addView(text2)
                        } else {
                            // ?????????View????????????
                            val view = View(this)
                            view.layoutParams = lp
                            it.addView(view)
                        }

                    }
                    it.addView(tr)
                }
            }
        } else {
            binding.tableLayoutTag.visibility = View.GONE
            binding.textViewTagHint.visibility = View.VISIBLE
        }

    }

    /**
     * TAG???View??????
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
     * ????????????????????????
     */
    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        binding.layoutContent.requestFocus()
    }

    // ??????????????????????????????????????????????????????
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                backPage()
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * ??????????????????
     */
    private fun setVisitDateTitle() {
        var title = "????????????????????????"
        if (condVisitDate[0] != 0) {
            title = "%d??? %d??? %d???".format(condVisitDate[0], condVisitDate[1], condVisitDate[2])
        }
        binding.textViewVisitDate.text = title
    }

    /**
     * ????????????????????????
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
            .setTitle("?????????")
            .setPositiveButton("??????", DialogInterface.OnClickListener { dialog, which ->
                val y = datePicker.year
                val m = datePicker.month + 1
                val d = datePicker.dayOfMonth
                // Log.i(">> $TAG", "%d-%d-%d".format(y, m ,d))
                condVisitDate = arrayOf(y, m, d)
                setVisitDateTitle()
            })
            .setNegativeButton("???????????????", null)
            .show()
    }

    /** ??????????????????????????? **/
    /**
     * ??????????????????????????????????????????
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
     * ?????????????????????????????????
     */
    private fun onClickPost() {

        val isVisitDate = condVisitDate[0] != 0
        val isReview = binding.editTextReview.length() > 0
        val isImage = isImages()

        var enable = true
        when (reviewType) {
            1 -> {
                // ?????????
                if (!isVisitDate || (!isReview && !isImage)) {
                    enable = false
                }
            }
            2 -> {
                // ????????????
                if (!isVisitDate || !isImage) {
                    enable = false
                }
            }
        }

        if (enable) {
            // ?????????
            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_ALERT_POST_CONF,
                title = "????????????????????????????????????",
                msg = null,
                positiveLabel = "??????",
                negativeLabel = "???????????????"
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        } else {
            // ????????????
            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "??????????????????????????????????????????????????????",
                msg = "???????????????????????????????????????\n?????????????????????????????????????????????????????????",
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)
        }
    }

    /**
     * ??????????????????????????????(????????????)
     */
    private fun checkInputData() {
        loading.updateLayout("?????????????????????????????????...", true)

        val url = MyString().my_http_url_app() + "/review/error_check_input.php"
        val params = listOf(
            "comingFlg" to getVisitDate(),
            "review" to binding.editTextReview.text.toString(),
            "image_name" to "")

        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    condReview = datas.getString("review")
                    if (isImages()) {
                        // ?????????????????????????????????
                        uploadImage()
                    } else {
                        // ??????????????????
                        insertReviewData()
                    }
                } else {
                    // ???????????????????????????
                    val errors = json.obj().get("errors") as JSONObject
                    showAlertSetverError(errors)
                }
            }, failure = { error ->
                // ???????????????
                Log.e(TAG, error.toString())
                showAlertSystemError()
            })
        }
    }

    /**
     * ???????????????????????????????????????
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
     * ???????????????
     */
    private fun showAlertSystemError(isDraft: Boolean = false, isDelete: Boolean = false) {
        loading.onDismiss()
        var title = if (isDraft) "?????????????????????????????????????????????" else "?????????????????????????????????????????????"
        if (isDelete) {
            title = "??????????????????????????????????????????"
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

    // Uri??????????????????????????? ??? AndroidQ?????????????????????
    private fun convertUriToFile(uri: Uri): File? {
        var path = ""
        if (DocumentsContract.isDocumentUri(mContext!!, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {
                // ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    // path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    path = "%s/%s".format(Environment.getExternalStorageDirectory().toString(), split[1])
                } else {
                    // path = "/stroage/" + type + "/" + split[1]
                    path = "/stroage/%s/%s".format(type, split[1])
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                // DownloadsProvider
                var fileName = ""
                var c: Cursor? = null
                try {
                    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                    c = mContext!!.contentResolver.query(uri, projection, null, null, null)
                    if (c != null && c.moveToFirst()) {
                        fileName = c.getString(
                            c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    }
                } finally {
                    c?.close()
                }
                path = "%s/%s".format(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), fileName)
            } else if ("com.android.providers.media.documents" == uri.authority) {
                // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val contentUri: Uri? = MediaStore.Files.getContentUri("external")
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                var c: Cursor? = null
                val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
                try {
                    c = mContext!!.contentResolver.query(
                        contentUri!!, projection, selection, selectionArgs, null
                    )
                    if (c != null && c.moveToFirst()) {
                        val cindex = c.getColumnIndexOrThrow(projection[0])
                        path = c.getString(cindex)
                    }
                } finally {
                    c?.close()
                }
            }
        } else {
            try {
                path = uri.path!!
            } catch (e: Exception) {}
        }

        if (path.isEmpty()) {
            return null
        }

        return File(path)
    }

    /**
     * ???????????????????????????(isDraft: ???????????????)
     */
    private fun uploadImage(isDraft: Boolean = false) {

        val files: ArrayList<File> = arrayListOf()
        for (i in 0..imageListData.count() - 1) {
            if (imageListData[i] != null) {
                val uri: Uri = imageListData[i]!!
                try {
                    if (isSelectImageIrregular) {
                        files.add(convertUriToFile(uri)!!)
                    } else {
                        files.add(File(uri.path!!))
                    }
                } catch (e: Exception) {
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_TOCHIGI_ALE,
                        title = "???????????????????????????????????????????????????",
                        msg = "????????????????????????????????????????????????",
                        positiveLabel = "OK",
                        negativeLabel = null
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)
                    return
                }
            }
        }

        loading.updateLayout("??????????????????????????????????????????...", true)
        val url = MyString().my_http_url_app() + "/review/upload_image.php"

        url.httpUpload(Method.POST)
            .sources { request, url ->
                files
            }
            .name {
                // $FILES is [ only: image or multi: image1, image2 ...]
                if (files.count() == 1) "image1" else "image"
            }
            .progress { readBytes, totalBytes ->
                // ?????????????????????
                runOnUiThread {
                    if (readBytes > 0L && totalBytes > 0L) {
                        loading.updateLayout("??????????????????????????????????????????\n%d%%...",
                            readBytes.toDouble(), totalBytes.toDouble())
                    }
                }
            }
            .responseJson { request, response, result ->
                result.fold(success = { json ->

                    val datas = json.obj().get("datas") as JSONObject
                    if (datas.get("result") as Boolean) {
                        val json_array = datas.getJSONArray("image_names")
                        val image_names: ArrayList<String> = ArrayList()
                        for (i in 0..json_array.length() - 1) {
                            image_names.add(json_array[i].toString())
                        }
                        if (isDraft) {
                            // ????????????????????????
                            insertDraftData(image_names)
                        } else {
                            // ???????????????????????????
                            insertReviewData(image_names)
                        }
                    } else {
                        // ????????????????????????
                        val errors = json.obj().get("errors") as JSONObject
                        showAlertSetverError(errors)
                    }
                }, failure = { error ->
                    // ???????????????
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
     * ???????????????????????????????????????????????????
     */
    private fun getInsertParams(imageNames: ArrayList<String>?): List<Pair<String, Any?>>? {
        val params = arrayListOf(
            "spot_id" to dataSpot.id,
            "comingFlg" to getVisitDate())

        // ????????????
        var review = condReview
        if (review.isEmpty()) {
            review = binding.editTextReview.text.toString()
        }
        params.add("review" to review)

        // ????????????ID
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

        //????????????ID
        if (isDraft && dataDraftReview!!.id > 0) {
            params.add("draft_id" to dataDraftReview!!.id)
        }

        // ??????
        ifNotNull(imageNames, {
            for (i in 0..it.size - 1) {
                params.add("image_names[$i]" to it[i])
            }
        })

        // ??????
        if (condTags.size > 0) {
            for (i in 0..condTags.size - 1) {
                params.add("tag_ids[]" to condTags[i].id)
            }
        }
        return params
    }

    /**
     * ??????????????????
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
                        // ???????????????
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

                        // ???????????????
                        val modal = ModalGetBadge.newInstance(badge_array)
                        modal.show(supportFragmentManager, ModalGetBadge.TAG)
                    } else {
                        // ?????????????????????
                        val alert = AlertNormal.newInstance(
                            requestCode = REQUEST_ALERT_ACTION_FINISH,
                            title = "??????????????????????????????????????????",
                            msg = "???????????????????????????????????????????????????????????????",
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
                // ???????????????
                Log.e(TAG, error.toString())
                showAlertSystemError()
            })
        }
    }

    /**
     * ????????????????????????
     */
    private fun insertDraftData(imageNames: ArrayList<String>? = null) {
        loading.onDismiss()

        val url = MyString().my_http_url_app() + "/review/v2/insert_draft_review.php"
        val params = getInsertParams(imageNames)

        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    // ?????????????????????
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_ACTION_FINISH,
                        title = "??????????????????????????????",
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
                // ???????????????
                Log.e(TAG, error.toString())
                showAlertSystemError(true)
            })
        }
    }

    /**
     * ??????????????????
     */
    private fun deleteDraft() {
        val url = MyString().my_http_url_app() + "/review/v2/delete_draft_review.php"
        val params = arrayListOf(
            "id" to dataDraftReview!!.id
        )
        // ????????????ID
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
                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    // ?????????????????????
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_ACTION_FINISH,
                        title = "??????????????????????????????",
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
                // ???????????????
                Log.e(TAG, error.toString())
                showAlertSystemError(true , true)
            })
        }
    }

}
