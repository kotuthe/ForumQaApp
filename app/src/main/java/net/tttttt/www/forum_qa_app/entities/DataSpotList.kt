package net.tttttt.www.forum_qa_app.entities

import java.io.Serializable

class DataSpotList(
    id: Int,
    type: Int, // 1:スポット, それ以外:病院
    name: String,
    address: String,
    parent_category_id: Int,
    category: String,
    distance: String,
    review_num: Int,
    image_url: String,
    checkin_enable: Boolean,
    coupon_enable: Boolean,
    reviewImageNum: Int,
    favoriteNum: Int,
    reviewImageList: ArrayList<String>,
    latitude: Double,
    longitude: Double
): Serializable {

    companion object {
        val TAG = "DataSpotList"
    }

    var id: Int = 0
    var type: Int = 0
    var name: String = ""
    var address: String = ""
    var parent_category_id: Int = 0
    var category: String = ""
    var distance: String = ""
    var review_num: Int = 0
    var image_url: String = ""
    var checkin_enable: Boolean = false
    var coupon_enable: Boolean = false
    var reviewImageNum: Int = 0
    var favoriteNum: Int = 0
    var reviewImageList: ArrayList<String> = ArrayList()
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    init {
        this.id = id
        this.type = type
        this.name  = name
        this.address  = address
        this.parent_category_id = parent_category_id
        this.category  = category
        this.distance  = distance
        this.review_num = review_num
        this.image_url  = image_url
        this.checkin_enable = checkin_enable
        this.coupon_enable = coupon_enable
        this.reviewImageNum = reviewImageNum
        this.favoriteNum = favoriteNum
        this.reviewImageList = reviewImageList
        this.latitude = latitude
        this.longitude = longitude
    }
}