package net.tttttt.www.forum_qa_app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import net.tttttt.www.forum_qa_app.databinding.FragmentTopBkBinding
import net.tttttt.www.forum_qa_app.entities.DataCategory1
import net.tttttt.www.forum_qa_app.entities.DataSpotList
import net.tttttt.www.forum_qa_app.entities.DataTopSelection
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableAppData
import net.tttttt.www.forum_qa_app.storage.DBTableArea2
import net.tttttt.www.forum_qa_app.storage.DBTableCategory1
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

// 静的画像のモーダルを設定認め、クチコミ画面でそれを使う

class FragmentTopBk : Fragment() {
    companion object {
        val TAG = "Top"
    }

    private lateinit var binding: FragmentTopBkBinding

    // リクエスト
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_CODE_ALERT_LOCATION: Int = 0x2
    private val REQUEST_APP_DATA_UPDATE: Int = 0x3

    // カラー
    private val COLOR_CATEGORY_ALL = Color.parseColor("#555555")
    private val COLOR_DIS_CATEGORY_ALL = Color.parseColor("#CCCCCC")

    private lateinit var firebase: FirebaseHelper
    private var mySP: MySharedPreferences? = null
    private var functions: Functions? = null

    // UI //
    private var loading: LoadingNormal? = null

    // 変数 //
    private var mLocationClient: FusedLocationProviderClient? = null
    private var mLocation: Location? = null
    private var mLocationCallback: LocationCallback? = null
    // 検索条件
    private var condPage: Int = 1
    private var condCategory: Int = 0
    private var condArea: Int = 0
    private var condSort: Int = 1
    // 絞り込み条件
    private var dataCategoryArray: ArrayList<DataCategory1> = arrayListOf(
        DataCategory1(0, "すべて", "")
    )
    private val dataSelectionAreas: ArrayList<DataTopSelection> = arrayListOf(
        DataTopSelection(0, "周辺", true)
    )
    private val dataSelectionSorts: ArrayList<DataTopSelection> =
        arrayListOf(
                DataTopSelection(1, "写真が多い順", true),
                DataTopSelection(2, "クチコミが多い順", false),
                DataTopSelection(3, "グッときたが多い順", false),
                DataTopSelection(4, "新着店舗順", false)
        )

    private var viewMenuItems: ArrayList<RelativeLayout> = ArrayList()
    private var selectMenuIndex = 0

    // データ
    private var mAdapter: RecyclerTopAdapter? = null
    private var listData: ArrayList<DataSpotList> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTopBkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebase = FirebaseHelper(requireContext())
        mySP = MySharedPreferences(requireContext())
        functions = Functions(requireContext())

        firebase.sendScreen(FirebaseHelper.screenName.Top, null)

        val db = DBHelper(requireContext())
        try {
            // カテゴリー
            val category = DBTableCategory1(requireContext()).getAll(db)
            for (i in 0..category.size - 1) {
                dataCategoryArray.add(category[i])
            }

            // エリア
            val area = DBTableArea2(requireContext()).getAll(db)
            for (i in 0..area.size - 1) {
                val item = area[i]
                dataSelectionAreas.add(DataTopSelection(item.id, item.name, false))
            }

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // データの初期化
        condPage = 1
        condCategory = 0
        condArea = 0
        condSort = 1
        selectMenuIndex = 0
        if (listData.count() > 0) {
            listData.clear()
            if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
        }

        for (i in 0..dataSelectionAreas.size - 1) {
            dataSelectionAreas[i].checke = false
        }
        dataSelectionAreas[0].checke = true

        for (i in 0..dataSelectionSorts.size - 1) {
            dataSelectionSorts[i].checke = false
        }
        dataSelectionSorts[0].checke = true

        // メニュー
        createViewMenu()
        // 選択項目
        binding.textViewArea.text = dataSelectionAreas[0].value
        binding.textViewSort.text = dataSelectionSorts[0].value

        // エリアをクリック
        binding.viewArea.setOnClickListener {
            showSelection(1)
        }

        // 並べ替えをクリック
        binding.viewSort.setOnClickListener { showSelection(2) }

        // 広告
        binding.viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppTopPage, resources)

        // 一覧
        mAdapter = RecyclerTopAdapter(requireContext(), listData)

        binding.recyclerView.apply {
            // レイアウト設定 //
            setHasFixedSize(true)
            // 列
            val lmg = GridLayoutManager(context, mAdapter!!.spanCount)
            lmg.spanSizeLookup = mAdapter!!.spanSizeLookup
            layoutManager = lmg
            // マージン
            addItemDecoration(mAdapter!!.mItemDecoration)
            adapter = mAdapter

            // イベント //
            clearOnScrollListeners()
            addOnScrollListener(RecyclerInfiniteScrollListener(lmg) {
                // 続きを検索
                onSearch()
            })
        }
        mAdapter!!.setOnItemClickListener(View.OnClickListener { view ->
            // ※ダブルクリック禁止を実装(後で考える)
            val index = view.id
            val item: DataSpotList = listData[index]
            firebase.sendISSpotInfo(FirebaseHelper.screenName.Top, item.type, item.id)
            if (item.type == 1) {
                // スポット情報へ
                val intent = Intent(activity, ActivitySpotInfo_ImageSearch::class.java)
                intent.putExtra("id", item.id)
                intent.putExtra("name", item.name)
                startActivity(intent)
            } else {
                // 病院
            }
        })

    }

    // フラグメント　オンスクリーン
    override fun onResume() {
        super.onResume()
        // Loadingの設定
        if (fragmentManager != null) {
            var enable: Boolean = false
            if (loading == null) { enable = true }

            if (loading != null && !loading!!.isVisible) { enable = true }
            if (enable) {
                loading = LoadingNormal.newInstance(
                    message = getString(R.string.loading_normal_message),
                    isProgress = true
                )
                loading!!.show(parentFragmentManager, LoadingNormal.TAG)
            }
        }

        // Appデータの更新チェック(1時間に一度更新のチェックをする)
        val app_time = mySP!!.get(MySharedPreferences.Keys.app_data_update_time) as Long
        val oneHour = 1000 * 60 * 60.toLong()
        val nowTime = Date(System.currentTimeMillis()).time
        val diff = (nowTime - app_time) / oneHour
        if (diff >= 1) {
            // AppDataの更新
            mySP!!.put(MySharedPreferences.Keys.app_data_update_time, nowTime)
            updateAppData()
        } else {
            // 位置情報の更新へ
            setLocation()
        }
    }

    // フラグメント　オフスクリーン
    override fun onPause() {
        super.onPause()
        // getLocationHighQualityの取得に時間がかかるため
        if (mLocationClient != null && mLocationCallback != null) {
            mLocationClient!!.removeLocationUpdates(mLocationCallback!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            // AppDataの更新後
            REQUEST_APP_DATA_UPDATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 位置情報取得へ
                    setLocation()
                }
            }
        }
    }

    /*** スライドメニュー ***/
    /** メニューの作成  */
    private fun createViewMenu() {
        if (!viewMenuItems.isEmpty()) {
            viewMenuItems.clear()
        }
        // カテゴリー
        for (i in 0..dataCategoryArray.size - 1) {
            val item = dataCategoryArray[i]
            // 「健康」は含まない
            if (item.id == 7) continue
            val color: Int = if (item.id == 0) COLOR_DIS_CATEGORY_ALL else MyColor().top_menu_dis_category(item.id)
            val v = createViewMenuItem(i, item.name, color)
            v!!.setOnClickListener(View.OnClickListener { view ->
                val index = view.tag as Int
                selectViewMenuItem(index)
                // カテゴリーで検索
                // condCategory = if (index == 0) 0 else dataCategoryArray[index - 1].id
                condCategory = dataCategoryArray[index].id
                newOnSearch()
            })
            viewMenuItems.add(v)
            binding.viewMenu.addView(viewMenuItems.get(i))
        }

        // すべてを選択状態にする
        selectViewMenuItem(0)
    }

    /** メニューの項目を選択  */
    private fun selectViewMenuItem(tag: Int) {
        // 非選択
        var beforeColor = COLOR_DIS_CATEGORY_ALL
        if (selectMenuIndex > 0) {
            beforeColor = MyColor().top_menu_dis_category(dataCategoryArray[selectMenuIndex].id)
        }
        viewMenuItems[selectMenuIndex].setBackgroundColor(beforeColor)
        val textViewBefore = viewMenuItems[selectMenuIndex].getChildAt(0) as TextView
        textViewBefore.setTextColor(Color.BLACK)

        // 選択
        var selectColor = COLOR_CATEGORY_ALL
        if (tag > 0) {
            selectColor = MyColor().top_menu_category(dataCategoryArray[tag].id)
        }
        viewMenuItems[tag].setBackgroundColor(selectColor)
        binding.viewMenuUnder.setBackgroundColor(selectColor)
        val textViewSelect = viewMenuItems[tag].getChildAt(0) as TextView
        textViewSelect.setTextColor(Color.WHITE)
        selectMenuIndex = tag
    }

    /** メニューの項目を作成  */
    private fun createViewMenuItem(
        tag: Int,
        name: String,
        color: Int
    ): RelativeLayout? {
        val viewItem = RelativeLayout(context)
        viewItem.tag = tag
        val rlp = RelativeLayout.LayoutParams(
            80f.convertDpToPx(requireContext()).toInt(),
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        viewItem.layoutParams = rlp
        viewItem.setBackgroundColor(color)
        val textView = TextView(context)
        val tlp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        tlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        tlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        textView.layoutParams = tlp
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.textSize = 13.0f
        textView.setTextColor(Color.BLACK)
        textView.text = name
        viewItem.addView(textView)
        return viewItem
    }


    // layoutSelectionは後ろをクリックしたらボタンを押せないようにする
    private fun showSelection(type: Int) {
        setClickEnableParentView(false)
        // binding.layoutSelection.visibility = View.VISIBLE
        val animeAlpha = AlphaAnimation(0f, 1f)
        animeAlpha.duration = 250
        // binding.layoutSelection.startAnimation(animeAlpha)

        var title = ""
        var items: ArrayList<DataTopSelection> = ArrayList()
        when (type) {
            1 -> {
                title = "エリア"
                items = dataSelectionAreas
            }
            2 -> {
                title = "並び替え"
                items = dataSelectionSorts
            }
        }

        binding.layoutSelection.textViewTitle.text = title

        // 閉じる
        binding.layoutSelection.viewClose.setOnClickListener {
            hideSelection()
        }

        val adapter = ListTopSelectionAdapter(requireContext(), 0)
        adapter.addAll(items)
        binding.layoutSelection.listView.adapter = adapter

        // チェックした位置にスクロール
        for (i in 0..items.size - 1) {
            if (items[i].checke) {
                // binding.listView.setSelection(if (i>2) i - 2 else 0)
                break
            }
        }

        binding.layoutSelection.listView.setOnItemClickListener { parent, view, position, id ->
            when (type) {
                1 -> {
                    // エリア
                    for (i in 0..dataSelectionAreas.size - 1) {
                        dataSelectionAreas[i].checke = false
                    }
                    dataSelectionAreas[position].checke = true
                    binding.textViewArea.text = dataSelectionAreas[position].value
                    condArea = dataSelectionAreas[position].key
                    if (condArea == 0) {
                        mySP!!.put(MySharedPreferences.Keys.top_location_first_alert, false)
                    }
                }
                2 -> {
                    // 並び替え
                    for (i in 0..dataSelectionSorts.size - 1) {
                        dataSelectionSorts[i].checke = false
                    }

                    dataSelectionSorts[position].checke = true
                    binding.textViewSort.text = dataSelectionSorts[position].value
                    condSort = dataSelectionSorts[position].key
                }
            }

            // 検索へ
            newOnSearch()

            // 選択モーダルを消す
            hideSelection()
        }

        // チェックした位置にスクロール
    }

    private fun hideSelection() {
        setClickEnableParentView(true)
        // binding.layoutSelection.visibility = View.GONE
    }

    private fun setClickEnableParentView(enable: Boolean) {
        for (i in 0..viewMenuItems.size - 1) {
            viewMenuItems[i].isEnabled = enable
        }
        binding.viewArea.isEnabled = enable
        binding.viewSort.isEnabled = enable
        mAdapter!!.setIsClickable(enable)
    }

    /**
     * アプリデータの更新を確認後、更新を行う
     */
    private fun updateAppData() {
        // AppDataの更新後に位置情報へ
        if (loading != null) {
            loading!!.updateLayout("データの更新を確認中...", true)
        }

        // アプリデータの更新を確認
        var app_category_date: String = ""
        var app_area_date: String = ""
        val db = DBHelper(requireContext())
        try {
            val tableAppData = DBTableAppData(requireContext())
            ifNotNull(tableAppData.getData(db, DBTableAppData.Ids.category_update), {
                app_category_date = it.modified!!.convertString()
            })
            ifNotNull(tableAppData.getData(db, DBTableAppData.Ids.area_update), {
                app_area_date = it.modified!!.convertString()
            })
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }

        val url = MyString().my_http_url_app() + "/app_data/check_update.php"
        val params = listOf("md_category" to app_category_date, "md_area" to app_area_date)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {
                    if (loading != null) {
                        loading!!.onDismiss()
                    }
                    // データの更新へ
                    val intent = Intent(requireContext(), ActivityAppDataUpdate::class.java)
                    intent.putExtra("tag", TAG)
                    startActivityForResult(intent, REQUEST_APP_DATA_UPDATE)
                } else {
                    // 位置情報を取得へ
                    setLocation()
                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
            })
        }

    }

    /** 位置情報取得 -> 周辺検索への流れ　（周辺検索する場合はこの関数を読めばOK） **/
    private fun setLocation() {

        if (loading != null) {
            loading!!.updateLayout("現在地を取得中...", true)
        }

        // 端末の位置情報サービスをチェック
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // パーミッションのチェックが必要か確認
            if (Build.VERSION.SDK_INT >= 23) {
                // Android6以上
                checkPermission()
            } else {
                // それ以外
                getLocation()
            }
        } else {
            errorLocation()
        }
    }

    /**
     * Permissionのチェック
     */
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // アプリの権限が許可してる
            getLocation()
        } else {
            // 許可してない
            //Log.i(">> $TAG", "checkPermission more request")
            requestLocationPermission()
        }
    }

    /**
     * Permissionの不許可チェック
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Log.i(">> $TAG", "requestLocationPermission alert");
            // パーミッションの許可で不許可にした場合
            errorLocation()
        } else {
            //Log.i(">> $TAG", "requestLocationPermission request")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_FINE_LOCATION
            )
        }
    }

    /**
     * Permissionアラートの結果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // ここで許可or許可しないしても、次にFragmentのonResumeが呼ばれるため以下は実行しない

        /*
        //Log.i(">> $TAG", "onRequestPermissionsResult: $requestCode")
        when (requestCode) {
            REQUEST_PERMISSION_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可された時
                    // Log.i(">> $TAG", "onRequestPermissionsResult OK");
                    getLocation()
                } else {
                    // 拒否された時
                    //Log.i(">> $TAG", "onRequestPermissionsResult alert");
                    errorLocation()
                }
            }
        }
        */
    }

    /**
     * 位置情報を取得　Permissionで許可された時
     */
    private fun getLocation() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationClient!!.lastLocation.addOnCompleteListener(
            requireActivity(),
            OnCompleteListener<Location?> { task ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        mLocation = task.result
                        // この後周辺検索へ
                        onSearch()
                    } else {
                        // last location is null
                        getLocationHighQuality()
                    }
                } else {
                    errorLocation()
                }
            })
    }

    /**
     * getLocationで位置取得できない時、さらに位置情報取得
     * ※位置情報サービスOFF -> ON に変更した時になる
     */
    private fun getLocationHighQuality() {
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 500
        request.fastestInterval = 300

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                mLocation = result.lastLocation
                // 現在地だけ欲しいので、1回取得したらすぐに外す
                mLocationClient!!.removeLocationUpdates(this)
                // この後周辺検索へ
                onSearch()
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationClient!!.requestLocationUpdates(request, mLocationCallback!!, null)
    }

    /**
     * 位置情報の取得失敗
     */
    private fun errorLocation() {
        // ※エリアで「周辺」が選択されたタイミングでtop_location_first_alert=falseする
        if (!(mySP!!.get(MySharedPreferences.Keys.top_location_first_alert) as Boolean)) {
            // アラートを表示
            mySP!!.put(MySharedPreferences.Keys.top_location_first_alert, true)

            val alert = AlertNormal.newInstance(
                requestCode = REQUEST_CODE_ALERT_LOCATION,
                title = "現在地を取得できませんでした",
                msg = "周辺のお店を検索するには位置情報サービスをONにしてください",
                positiveLabel = "OK",
                negativeLabel = null
            )
            alert.setFragment(this)
            alert.show(parentFragmentManager, AlertNormal.TAG)
        }
        // 位置情報なしの検索へ
        onSearch()
    }

    /**
     * タッチイベント後の検索はここを通る
     */
    private fun newOnSearch() {
        condPage = 1
        mLocation = null

        mAdapter!!.setRandomNumber()

        // イベントを更新する
        val lmg = binding.recyclerView.layoutManager as LinearLayoutManager
        binding.recyclerView.clearOnScrollListeners()
        binding.recyclerView.addOnScrollListener(RecyclerInfiniteScrollListener(lmg) {
            onSearch()
        })

        loading!!.show(parentFragmentManager, LoadingNormal.TAG)
        loading!!.updateLayout(getString(R.string.loading_normal_message), true)

        if (condArea == 0) {
            // 位置情報を更新してから検索
            setLocation()
        } else {
            onSearch()
        }
    }

    /**
     * お店の検索
     */
    private fun onSearch() {
        if (loading != null) {
            loading!!.updateLayout(getString(R.string.loading_normal_message), true)
            loading!!.onDismiss(1000)
        }

        val params: ArrayList<Pair<String, Any>> = ArrayList()
        val fParams: ArrayList<Pair<String, Any>> = ArrayList()
        params.add("page" to condPage)
        fParams.add("page" to condPage.toString())
        if (condArea > 0) {
            // エリア検索
            params.add("cond_area" to condArea)
            fParams.add("cond_area" to condArea.toString())
        } else {
            // 周辺検索
            if (mLocation != null) {
                params.add("latitude" to mLocation!!.latitude)
                params.add("longitude" to mLocation!!.longitude)
                fParams.add("lat_lon" to "%f,%f".format(mLocation!!.latitude, mLocation!!.longitude))
            }
        }

        if (condCategory > 0) {
            params.add("cond_category[0]" to condCategory)
            fParams.add("cond_category" to condCategory.toString())
        } else {
            // カテゴリーの選択がない時は健康をとった大カテゴリーで検索する
            // すべて //
            if (dataCategoryArray.count() > 0) {
                fParams.add("cond_category" to "all")
                for (i in 0..dataCategoryArray.size - 1) {
                    val id = dataCategoryArray.get(i).id
                    // 「健康」は含まない
                    if (id == 7) { continue }
                    params.add("cond_category[$i]" to id)
                }
            }
        }

        if (condSort > 0) {
            params.add("cond_sort" to condSort)
            fParams.add("cond_sort" to condSort.toString())
        }

        firebase.sendScreen(FirebaseHelper.screenName.Top, fParams)

        val url = MyString().my_http_url_app() + "/search_spot/search.php"
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->
                if (loading != null) {
                    loading!!.onDismiss(300)
                }

                if (condPage == 1 && listData.count() > 0) {
                    binding.recyclerView.smoothScrollToPosition(0)
                    listData.clear()
                    mAdapter!!.notifyDataSetChanged()
                }

                val datas = json.obj().get("datas") as JSONObject
                if (datas.get("result") as Boolean) {
                    val spot_array = datas.getJSONArray("spot_array")
                    for (i in 0..spot_array.length() - 1) {
                        val obj = spot_array.getJSONObject(i)
                        val review_image_list = obj.getJSONArray("review_image_list")
                        val review_image_array: ArrayList<String> = ArrayList()
                        if (review_image_list.length() > 0) {
                            for (j in 0..review_image_list.length() - 1) {
                                review_image_array.add(review_image_list.getString(j))
                            }
                        }
                        val distance: String = if (mLocation != null) obj.getString("distance") else ""
                        listData.add(
                            DataSpotList(
                                obj.getInt("id"),
                                obj.getInt("type"),
                                obj.getString("name"),
                                obj.getString("address"),
                                obj.getInt("parent_category_id"),
                                obj.getString("category"),
                                distance,
                                obj.getInt("review_num"),
                                obj.getString("image"),
                                obj.getBoolean("checkin_enable"),
                                obj.getBoolean("coupon_enable"),
                                obj.getInt("review_image_num"),
                                obj.getInt("favorite_num"),
                                review_image_array,
                                0.0,
                                0.0
                            )
                        )
                    }
                    mAdapter!!.notifyDataSetChanged()
                    condPage++
                } else {

                }
            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
            })
        }
    }

}
