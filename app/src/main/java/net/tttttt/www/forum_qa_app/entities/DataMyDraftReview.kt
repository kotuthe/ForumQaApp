package net.tttttt.www.forum_qa_app.entities

import java.io.Serializable

class DataMyDraftReview(
    id: Int,
    isDraft: Boolean, // false: 投稿済み, true: 下書き
    spotId: Int,
    spotName: String,
    type: Int, // クチコミのタイプ: スポット, エリア, イベント
    reviewDate: String,
    review: String,
    reviewImageUrls: ArrayList<String>,
    goodNum: Int,
    // 下書き用　
    photoFlag: Boolean, // 画像投稿の許可フラグ
    comingFlg: Array<Int>,
    tagIds: ArrayList<DataReviewTag>
): Serializable {

    companion object {
        val TAG = "DataMyReview"
    }

    var id: Int = 0
    var isDraft: Boolean = false
    var spotId: Int = 0
    var spotName: String = ""
    var type: Int = 0
    var reviewDate: String = ""
    var review: String = ""
    var reviewImageUrls: ArrayList<String> = arrayListOf()
    var goodNum: Int = 0
    var photoFlag: Boolean = false
    var comingFlg: Array<Int> = Array(3) {0}
    var tagIds: ArrayList<DataReviewTag> = arrayListOf()

    init {
        this.id = id
        this.isDraft = isDraft
        this.spotId = spotId
        this.spotName = spotName
        this.type = type
        this.reviewDate = reviewDate
        this.review = review
        this.reviewImageUrls = reviewImageUrls
        this.goodNum = goodNum
        this.photoFlag = photoFlag
        this.comingFlg = comingFlg
        this.tagIds = tagIds
    }

}