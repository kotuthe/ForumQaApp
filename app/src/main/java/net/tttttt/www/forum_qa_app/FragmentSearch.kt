package net.tttttt.www.forum_qa_app


import android.content.Intent
import android.os.*
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import net.tttttt.www.forum_qa_app.value.*
import net.tttttt.www.forum_qa_app.view.*
import net.tttttt.www.forum_qa_app.databinding.FragmentSearchBinding
import net.tttttt.www.forum_qa_app.view.CardTopicsAdapter
import net.tttttt.www.forum_qa_app.entities.DataCardTopics
import java.text.SimpleDateFormat
import java.util.Date

// カードビューを使う
// https://www.youtube.com/watch?v=J2nuk1Q_bNs

class FragmentSearch : Fragment() {
    companion object {
        val TAG = "Search"
    }

    private lateinit var binding: FragmentSearchBinding

    private var mySP: MySharedPreferences? = null
    private var functions: Functions? = null

    // UI //
    private var loading: LoadingNormal? = null
    // 変数 //

    // データ
    private var listData: Array<DataCardTopics> = arrayOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mySP = MySharedPreferences(requireContext())
        functions = Functions(requireContext())

        // テストデータ //
        listData = arrayOf(
            DataCardTopics(convertStrToDate("2022-01-16"), "システムメンテナンスのお知らせ", "本サイトは0000年00月00日（○）にサーバーのメンテナンスを実施します。\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "停止に伴い、下記の通りホームページの閲覧および全サービスを一時休止いたします。\n" +
                    "お客様にはご不便をおかけいたしまして、誠に申し訳ございませんが、ご了承くださいますようお願い申し上げます。\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "■ホームページの閲覧およびサービスの休止日時\n" +
                    "　0000年00月00日（○）00：00 ～ 0000年00月00日（○）00：00\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "※作業の状況により終了時間が前後することがございますのでご了承下さい。",true),
            DataCardTopics(convertStrToDate("2022-03-19"), "○○さんにフォローされました", "テストさんにフォローされました",true),
            DataCardTopics(convertStrToDate("2022-08-01"), "質問の回答が届いています", "こんにちはテストさん",false),
            DataCardTopics(convertStrToDate("2022-12-12"), "システムメンテナンスのお知らせ", "本サイトは0000年00月00日（○）にサーバーのメンテナンスを実施します。\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "停止に伴い、下記の通りホームページの閲覧および全サービスを一時休止いたします。\n" +
                    "お客様にはご不便をおかけいたしまして、誠に申し訳ございませんが、ご了承くださいますようお願い申し上げます。\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "■ホームページの閲覧およびサービスの休止日時\n" +
                    "　0000年00月00日（○）00：00 ～ 0000年00月00日（○）00：00\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "※作業の状況により終了時間が前後することがございますのでご了承下さい。",false),
        )
        // テストデータ //

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CardTopicsAdapter(listData)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        adapter.setOnItemClickListener {
            val item: DataCardTopics = listData[it.id]
            // Toast.makeText(context, "「${item.title}」", Toast.LENGTH_SHORT).show()

            // 詳細へ
            val intent = Intent(activity, ActivityTopicsDetail::class.java)
            intent.putExtra("date", item.date.convertString("YYYY-MM-dd"))
            intent.putExtra("title", item.title)
            intent.putExtra("note", item.note)
            startActivity(intent)
        }
    }

    private fun convertStrToDate(str: String): Date {
        val df = SimpleDateFormat("yyyy-MM-dd")
        val date = df.parse(str)
        return date!!
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
    }

}
