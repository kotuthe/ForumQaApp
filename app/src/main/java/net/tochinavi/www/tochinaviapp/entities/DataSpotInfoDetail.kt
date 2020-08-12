package net.tochinavi.www.tochinaviapp.entities

import net.tochinavi.www.tochinaviapp.value.Constants

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