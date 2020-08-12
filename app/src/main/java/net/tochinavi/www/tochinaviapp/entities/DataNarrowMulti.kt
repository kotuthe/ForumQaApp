package net.tochinavi.www.tochinaviapp.entities

class DataNarrowMulti(
    id: Int,
    name: String,
    parent_id: Int,
    checked: Boolean
) {
    var id: Int = 0
    var name: String = ""
    var parent_id: Int = 0
    var checked: Boolean = false

    companion object {
        val TAG = "DataNarrowMulti"
    }

    init {
        this.id = id
        this.name = name
        this.parent_id = parent_id
        this.checked = checked
    }
}