package net.tttttt.www.forum_qa_app


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_post_qa.*
import net.tttttt.www.forum_qa_app.view.RecyclerInputReviewAdapter

class FragmentPostQa : Fragment() {

    // 定数 //
    private val MAX_SELECT_IMAGE: Int = 3

    // 画像投稿 //
    private var imageAdapter: RecyclerInputReviewAdapter? = null
    private var imageListData: ArrayList<Uri?> = arrayListOf(null) // 最初にnullを１つ入れる

    companion object {
        val TAG = "PostQa"

        fun newInstance(): FragmentPostQa {
            return FragmentPostQa()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_qa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLayout()
    }

    fun initLayout() {

        imageAdapter = RecyclerInputReviewAdapter(requireContext(), imageListData)
        rcvImages.apply {
            setHasFixedSize(true)

            val glm = GridLayoutManager(context, imageAdapter!!.spanCount)
            glm.spanSizeLookup = imageAdapter!!.spanSizeLookup
            layoutManager = glm

            addItemDecoration(imageAdapter!!.mItemDecoration)
            adapter = imageAdapter
        }
    }

    /**
     * ギャラーから追加した画像
     */
    private fun addImageListData(uris: ArrayList<Uri>) {
        for (i in 0..uris.size - 1) {
            imageListData[imageListData.size - 1] = uris[i]
            if (imageListData.size < MAX_SELECT_IMAGE) {
                // 9アイテム以下の時まではADDを追加
                imageListData.add(null)
            }
        }
        imageAdapter!!.notifyDataSetChanged()
    }

    /**
     * ギャラリー画像を削除
     */
    private fun removeImageListData(index: Int) {
        imageListData.removeAt(index)
        if (imageListData[imageListData.size - 1] != null) {
            imageListData.add(null)
        }
        imageAdapter!!.notifyDataSetChanged()
    }

}
