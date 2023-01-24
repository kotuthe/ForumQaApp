package net.tttttt.www.forum_qa_app.value

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.net.URLEncoder

/**
 * 電話やメールなどのIntentを使った共通機能
 */
class MyIntent {

    init {
    }

    /**
     * ブラウザへ
     */
    fun web_browser(url: String): Intent {
        val uri = Uri.parse(url)
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /**
     * 電話
     */
    fun phone(number: String): Intent {
        val uri = Uri.parse("tel:%s".format(number))
        return Intent(Intent.ACTION_DIAL, uri)
    }

    /**
     * メール
     */
    fun mail(text: String): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        return intent
    }

    /**
     * LINE
     */
    fun line(text: String, thenPart: (Intent) -> Unit, elsePart: () -> Unit) {
        var uri: String? = "line://msg/text/"
        try {
            uri += URLEncoder.encode(text, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        try {
            val intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
            thenPart(intent)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: ActivityNotFoundException) {
            // Lineのインストールが必要
            elsePart()
        }
    }

}