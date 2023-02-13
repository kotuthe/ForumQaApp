package net.tttttt.www.forum_qa_app


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
import net.tttttt.www.forum_qa_app.databinding.FragmentSpotSearchBinding
import net.tttttt.www.forum_qa_app.entities.DataCategory1
import net.tttttt.www.forum_qa_app.entities.DataListSearch
import net.tttttt.www.forum_qa_app.storage.DBHelper
import net.tttttt.www.forum_qa_app.storage.DBTableCategory1
import net.tttttt.www.forum_qa_app.value.MyImage
import net.tttttt.www.forum_qa_app.view.ListSearchAdapter
import net.tttttt.www.forum_qa_app.view.ViewAdvtFooter


class FragmentSpotSearch : Fragment() {

    companion object {
        val TAG = "FragmentSpotSearch"
        val TAG_SHORT = "SpotSearch"
    }

    private lateinit var binding: FragmentSpotSearchBinding

    private var dataCategory: ArrayList<DataCategory1> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpotSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DBHelper(requireContext())
        try {
            dataCategory = DBTableCategory1(requireContext()).getAll(db)
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
            val mAdapter = ListSearchAdapter(requireContext(), 0)
            for (i in 0..dataCategory.size - 1) {
                val item = dataCategory.get(i)
                mAdapter.add(DataListSearch(MyImage().icon_category(item.id), item.name, item.sub_title))
            }

            binding.listView.apply {
                adapter = mAdapter
                setOnItemClickListener { parent, view, position, id ->
                    // カテゴリー検索へ
                    val item = dataCategory.get(position)
                    onSearch(null, item.id)
                }
            }
        }

        // 広告
        binding.viewAdvtFooter.setAdvt(ViewAdvtFooter.screenName.AppSearch, resources)
    }

    override fun onResume() {
        super.onResume()
        if (activity != null) {
            requireActivity().title = getString(R.string.spot_search_title)
        }
    }

    override fun onPause() {
        super.onPause()
        // 検索フォームをクリア
        if (!binding.searchView.isIconified) {
            binding.searchView.isIconified = true
            binding.searchView.isIconified = true
        }
    }

    /** 検索フォームの設定 **/
    private fun setSearchView() {
        // SearchViewのキャンセル
        binding.buttonCancel.setOnClickListener {
            binding.searchView.isIconified = true
            binding.searchView.isIconified = true
            // textViewSVHint.visibility = View.VISIBLE
        }

        // SearchView //
        // ヒント
        binding.textViewSVHint.setOnClickListener {
            binding.searchView.isIconified = false
            binding.textViewSVHint.visibility = View.GONE
        }

        // 虫眼鏡のクリック
        binding.searchView.setOnSearchClickListener {
            binding.textViewSVHint.visibility = View.GONE
        }

        // テキストの変化
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
        binding.searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.textViewSVHint.visibility = View.VISIBLE
                return false
            }
        })
    }

    /** キーボードを閉じる **/
    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val manager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /** 検索へ **/
    private fun onSearch(word: String?, category: Int) {
        val intent = Intent(activity, ActivitySpotSearchList::class.java)
        intent.putExtra("word", if (word == null) "" else word)
        intent.putExtra("category", category)
        startActivity(intent)

    }


}
