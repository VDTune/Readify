package com.example.readify

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    //tạo các phương thức tĩnh để tái sử dụng
    companion object {

        fun formatTimestamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //lấy pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    val kb = bytes / 1024
                    val mb = kb / 10224
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} Bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pageTv: TextView?
        ){

            val TAG = "PDF_THUMBNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //set to pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError {t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            progressBar.visibility = View.INVISIBLE

                            if(pageTv !=null){
                                pageTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }


        fun loadCategory(categoryId: String, categoryTv: TextView){

            val ref = FirebaseDatabase.getInstance().getReference("Category")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //lấy thể loại
                        val category = "${snapshot.child("category").value}"

                        //set the loai
                        categoryTv.text =category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

        fun deleteBook(context: Context, bookId:String, bookUrl: String, bookTitle: String){
            /*  - context: được sử dụng khi yêu cầu vd: progressDialog/Toast
            *   - bookId: để xóa sách ở csdl
            *   - bookUrl: xóa sách ở firebase Storage
            *   - bookTitle: dùng hiển thị ở hộp thoại*/

            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook:  Đang xóa...")

            //hộp thoại tiến trình
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Chỉ một lát")
            progressDialog.setMessage("Xóa sách: $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Xóa từ Storage...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Xóa ở Storage thành công")
                    Log.d(TAG, "deleteBook: Đang xóa ở db...")

                    val ref = FirebaseDatabase.getInstance().getReference("Book")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Xóa thành công!",Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Xóa dữ liệu từ db thành côcng")
                        }
                        .addOnFailureListener {e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Xóa thất bại... Lỗi ${e.message}")
                            Toast.makeText(context,"Xóa thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {e ->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Xóa dữ liệu từ Storage thất bại... Lỗi ${e.message}")
                    Toast.makeText(context,"Xóa dữ liệu từ Storage thất bại... Lỗi: ${e.message}",Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementBookViewCount(bookId: String){
            val ref = FirebaseDatabase.getInstance().getReference("Book")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = "${snapshot.child(" viewCount ").value}"

                        if(viewsCount=="" || viewsCount=="null"){
                            viewsCount = "0"
                        }

                        val newViewCount = viewsCount.toLong() + 1

                        //setup data
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewCount"] =  newViewCount

                        val dbRef = FirebaseDatabase.getInstance().getReference("Book")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

}