package com.example.readify

class ModelPdf {
    //tạo biến
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var desc:String = ""
    var categoryId:String = ""
    var url:String = ""
    var timestamp:Long = 0
    var viewCount:Long = 0
    var downloadCount:Long = 0

    //constructor rỗng
    constructor()

    //constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        desc: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewCount: Long,
        downloadCount: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.desc = desc
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewCount = viewCount
        this.downloadCount = downloadCount
    }



}