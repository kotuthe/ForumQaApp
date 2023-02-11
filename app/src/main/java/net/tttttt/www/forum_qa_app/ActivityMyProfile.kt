package net.tttttt.www.forum_qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import net.tttttt.www.forum_qa_app.entities.DataMember
import kotlinx.android.synthetic.main.activity_my_profile.*
import net.tttttt.www.forum_qa_app.value.MyAddress
import net.tttttt.www.forum_qa_app.value.MyProfile

class ActivityMyProfile : AppCompatActivity() {

    private lateinit var dataMember: DataMember

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        if (supportActionBar != null) {
            supportActionBar!!.title = "プロフィール編集"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        dataMember = intent.getSerializableExtra("dataMember") as DataMember

        initLayout()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initLayout() {

        // 性別 //
        val gndAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            MyProfile().getGenderTexts()
        )
        gndAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = gndAdapter
        spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent!!.selectedItem as String
                Toast.makeText(applicationContext, "select ${item}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }

        // 住んでいる県 //
        val prfAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            MyAddress().getPrefectureTexts()
        )
        prfAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrefecture.adapter = prfAdapter
        spinnerPrefecture.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent!!.selectedItem as String
                Toast.makeText(applicationContext, "select ${item}", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }
    }



}