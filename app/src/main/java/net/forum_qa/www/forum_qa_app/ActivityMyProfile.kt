package net.forum_qa.www.forum_qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.tttttt.www.forum_qa_app.R
import net.tttttt.www.forum_qa_app.entities.DataMember

class ActivityMyProfile : AppCompatActivity() {

    private lateinit var dataMember: DataMember

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        dataMember = intent.getSerializableExtra("dataMember") as DataMember

    }
}