package net.tochinavi.www.tochinaviapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page_no_login.*
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableUsers
import net.tochinavi.www.tochinaviapp.value.ifNotNull
import net.tochinavi.www.tochinaviapp.view.ViewAdvtFooter


class FragmentMyPage : Fragment() {

    companion object {
        val TAG = "FragmentMyPage"
    }

    private val REQUEST_LOGIN: Int = 0x1

    private var mContext: Context? = null

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(">>", "onCreate")

        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateLayout()

        // イベント
        buttonLogin.setOnClickListener {

            // ログインページへ > ログインして > 戻って > FragmentMyPageを更新
            val intent = Intent(activity, ActivityLogin::class.java)
            intent.putExtra("tag", TAG)
            startActivityForResult(intent, REQUEST_LOGIN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateLayout()
                }
            }
        }
    }

    private fun updateLayout() {
        // メンバーIDを取得
        val db = DBHelper(mContext!!)
        try {
            val tableUsers = DBTableUsers(mContext!!)
            ifNotNull(tableUsers.getData(db, DBTableUsers.Ids.member_login), {
                userId = it.user_id
            })
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }

        // レイアウトの更新
        if (userId > 0) {
            // Myページ
            layoutNoLogin.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE

            // 広告
            viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppMyPage, resources)
        } else {
            // ログインページ
            layoutNoLogin.visibility = View.VISIBLE
            layoutLogin.visibility = View.GONE
        }
    }

    private fun setMyPageLayout() {
        // ここにユーザー情報取得後の値設定をプログラムする
    }


}
