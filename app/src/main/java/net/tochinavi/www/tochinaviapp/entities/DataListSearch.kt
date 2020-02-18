package net.tochinavi.www.tochinaviapp.entities

class DataListSearch(
    icon: Int,
    title: String,
    subTitle: String
) {
    var icon: Int = 0
    var title: String = ""
    var subTitle: String = ""

    companion object {
        val TAG = "DataListSearch"
    }

    init {
        this.icon = icon
        this.title = title
        this.subTitle = subTitle
    }
}