package net.tttttt.www.forum_qa_app


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.andremion.louvre.Louvre
import net.tttttt.www.forum_qa_app.databinding.FragmentPostQaBinding
import net.tttttt.www.forum_qa_app.view.AlertNormal
import net.tttttt.www.forum_qa_app.view.ModalUriImagePreview
import net.tttttt.www.forum_qa_app.view.RecyclerPostImageAdapter

class FragmentPostQa : Fragment() {

    // 定数 //
    private val MAX_SELECT_IMAGE: Int = 3

    private lateinit var binding: FragmentPostQaBinding

    // 画像投稿 //
    private var imageAdapter: RecyclerPostImageAdapter? = null
    private var imageListData: ArrayList<Uri?> = arrayListOf(null) // 最初にnullを１つ入れる

    companion object {
        val TAG = "PostQa"

        fun newInstance(): FragmentPostQa {
            return FragmentPostQa()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // requestStoragePermission()

        imageAdapter = RecyclerPostImageAdapter(requireContext(), imageListData)

        imageAdapter!!.setOnItemClickListener {
            val index = it.id

            when (it.tag) {
                imageAdapter!!.TAG_VIEW_IMAGE -> {
                    // プレビュー
                    val uris: ArrayList<Uri> = arrayListOf()
                    for (i in 0..imageListData.size - 1) {
                        if (imageListData[i] != null) {
                            uris.add(imageListData[i]!!)
                        }
                    }
                    /*val modal = ModalUriImagePreview.newInstance(uris, index)
                    modal.show(supportFragmentManager, ModalUriImagePreview.TAG)*/
                }
                imageAdapter!!.TAG_VIEW_BUTTON_DELETE -> {
                    // 写真の削除
                    /*deleteItemIndex = index
                    val alert = AlertNormal.newInstance(
                        requestCode = REQUEST_ALERT_IMAGE_DELETE,
                        title = "写真を削除しますか？",
                        msg = null,
                        positiveLabel = "削除",
                        negativeLabel = "キャンセル"
                    )
                    alert.show(supportFragmentManager, AlertNormal.TAG)*/
                }
                imageAdapter!!.TAG_VIEW_ADD -> {

                    // registerForActivityResult ここでエらる
                    val pickMultipleMedia =
                        registerForActivityResult(
                            ActivityResultContracts.PickMultipleVisualMedia(
                                MAX_SELECT_IMAGE - (imageListData.size - 1)
                            )
                        ) { uris ->
                            // Callback is invoked after the user selects media items or closes the
                            // photo picker.
                            if (uris.isNotEmpty()) {
                                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                            } else {
                                Log.d("PhotoPicker", "No media selected")
                            }
                        }
                    pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                    /*Louvre.init(this)
                        .setRequestCode(111)
                        .setMaxSelection(MAX_SELECT_IMAGE - (imageListData.size - 1))
                        .setSelection(listOf<Uri>())
                        .setMediaTypeFilter(Louvre.IMAGE_TYPE_JPEG, Louvre.IMAGE_TYPE_PNG, Louvre.IMAGE_TYPE_BMP)
                        .open()*/

                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostQaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLayout()
    }

    /*private fun requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if(result) {
                    // リクエスト許可時の処理
                    // Toast.makeText(this, "Permission Accepted.", Toast.LENGTH_SHORT).show()
                }
                else {
                    // リクエスト拒否時の処理
                    // Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show()
                }
            }
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }*/

    fun initLayout() {


        binding.rcvImages.apply {
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
