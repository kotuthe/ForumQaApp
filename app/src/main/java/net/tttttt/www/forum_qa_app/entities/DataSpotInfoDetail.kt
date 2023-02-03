package net.tttttt.www.forum_qa_app.entities

import net.tttttt.www.forum_qa_app.value.Constants

// title, value, arrow
class DataSpotInfoDetail(
    type: Constants.SPOT_BASIC_INFO_TYPE,
    title: String,
    value: String
) {

    var type: Constants.SPOT_BASIC_INFO_TYPE = Constants.SPOT_BASIC_INFO_TYPE.none
    var title: String = ""
    var value: String = ""

    companion object {
        val TAG = "DataSpotInfoDetail"
    }

    init {
        this.type = type
        this.title = title
        this.value = value
    }
}