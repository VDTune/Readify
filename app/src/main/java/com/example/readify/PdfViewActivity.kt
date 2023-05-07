package com.example.readify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.readify.databinding.ActivityPdfViewBinding
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    //binding
    private lateinit var binding: ActivityPdfViewBinding

    //TAG
    private companion object {
        const val TAG = "PDF_VIEW_TAG"
    }

    var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lấy book id từ intent
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadDetails: Lấy pdf từ db")

        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy url sách
                    val pdfUrl = snapshot.child(("url")).value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    //load pdf
                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Lấy pdf từ storage bằng url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes
                ->
                Log.d(TAG, "loadBookFromUrl: lấy url thành công")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        val toolbarSubTitle = findViewById<TextView>(R.id.toolbarSubTitleTv)
                        toolbarSubTitle.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")

                    }
                    .onError { t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: lấy url không thành công...Lỗi: ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}