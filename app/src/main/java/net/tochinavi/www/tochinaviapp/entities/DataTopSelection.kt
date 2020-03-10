package net.tochinavi.www.tochinaviapp.entities

class DataTopSelection(
    key: Int,
    value: String,
    checke: Boolean
) {

    companion object {
        val TAG = "DataTopSelection"
    }

    var key: Int = 0
    var value: String = ""
    var checke: Boolean = false

    init {
        this.key = key
        this.value = value
        this.checke = checke
    }
}