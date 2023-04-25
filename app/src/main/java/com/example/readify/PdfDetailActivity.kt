package com.example.readify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.readify.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailActivity : AppCompatActivity() {
    //binding
    private lateinit var binding: ActivityPdfDetailBinding

    //bookId
    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lấy id sách từ intent
        bookId = intent.getStringExtra("bookId")!!

        //tăng số lượt xem khi bắt đầu trang pdf detail
        MyApplication.incrementBookViewCount(bookId)

        //load chi tiết
        loadBookDetails()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lay du lieu
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val desc = "${snapshot.child("desc").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewCount = "${snapshot.child("viewCount").value}"

                    //format date
                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage("$url","$title",binding.pdfView, binding.progressBar, binding.pagesTv)

                    MyApplication.loadPdfSize("$url", "$title",binding.sizeTv)

                    //set data
                    binding.titleTv.text = title
                    binding.descTv.text = desc
                    binding.viewsTv.text = viewCount
                    binding.downloadsTv.text = downloadCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}