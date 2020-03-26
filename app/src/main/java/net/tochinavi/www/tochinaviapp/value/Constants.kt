package net.tochinavi.www.tochinaviapp.value

object Constants {

    /**
     * 通信
     */
    enum class HTTP_STATUS {
        nodata,
        network
    }

    /**
     * タブ設定値
     */
    enum class TAB_ITEM {
        TOP,
        NEIGHBOR,
        SEARCH,
        RANKING,
        MY_PAGE
    }

    /**
     * 基本情報のタイプ
     */
    enum class SPOT_BASIC_INFO_TYPE {
        none, // 特に意味はなし
        address,
        phone,
        holiday,
        hour,
        coupon,
        more_detail
    }

}