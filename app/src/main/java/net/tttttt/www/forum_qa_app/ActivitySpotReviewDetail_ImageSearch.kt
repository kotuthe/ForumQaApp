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

    // リクエスト
    private val REQUEST_ALERT_PHONE: Int = 0x1

    private lateinit var firebase: FirebaseHelper

    // データ //
    private lateinit var functions: Functions
    private lateinit var mContext: Context
    private lateinit var dataSpot: DataSpotInfo
    private lateinit var dataReview: DataSpotReview

    // ビュー
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

        // データの取得
        dataSpot = intent.getSerializableExtra("dataSpot") as DataSpotInfo
        dataReview = intent.getSerializableExtra("dataReview") as DataSpotReview

        if (supportActionBar != null) {
            supportActionBar!!.title = "写真から探す"
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
                // 電話をする
                startActivity(MyIntent().phone(dataSpot.phone))
            }
        }
    }

    override fun onSimpleDialogNegativeClick(requestCode: Int) {
    }

    private fun initLayout() {

        // 写真なしの場合
        if (dataReview.reviewImageUrls.size == 0) {
            binding.imageViewTop.visibility = View.GONE
            binding.viewSpotNameArea.setBackgroundColor(Color.WHITE)
            binding.textViewSpotName.setTextColor(Color.BLACK)
            binding.viewGalleryArea.visibility = View.GONE
        } else {
            // トップ
            // imageViewTopで切り替えアニメーション
            val task = TaskWebImageAnime(mContext)
            task.setListener(TaskWebImageAnimeListener())
            task.execute(dataReview.reviewImageUrls)

            // ギャラリーの設定
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
                // レイアウト設定 //
                setHasFixedSize(true)
                // 列
                val lmg = GridLayoutManager(context, imageAdapter!!.spanCount)
                lmg.orientation = LinearLayoutManager.HORIZONTAL
                lmg.spanSizeLookup = imageAdapter!!.spanSizeLookup
                layoutManager = lmg
                // マージン
                addItemDecoration(imageAdapter!!.mItemDecoration)
                adapter = imageAdapter
            }
            imageAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
                // ギャラリーへ
                firebase.sendEvent(
                    FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                    FirebaseHelper.eventCategory.Cell,
                    FirebaseHelper.eventAction.Tap,
                    "IS:クチコミ_写真")
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
                text = "\"ぐッ\"ときた　%d件".format(dataReview.goodNum)
            } else {
                visibility = View.GONE
            }
        }

        // 基本情報 //
        // 地図
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.layoutBasic.buttonMap.setOnClickListener {
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:地図")
            showMap()
        }
        binding.layoutBasic.buttonWebDetail.setOnClickListener {
            // WEBへ
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "IS:栃ナビ！サイトを見る")
            startActivity(
                MyIntent().web_browser(
                MyString().my_http_url_spot_info(dataSpot.id)))
        }

        // 一覧
        basicListData = HttpSpotInfo(mContext).get_is_basic_list_data(dataSpot)
        basicAdapter = ListSpotInfoBasicAdapter(mContext, basicListData)
        binding.layoutBasic.listView.apply {
            adapter = basicAdapter
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
                val item = basicListData[pos]
                when (item.type) {
                    Constants.SPOT_BASIC_INFO_TYPE.address -> {
                        // 地図へ
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:住所")
                        showMap()
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.phone -> {
                        // 電話
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:電話")
                        showPhoneAlert()
                    }
                    Constants.SPOT_BASIC_INFO_TYPE.coupon -> {
                        // クーポンへ
                        firebase.sendEvent(
                            FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                            FirebaseHelper.eventCategory.Cell,
                            FirebaseHelper.eventAction.Tap,
                            "IS:クーポン")
                        startActivity(MyIntent().web_browser(
                            MyString().my_http_url_coupon(dataSpot.id)))
                    }
                    else -> {

                    }
                }
            }
        }

        // クチコミのシェア //
        binding.layoutShare.textViewTitle.text = "クチコミをシェアする"
        binding.layoutShare.textViewCopyTitle.text = "URLをコピーする"
        binding.layoutShare.textViewCopy.text = getShareText()
        binding.layoutShare.textViewCopy.setOnClickListener {
            // クリップボードにコピー
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Cell,
                FirebaseHelper.eventAction.Tap,
                "alt:クチコミコピー")
            functions.clipboardText(this, (it as TextView).text.toString())

            val alert = AlertNormal.newInstance(
                requestCode = 0,
                title = "クチコミをコピーしました。\n貼り付けることができます。",
                msg = null,
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.show(supportFragmentManager, AlertNormal.TAG)

        }
        binding.layoutShare.buttonMail.setLeftIcon(R.drawable.img_share_mail)
        binding.layoutShare.buttonMail.setOnClickListener {
            // メールのシェア
            firebase.sendEvent(
                FirebaseHelper.screenName.IS_Spot_Info_Kuchikomi_Detail,
                FirebaseHelper.eventCategory.Button,
                FirebaseHelper.eventAction.Tap,
                "メール")
            startActivity(
                MyIntent().mail(getShareText()))
        }
        binding.layoutShare.buttonLine.setLeftIcon(R.drawable.img_share_line)
        binding.layoutShare.buttonLine.setOnClickListener {
            // ラインのシェア
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
                    msg = "クチコミをシェアするには\nLINEのインストールが必要です",
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
     * TopのImageViewにアニメーションを実装
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
     * 電話をしますかアラート
     */
    private fun showPhoneAlert() {
        val alert = AlertNormal.newInstance(
            requestCode = REQUEST_ALERT_PHONE,
            title = dataSpot.name,
            msg = "%sに電話をする".format(dataSpot.phone),
            positiveLabel = "電話をする",
            negativeLabel = "キャンセル"
        )
        alert.show(supportFragmentManager, AlertNormal.TAG)
    }

    private fun showMap() {
        // 地図へ
        val intent = Intent(this@ActivitySpotReviewDetail_ImageSearch, ActivitySpotMap::class.java)
        intent.putExtra("dataSpot", dataSpot)
        startActivity(intent)
    }

    private fun getShareText(): String {
        val url = MyString().my_http_url_spot_review_detail(dataReview.userId, dataReview.id)
        return "%s　%sさんのクチコミ\n\n%s".format(dataSpot.name, dataReview.userName, url)
    }

}
