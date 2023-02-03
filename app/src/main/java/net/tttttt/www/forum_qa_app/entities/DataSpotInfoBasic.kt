package net.tttttt.www.forum_qa_app.entities

import net.tttttt.www.forum_qa_app.value.Constants
import java.io.Serializable

class DataSpotInfoBasic(
    type: Constants.SPOT_BASIC_INFO_TYPE,
    title: String
): Serializable {

    companion object {
        val TAG = "DataSpotInfoBasic"
    }

    var type: Constants.SPOT_BASIC_INFO_TYPE = Constants.SPOT_BASIC_INFO_TYPE.none
    var title: String = ""

    init {
        this.type = type
        this.title = title
    }

}