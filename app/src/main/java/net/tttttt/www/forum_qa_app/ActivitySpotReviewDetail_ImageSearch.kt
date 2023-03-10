package net.tttttt.www.forum_qa_app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import net.tttttt.www.forum_qa_app.databinding.ActivitySpotReviewDetailImageSearchBinding
/*
import kotlinx.android.synthetic.main.activity_spot_review_detail_image_search.*
import kotlinx.android.synthetic.main.view_info_share.view.*
import kotlinx.android.synthetic.main.view_info_spot_basic.view.*
*/
import net.tttttt.www.forum_qa_app.entities.DataISReviewDetailImage
import net.tttttt.www.forum_qa_app.entities.DataSpotInfo
import net.tttttt.www.forum_qa_app.entities.DataSpotInfoBasic
import net.tttttt.www.forum_qa_app.entities.DataSpotReview
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.network.HttpSpotInfo
import net.tttttt.www.forum_qa_app.network.TaskWebImageAnime
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.ListSpotInfoBasicAdapter
import net.tttttt.www.forum_qa_app.view.RecyclerISReviewDetailImagesAdapter

class ActivitySpotReviewDetail_ImageSearch :
    AppCompatActivity(),
    OnMapReadyCallback,
    AlertNormal.OnSimpleDialogClickListener {

    companion object {
        val TAG = "ActivitySpotReviewDetail_ImageSearch"
        val TAG_SHORT = "SpotReviewDetail_IS"
    }

    private lateinit var binding: ActivitySpotReviewDetailImageSearchBinding

    // ???????????????
    private val REQUEST_ALERT_PHONE: Int = 0x1

    private lateinit var firebase: FirebaseHelper

    // ????????? //
    private lateinit var functions: Functions
    private lateinit var mContext: Context
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var dataReview: DataSpotReview

    // ?????????
    private lateinit var mGoogleMap: GoogleMap
    private var basicAdapter: BaseAdapter? = null
    private var basicListData: ArrayList<DataSpotInfoBasic> = ArrayList()
    private var imageAdapter: RecyclerISReviewDetailImagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_spot_review_detail_image_search)
        binding = ActivitySpotReviewDetailImageSearchBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mContext = applicationContext
        firebase = FirebaseHelper(mContext)
        functions = Functions(mContext)

        // ??????????????????
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        if (supportActionBar != null) {
            supportActionBar!!.title = "??????????????????"
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

    override fun onSimpleDialogPositiveClick(requestCode: Int) {
        when (requestCode) {
            REQUEST_ALERT_PHONE -> {
                // ???????????????
                startActivity(MyIntent().phone(dataSpot.phone))
            }
        }
    }

    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    private fun initLayout() {

        // ?????????????????????
        if (dataReview.reviewImageUrls.size == 0) {
            binding.imageViewTop.visibility = View.GONE
            binding.viewSpotNameArea.setBackgroundColor(Color.WHITE)
            binding.textViewSpotName.setTextColor(Color.BLACK)
            binding.viewGalleryArea.visibility = View.GONE
        } else {
            // ?????????
            // imageViewTop????????????????????????????????????
            val task = TaskWebImageAnime(mContext)
            task.setListener(TaskWebImageAnimeListener())
            task.execute(dataReview.reviewImageUrls)

            // ????????????????????????
            // recyclerView
            val items: ArrayList<DataISReviewDetailImage> = arrayListOf()
            val reviewImageSize = dataReview.reviewImageUrls.size
            for (i in 0..reviewImageSize - 1) {
                var other = 0
                if (i >= 3) {
                    break
                } else if (i >= 2) {
                    other = reviewImageSize - 3
                }
                items.add(DataISReviewDetailImage(
                    dataReview.reviewImageUrls[i],
                    other
                ))
            }

            imageAdapter = RecyclerISReviewDetailImagesAdapter(mContext, items)
            binding.recyclerView.apply {
                // ????????????????????? //
                setHasFixedSize(true)
                // ???
                val lmg = GridLayoutManager(context, imageAdapter!!.spanCount)
                lmg.orientation = LinearLayoutManager.HORIZONTAL
                lmg.spanSizeLookup = imageAdapter!!.spanSizeLookup
                layoutManager = lmg
                // ????????????
                addItemDecoration(imageAdapter!!.mItemDecoration)
                adapter = imageAdapter
            }
            imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
                // ??????????????????
                firebase.sendEvent(
                    FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "IS:????????????_??????")
                val index = view.id
                val intent = Intent(this@ActivitySpotReviewDetail_ImageSearch, ActivitySpotReviewGalleryPreview_ImageSearch::class.java)
                intent.putExtra("selectIndex", index)
                intent.putExtra("dataSpot", dataSpot)
                intent.putExtra("dataReview", dataReview)
                startActivity(intent)
            })
        }

        binding.textViewSpotName.text = dataSpot.name
        binding.imageViewUserIcon.load(dataReview.userImage) {
            placeholder(R.drawable.ic_image_placeholder)
        }
        binding.textViewUserName.text = dataReview.userName
        binding.textViewReviewDate.text = dataReview.reviewDate

        binding.textViewReview.apply {
            if (!dataReview.review.isEmpty()) {
                text = dataReview.review
            } else {
                visibility = View.GONE
            }
        }

        binding.textViewReviewInfo.apply {
            if (dataReview.goodNum > 0) {
                text = "\"??????\"????????????%d???".format(dataReview.goodNum)
            } else {
                visibility = View.GONE
            }
        }

        // ???????????? //
        // ??????
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.layoutBasic.buttonMap.setOnClickListener {
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:??????")
            showMap()
        }
        binding.layoutBasic.buttonWebDetail.setOnClickListener {
            // WEB???
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:??????????????????????????????")
            startActivity(
                MyIntent().web_browser(
                MyString().my_http_url_spot_info(dataSpot.id)))
        }

        // ??????
        basicListData = HttpSpotInfo(mContext).get_is_basic_list_data(dataSpot)
        basicAdapter = ListSpotInfoBasicAdapter(mContext, basicListData)
        binding.layoutBasic.listView.apply {
            adapter = basicAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val item = basicListData[pos]
                when (item.type) {
                    Constants.SPOT_BASIC_INFO_TYPE.address -> {
                        // ?????????
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:??????")
                        showMap()
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                        // ??????
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:??????")
                        showPhoneAlert()
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.coupon -> {
                        // ???????????????
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:????????????")
                        startActivity(MyIntent().web_browser(
                            MyString().my_http_url_coupon(dataSpot.id)))
                    }
                    else -> {

                    }
                }
            }
        }

        // ???????????????????????? //
        binding.layoutShare.textViewTitle.text = "??????????????????????????????"
        binding.layoutShare.textViewCopyTitle.text = "URL??????????????????"
        binding.layoutShare.textViewCopy.text = getShareText()
        binding.layoutShare.textViewCopy.setOnClickListener {
            // ?????????????????????????????????
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "alt:?????????????????????")
            functions.clipboardText(this, (it as TextView).text.toString())

            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "???????????????????????????????????????\n???????????????????????????????????????",
                msg = null,
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)

        }
        binding.layoutShare.buttonMail.setLeftIcon(R.drawable.img_share_mail)
        binding.layoutShare.buttonMail.setOnClickListener {
            // ?????????????????????
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "?????????")
            startActivity(
                MyIntent().mail(getShareText()))
        }
        binding.layoutShare.buttonLine.setLeftIcon(R.drawable.img_share_line)
        binding.layoutShare.buttonLine.setOnClickListener {
            // ?????????????????????
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "LINE")
            MyIntent().line(getShareText(), {
                startActivity(it)
            }, {
                val alert = AlertNormal.newInstance(
                    requestCode = 0,
                    title = null,
                    msg = "????????????????????????????????????\nLINE????????????????????????????????????",
                    positiveLabel = "OK",
                    negativeLabel = null
                )
                alert.show(supportFragmentManager, AlertNormal.TAG)
            })
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        // HttpSpotInfo(mContext).init_map_position(googleMap)

        HttpSpotInfo(mContext).update_map_position(
            mGoogleMap,
            LatLng(dataSpot.latitude, dataSpot.longitude),
            dataSpot.category_large_id
        )
    }

    /**
     * Top???ImageView?????????????????????????????????
     */
    private fun TaskWebImageAnimeListener(): TaskWebImageAnime.Listener? {
        return object : TaskWebImageAnime.Listener {
            override fun onSuccess(anime: AnimationDrawable?) {
                binding.imageViewTop.setImageDrawable(anime!!)
                anime.start()
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private fun showPhoneAlert() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PHONE,
            title = dataSpot.name,
            msg = "%s??????????????????".format(dataSpot.phone),
            positiveLabel = "???????????????",
            negativeLabel = "???????????????"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    private fun showMap() {
        // ?????????
        val intent = Intent(this@ActivitySpotReviewDetail_ImageSearch, ActivitySpotMap::class.java)
        intent.putExtra("dataSpot", dataSpot)
        startActivity(intent)
    }

    private fun getShareText(): String {
        val url = MyString().my_http_url_spot_review_detail(dataReview.userId, dataReview.id)
        return "%s???%s?????????????????????\n\n%s".format(dataSpot.name, dataReview.userName, url)
    }

}
