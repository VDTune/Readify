package com.example.readify.model

class ModelComment {

    //khởi tạo các biến
    var id = ""
    var bookId= ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    //constructor
    constructor()

    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}