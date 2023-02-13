package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import net.tttttt.www.forum_qa_app.databinding.FragmentMyPageBinding
import net.tttttt.www.forum_qa_app.entities.DataMember
import net.tttttt.www.forum_qa_app.value.MyAddress
import net.tttttt.www.forum_qa_app.value.MyProfile

class FragmentMyPage : Fragment() {

    companion object {
        val TAG = "MyPage"
    }

    private lateinit var binding: FragmentMyPageBinding

    private val dataMember: DataMember =
        DataMember( "まさお丸", 30, 1, 8, "水戸市" )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textName.text = dataMember.name
        val addr = MyAddress().getPrefText(dataMember.pref)
        val gender = MyProfile().getGenderText(dataMember.gender)
        binding.textDetail.text = "${dataMember.age}歳 / ${gender} / ${addr}"

        binding.buttonUserEdit.setOnClickListener {
            val intent = Intent(activity, ActivityMyProfile::class.java)
            intent.putExtra("dataMember", dataMember)
            startActivity(intent)
        }

        binding.buttonQuestionList.setOnClickListener {
            val intent = Intent(activity, ActivityMyQuestionList::class.java)
            startActivity(intent)
        }

        binding.buttonAnswerList.setOnClickListener {
            val intent = Intent(activity, ActivityMyAnswerList::class.java)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        if (activity != null) {
            requireActivity().title = getString(R.string.mypage_title)
        }
    }

    override fun onPause() {
        super.onPause()
        // Log.i(">> $TAG", "onPause")
    }

    /** メニュー **/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // getActivity().getMenuInflater().inflate(R.menu.setting, menu);
        inflater.inflate(R.menu.setting, menu)
    }





}
