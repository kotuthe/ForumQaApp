package net.tochinavi.www.tochinaviapp.value


/*
これは使わないようにする
 */
class TochinaviURL {
    private val DOMAIN_PUBLIC = "https://www.tochinavi.net"
    private val DOMAIN_TEST = "http://test.tochinavi.net"
    private val DOMAIN_TEST_BASIC = "http://tochi:navi@test.tochinavi.net"
    /**
     * 公開状況
     * @return false=公開, true=テスト
     */
    private val isTest: Boolean = false

    /*
    // 栃ナビ用のURL
    fun returnURL(path: String, basic: Boolean?): String {
        // 本環境
        var domain = DOMAIN_PUBLIC
        if (isTest) {
            // test環境
            domain = if (!basic!!) DOMAIN_TEST else DOMAIN_TEST_BASIC
        }
        return domain + path
    }

    // json通信用
    fun my_http_url_app(): String {
        return returnURL("/api/application/v4", false)
    }

    // パスワードリマインダー
    fun my_http_url_forget_pass(): String {
        return returnURL("/config/member/password/", true)
    }

    // 使い方
    fun my_http_url_how_to_app(): String {
        return returnURL("/api/application/v4/common/android/how_to_app.pdf", true)
    }

    // 運営会社
    fun my_http_url_company_top(): String {
        return "http://www.yamazen-net.co.jp/"
    }

    // プライバシーポリシー
    fun my_http_url_privacy_policy(): String {
        return returnURL("/rules/p_policy.shtml", true)
    }

    // 性別、誕生日なしの時対応
    fun my_http_url_member_info_update(): String {
        return returnURL("/mypage/myprofile/form/index.shtml", true)
    }

    // サイトTOP
    fun my_http_url_partner_top_web(): String {
        return returnURL("", true)
    }

    // 会員ページ
    fun my_http_url_member_detail(): String {
        return returnURL("/mypage/home/show.shtml?id=", true)
    }

    // クーポンページ
    fun my_http_url_coupon(): String {
        return returnURL("/spot/osusume/?id=", true)
    }

    // スポットページ
    fun my_http_url_spot_info(): String {
        return returnURL("/spot/home/?id=", true)
    }
    */
}
