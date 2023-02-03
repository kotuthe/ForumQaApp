package net.tttttt.www.forum_qa_app.entities

/*
rank:    順位
iconUrl: ユーザアイコン
id:      会員ID
name:    ユーザー名
detail:  ユーザー詳細
result:  件数
 */
class DataRanking(
    rank: Int,
    iconUrl: String,
    id: Int,
    name: String,
    detail: String,
    result: String
) {

    companion object {
        val TAG = "DataRanking"
    }

    var rank: Int = 0
    var iconUrl: String = ""
    var id: Int = 0
    var name: String = ""
    var detail: String = ""
    var result: String = ""

    init {
        this.rank = rank
        this.iconUrl = iconUrl
        this.id = id
        this.name = name
        this.detail = detail
        this.result = result
    }
}