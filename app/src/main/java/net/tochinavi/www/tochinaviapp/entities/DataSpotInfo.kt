package net.tochinavi.www.tochinaviapp.entities

import java.io.Serializable

class DataSpotInfo(
    id: Int,
    imageUrl: String,
    moreImage: Boolean,
    name: String,
    address: String,
    phone: String,
    hour: String,
    holiday: String,
    simple_detail: String,
    simple_caption: String,
    latitude: Double,
    longitude: Double,
    category_large_id: Int,
    checkinEnable: Boolean,
    reviewEnable: Boolean,
    imageEnable: Boolean,
    bookMarkEnable: Boolean,
    snsShareText: String,
    snsShareTextLong: String,
    isFree: Int,
    coupon_enable: Boolean
): Serializable {

    companion object {
        val TAG = "DataSpotList"
    }

    var id: Int = 0
    var imageUrl: String = ""
    var moreImage: Boolean = false
    var name: String = ""
    var address: String = ""
    var phone: String = ""
    var hour: String = ""
    var holiday: String = ""
    var simple_detail: String = ""
    var simple_caption: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var category_large_id: Int = 0
    var checkinEnable: Boolean = false
    var reviewEnable: Boolean = false
    var imageEnable: Boolean = false
    var bookMarkEnable: Boolean = false
    var snsShareText: String = ""
    var snsShareTextLong: String = ""
    var isFree: Int = 0
    var coupon_enable: Boolean = false

    init {
        this.id = id
        this.imageUrl = imageUrl
        this.moreImage = moreImage
        this.name = name
        this.address = address
        this.phone = phone
        this.hour = hour
        this.holiday = holiday
        this.simple_detail = simple_detail
        this.simple_caption = simple_caption
        this.latitude = latitude
        this.longitude = longitude
        this.category_large_id = category_large_id
        this.checkinEnable = checkinEnable
        this.reviewEnable = reviewEnable
        this.imageEnable = imageEnable
        this.bookMarkEnable = bookMarkEnable
        this.snsShareText = snsShareText
        this.snsShareTextLong = snsShareTextLong
        this.isFree = isFree
        this.coupon_enable = coupon_enable
    }

}