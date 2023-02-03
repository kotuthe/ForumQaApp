package net.tttttt.www.forum_qa_app.entities

import net.tttttt.www.forum_qa_app.value.Constants
import java.io.Serializable

class DataMember(
    name: String,
    age: Int,
    gender: Int,
    pref: Int,
    city: String
): Serializable {

    companion object {
        val TAG = "DataMember"
    }

    var name: String = ""
    var age: Int = 0
    var gender: Int = 0
    var pref: Int = 0
    var city: String = ""

    init {
        this.name = name
        this.age= age
        this.gender = gender
        this.pref = pref
        this.city = city
    }

}