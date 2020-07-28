package net.tochinavi.www.tochinaviapp



import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page_no_login.*
import kotlinx.android.synthetic.main.layout_my_page_number.view.*
import net.tochinavi.www.tochinaviapp.entities.DataUsers
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.MyString
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.ListMyPageAdapter
import net.tochinavi.www.tochinaviapp.view.ViewAdvtFooter
import org.json.JSONObject


class FragmentMyPage : Fragment() {

    companion object {
        val TAG = "MyPage"
    }

    // リクエスト
    private val REQUEST_LOGIN: Int = 0x1
    private val REQUEST_SETTING: Int = 0x2

    private var mContext: Context? = null
    private var mySP: MySharedPreferences? = null

    private var userId: Int = 0
    private var userData: DataUsers? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(">>", "onCreate")

        mContext = context
        mySP = MySharedPreferences(mContext!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateUserData()

        setHasOptionsMenu(true)

        // イベント（これは1度設定すると削除したりするのが面倒だから）
        buttonLogin.setOnClickListener {
            // ログインページへ > ログインして > 戻って > FragmentMyPageを更新
            val intent = Intent(activity, ActivityLogin::class.java)
            intent.putExtra("tag", TAG)
            startActivityForResult(intent, REQUEST_LOGIN)
        }

        val adapter: BaseAdapter = ListMyPageAdapter(
            mContext!!,
            arrayListOf(
                R.drawable.img_mypage_badge,
                R.drawable.img_mypage_checkin,
                R.drawable.img_mypage_review,
                R.drawable.img_mypage_draft,
                R.drawable.img_mypage_favorite
            ),
            arrayListOf(
                "称号一覧",
                "チェックイン履歴",
                "クチコミ一覧",
                "クチコミ下書き",
                "お気に入り"
            )
        )
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, pos, id ->
            when(pos) {
                0 -> {
                    // 称号
                    val intent = Intent(activity, ActivityMyBadgeList::class.java)
                    startActivity(intent)
                }
                1 -> {
                    // チェックイン履歴
                    val intent = Intent(activity, ActivityMyCheckinList::class.java)
                    startActivity(intent)
                }
                2 -> {
                    // クチコミ一覧
                    val intent = Intent(activity, ActivityMyReviewList::class.java)
                    startActivity(intent)
                }
                3 -> {
                    // クチコミ下書き
                    val intent = Intent(activity, ActivityMyDraftList::class.java)
                    startActivity(intent)
                }
                4 -> {
                    // お気に入り
                    val intent = Intent(activity, ActivityMyWishList::class.java)
                    startActivity(intent)
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()
        Log.i(">> $TAG", "onResume")

        if (activity != null) {
            activity!!.title = getString(R.string.mypage_title)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(">> $TAG", "onPause")
    }

    /** メニュー **/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // getActivity().getMenuInflater().inflate(R.menu.setting, menu);
        inflater.inflate(R.menu.setting, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.setting) {
            // 設定
            val intent = Intent(activity, ActivityMyPageSetting::class.java)
            startActivityForResult(intent, REQUEST_SETTING)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            // ログイン ro 設定から戻った時
            REQUEST_LOGIN, REQUEST_SETTING -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateUserData()
                }
            }
        }
    }

    /**
     * UserDataの更新
     */
    private fun updateUserData() {

        if (mySP!!.get_status_login()) {
            // メンバーIDを取得
            val db = DBHelper(mContext!!)
            try {
                val tableUsers = DBTableUsers(mContext!!)
                ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                    userId = it.user_id
                    userData = it
                })
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            } finally {
                db.cleanup()
            }
        } else {
            userId = 0
        }

        // レイアウトの更新
        if (userId > 0) {
            // Myページ
            layoutNoLogin.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
            initMyPageLayout()
            getMyPageData()
        } else {
            // ログインページ
            layoutNoLogin.visibility = View.VISIBLE
            layoutLogin.visibility = View.GONE
        }
    }

    /**
     * MyPageの初期データ設定
     */
    private fun initMyPageLayout() {

        if (userData!!.image != null) {
            imageViewUser.setImageBitmap(userData!!.image)
        }
        textViewUserName.text = userData!!.name

        textViewUserDetail.text = ""
        layoutNumberBadge.textViewTitle.text = "称号獲得数"
        layoutNumberCheckin.textViewTitle.text = "チェックイン数"
        layoutNumberReview.textViewTitle.text = "クチコミ数"

        // 広告
        viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppMyPage, resources)
    }

    /**
     * MyPageデータの取得
     */
    private fun getMyPageData() {
        val url = MyString().my_http_url_app() + "/mypage/get_mypage_detail.php"
        val params = listOf("id" to userId)
        url.httpGet(params).responseJson { request, response, result ->
            result.fold(success = { json ->

                val datas = json.obj().get("datas") as JSONObject
                val result = datas.get("result") as Boolean
                if (result) {

                    val info = datas.getJSONObject("info")
                    // Myページの情報更新
                    textViewUserDetail.text = info.getString("user_info")
                    layoutNumberBadge.textViewNumber.text = info.getInt("badge_num").toString()
                    layoutNumberCheckin.textViewNumber.text = info.getInt("checkin_num").toString()
                    layoutNumberReview.textViewNumber.text = info.getInt("review_num").toString()

                    scrollView.post(Runnable {
                        if (scrollView != null) {
                            scrollView.fullScroll(View.FOCUS_UP)
                        }
                    })
                }

            }, failure = { error ->
                // 通信エラー
                Log.e(TAG, error.toString())
            })
        }
    }

}
