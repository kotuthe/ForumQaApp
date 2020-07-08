package net.tochinavi.www.tochinaviapp


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_spot_search.*
import net.tochinavi.www.tochinaviapp.entities.DataCategory1
import net.tochinavi.www.tochinaviapp.entities.DataListSearch
import net.tochinavi.www.tochinaviapp.storage.DBHelper
import net.tochinavi.www.tochinaviapp.storage.DBTableCategory1
import net.tochinavi.www.tochinaviapp.value.MyImage
import net.tochinavi.www.tochinaviapp.view.ListSearchAdapter
import net.tochinavi.www.tochinaviapp.view.ViewAdvtFooter


class FragmentSpotSearch : Fragment() {

    companion object {
        val TAG = "FragmentSpotSearch"
        val TAG_SHORT = "SpotSearch"
    }

    private var dataCategory: ArrayList<DataCategory1> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spot_search, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DBHelper(context!!)
        try {
            dataCategory = DBTableCategory1(context!!).getAll(db)
        } catch (e: Exception) {
            Log.e(TAG_SHORT, "" + e.message)
        } finally {
            db.cleanup()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // ここでViewの設定をする
        setSearchView()

        // カテゴリーの一覧
        if (dataCategory.size > 0) {
            val mAdapter = ListSearchAdapter(context!!, 0)
            for (i in 0..dataCategory.size - 1) {
                val item = dataCategory.get(i)
                mAdapter.add(DataListSearch(MyImage().icon_category(item.id), item.name, item.sub_title))
            }

            listView.apply {
                adapter = mAdapter
                setOnItemClickListener { parent, view, position, id ->
                    // カテゴリー検索へ
                    val item = dataCategory.get(position)
                    onSearch(null, item.id)
                }
            }
        }

        // 広告
        viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppSearch, resources)
    }

    override fun onResume() {
        super.onResume()
        Log.i(">> $TAG", "onResume")
        if (activity != null) {
            activity!!.title = getString(R.string.spot_search_title)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(">> $TAG", "onPause")

        // 検索フォームをクリア
        if (!searchView.isIconified) {
            searchView.isIconified = true
            searchView.isIconified = true
        }
    }

    /** 検索フォームの設定 **/
    private fun setSearchView() {
        // SearchViewのキャンセル
        buttonCancel.setOnClickListener {
            searchView.isIconified = true
            searchView.isIconified = true
            // textViewSVHint.visibility = View.VISIBLE
        }

        // SearchView //
        // ヒント
        textViewSVHint.setOnClickListener {
            searchView.isIconified = false
            textViewSVHint.visibility = View.GONE
        }

        // 虫眼鏡のクリック
        searchView.setOnSearchClickListener {
            textViewSVHint.visibility = View.GONE
        }

        // テキストの変化
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                // テキスト検索へ
                hideKeyboard()
                onSearch(query, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        // 閉じるイベント
        searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                textViewSVHint.visibility = View.VISIBLE
                return false
            }
        })
    }

    /** キーボードを閉じる **/
    private fun hideKeyboard() {
        val view = activity!!.currentFocus
        if (view != null) {
            val manager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /** 検索へ **/
    private fun onSearch(word: String?, category: Int) {
        Log.i("$TAG", "w: $word, c: $category")
        val intent = Intent(activity, ActivitySpotSearchList::class.java)
        intent.putExtra("word", if (word == null) "" else word)
        intent.putExtra("category", category)
        startActivity(intent)

    }


}
