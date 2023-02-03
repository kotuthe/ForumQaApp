package net.tttttt.www.forum_qa_app.entities

import java.io.Serializable

class DataMyReview(
    id: Int,
    type: Int,
    spotId: Int,
    spotName: String,
    spotImage: String,
    userId: Int,
    userName: String,
    userImage: String,
    userInfo: String,
    reviewDate: String,
    review: String,
    reviewImageUrls: ArrayList<String>,
    reviewUrl: String,
    goodNum: Int,
    enableGood: Boolean
): Serializable {

    companion object {
        val TAG = "DataSpotList"
    }

    var id: Int = 0
    var type: Int = 0
    var spotId: Int = 0
    var spotName: String = ""
    var spotImage: String = ""
    var userId: Int = 0
    var userName: String = ""
    var userImage: String = ""
    var userInfo: String = ""
    var reviewDate: String = ""
    var review: String = ""
    var reviewImageUrls: ArrayList<String> = arrayListOf()
    var reviewUrl: String = ""
    var goodNum: Int = 0
    var enableGood: Boolean = false

    init {
        this.id = id
        this.type = type
        this.spotId = spotId
        this.spotName = spotName
        this.spotImage = spotImage
        this.userId = userId
        this.userName = userName
        this.userImage = userImage
        this.userInfo = userInfo
        this.reviewDate = reviewDate
        this.review = review
        this.reviewImageUrls = reviewImageUrls
        this.reviewUrl = reviewUrl
        this.goodNum = goodNum
        this.enableGood = enableGood
    }

}