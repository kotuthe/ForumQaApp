package net.tttttt.www.forum_qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import net.tttttt.www.forum_qa_app.databinding.ActivityOfficialSupportBinding
import net.tttttt.www.forum_qa_app.value.MyIntent

class ActivityOfficialSupport : AppCompatActivity() {

    private lateinit var binding: ActivityOfficialSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOfficialSupportBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (supportActionBar != null) {
            supportActionBar!!.title = "運営の応援"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.textNote.text = "「〇〇〇〇」0000年00月00日に、弊社〇〇〇〇が掲載されましたのでお知らせいたします。\n" +
                "〇〇〇〇や〇〇〇〇などの〇〇掲載内容〇〇をご紹介いただきました。\n" +
                "\n" +
                " \n" +
                "\n" +
                "「動画を視聴する」を是非ご覧ください。"

        binding.buttonUserEdit.setOnClickListener {
            startActivity(
                MyIntent().web_browser("https://www.youtube.com/watch?v=yEvSAU-qWrg"))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}