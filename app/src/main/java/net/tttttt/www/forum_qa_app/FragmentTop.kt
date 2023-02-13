package net.tttttt.www.forum_qa_app


import android.content.Intent
import android.os.*
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import net.tttttt.www.forum_qa_app.databinding.FragmentTopBinding
import net.tttttt.www.forum_qa_app.network.FirebaseHelper
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.*
import net.tttttt.www.forum_qa_app.view.CardTopAdapter
import org.json.JSONObject

// カードビューを使う
// https://www.youtube.com/watch?v=J2nuk1Q_bNs

class FragmentTop : Fragment() {
    companion object {
        val TAG = "Top"
    }

    private lateinit var binding: FragmentTopBinding

    // リクエスト
    /*
    private val REQUEST_PERMISSION_FINE_LOCATION: Int = 0x1
    private val REQUEST_CODE_ALERT_LOCATION: Int = 0x2
    private val REQUEST_APP_DATA_UPDATE: Int = 0x3

    // カラー
    private val COLOR_CATEGORY_ALL = Color.parseColor("#555555")
    private val COLOR_DIS_CATEGORY_ALL = Color.parseColor("#CCCCCC")
    */

    private lateinit var firebase: FirebaseHelper
    private var mySP: MySharedPreferences? = null
    private var functions: Functions? = null

    // UI //
    private var loading: LoadingNormal? = null
    // 変数 //

    // データ
    /*private var mAdapter: RecyclerTopAdapter? = null
    private var listData: ArrayList<DataSpotList> = ArrayList()*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        firebase = FirebaseHelper(requireContext())
        mySP = MySharedPreferences(requireContext())
        functions = Functions(requireContext())

        firebase.sendScreen(FirebaseHelper.screenName.Top, null)

        /*
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
        */
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CardTopAdapter(arrayOf(1,1,1,1))
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_option, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*
        when (item.itemId) {
            R.id.action_support ->

                R.id.action_1 ->
        }
        */
        Toast.makeText(context, "「${item.title}」が押されました。", Toast.LENGTH_SHORT).show()
        return super.onOptionsItemSelected(item)
    }
    // フラグメント　オンスクリーン
    override fun onResume() {
        super.onResume()
        /*
        // Loadingの設定
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
        */

        // Appデータの更新チェック(1時間に一度更新のチェックをする)
        /*val app_time = mySP!!.get(MySharedPreferences.Keys.app_data_update_time) as Long
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
        */
    }

    // フラグメント　オフスクリーン
    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            // AppDataの更新後
            /*REQUEST_APP_DATA_UPDATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 位置情報取得へ
                    setLocation()
                }
            }*/
        }
    }

    /**
     * アプリデータの更新を確認後、更新を行う
     */
    private fun updateAppData() {
        // AppDataの更新後に位置情報へ
        if (loading != null) {
            loading!!.updateLayout("データの更新を確認中...", true)
        }

        /*val url = MyString().my_http_url_app() + "/app_data/check_update.php"
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
        }*/

    }

    /**
     * Permissionのチェック
     */
    private fun checkPermission() {
        /*if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // アプリの権限が許可してる
            getLocation()
        } else {
            // 許可してない
            //Log.i(">> $TAG", "checkPermission more request")
            requestLocationPermission()
        }*/
    }

    /**
     * Permissionの不許可チェック
     */
    private fun requestLocationPermission() {
        /*if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
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
        }*/
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

}
