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


class FragmentSpotSearch : Fragment() {

    companion object {
        val TAG = "FragmentSpotSearch"
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
            Log.e(TAG, "" + e.message)
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
            val adapter = ListSearchAdapter(context!!, 0)
            for (i in 0..dataCategory.size - 1) {
                val item = dataCategory.get(i)
                adapter.add(DataListSearch(MyImage().icon_category(item.id), item.name, item.sub_title))
            }
            listView.adapter = adapter
            listView.setOnItemClickListener { parent, view, position, id ->
                // カテゴリー検索へ
                val item = dataCategory.get(position)
                onSearch(null, item.id)
            }
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
                onSearch(query, null)
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
    private fun onSearch(word: String?, category: Int?) {
        /*
        val intent = Intent(activity, ActivitySpotSearch::class.java)
        intent.putExtra("word", word)
        intent.putExtra("category", category)
        startActivityForResult(intent, REQUEST_LOGIN)
        */

    }


}
