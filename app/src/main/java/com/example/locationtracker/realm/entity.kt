package com.example.locationtracker.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.Date


class UserInfo: RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var userName:String = ""
    var userEmail:String = ""
    var password:String = ""
}

class LocationInfo:RealmObject {
    var userId : String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var time : Long = Date().time
}