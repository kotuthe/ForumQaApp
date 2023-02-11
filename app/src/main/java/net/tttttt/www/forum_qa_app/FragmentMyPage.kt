package net.tttttt.www.forum_qa_app

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_my_page.*
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataMember
import net.tttttt.www.forum_qa_app.value.MyAddress
import net.tttttt.www.forum_qa_app.value.MyProfile

class FragmentMyPage : Fragment() {

    companion object {
        val TAG = "MyPage"
    }

    private val dataMember: DataMember =
        DataMember( "まさお丸", 30, 1, 8, "水戸市" )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textName.text = dataMember.name
        val addr = MyAddress().getPrefText(dataMember.pref)
        val gender = MyProfile().getGenderText(dataMember.gender)
        textDetail.text = "${dataMember.age}歳 / ${gender} / ${addr}"

        buttonUserEdit.setOnClickListener {
            val intent = Intent(activity, ActivityMyProfile::class.java)
            intent.putExtra("dataMember", dataMember)
            startActivity(intent)
        }

        buttonQuestionList.setOnClickListener {
            val intent = Intent(activity, ActivityMyQuestionList::class.java)
            startActivity(intent)
        }

        buttonAnswerList.setOnClickListener {
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
