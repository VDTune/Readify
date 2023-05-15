package com.example.readify.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.readify.Constants
import com.example.readify.MyApplication
import com.example.readify.R
import com.example.readify.databinding.ActivityPdfDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {
    //binding
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //bookId
    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""

    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    //show tien trinh
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lấy id sách từ intent
        bookId = intent.getStringExtra("bookId")!!

        //khởi tạo progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Chỉ một chút...")
        progressDialog.setCanceledOnTouchOutside(false)

        //khởi tạo firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        //tăng số lượt xem khi bắt đầu trang pdf detail
        MyApplication.incrementBookViewCount(bookId)

        //load chi tiết
        loadBookDetails()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        var readBook = findViewById<Button>(R.id.readBookBtn)
        readBook.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }
        //xử lí nút download
        binding.downloadBookBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: Storage permission đã được cấp phép")

            } else {
                Log.d(
                    TAG,
                    "onCreate: Storage permission không được cấp phép, hãy yêu cầu quyền truy cập "
                )
                requesStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //xử lí nút thích
        binding.favoriteBtn.setOnClickListener {
            //kiểm tra người dùng đã đăng kí hay chưa
            if(firebaseAuth.currentUser == null){
                //người dùng chưa đăng nhập
                Toast.makeText(this, "Bạn chưa đăng nhập!",Toast.LENGTH_SHORT).show()
            }else{
                //người dùng đã đăng nhập và có thể thích sách
                if(isInMyFavorite){
                    //đã được thích, chỉ bỏ thích
                    MyApplication.removeFromFavorite(this, bookId)
                }else {
                    //chưa được thích, chỉ được thích
                    addToFavorite()
                }
            }
        }
    }

    private val requesStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            //kiểm tra xem có được cấp hay không (Granted or not)
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
                Toast.makeText(this, "Quyền truy cập bị từ chối", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Downloading Book")
        progressDialog.setMessage("Đang tải sách...")
        progressDialog.show()

        //tải sách về từ firebase storage bằng url
        val storageReference = FirebaseStorage.getInstance().getReference(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveToDownloadFolder(bytes)
                Log.d(TAG, "downloadBook: Sách đã được tải...")
                Toast.makeText(this, "Sách đã được tải...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "downloadBook: Tải thất bại... Lỗi: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Tải sách thất bại... Lỗi: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadFolder: Saving downloaded book")

        val nameWithExtention = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs()

            val filePath = downloadFolder.path + "/" + nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Log.d(TAG, "saveToDownloadFolder: Lưu thành công")
            Toast.makeText(this, "Lưu vào mục Download", Toast.LENGTH_SHORT).show()

            progressDialog.dismiss()
            incrementDownloadCount()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadFolder: lưu thất bại.. lỗi ${e.message}")
            Toast.makeText(this, "Lưu thất bại... Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        //tăng lượt tải về lên firebase
        Log.d(TAG, "incrementDownloadCount: ")

        //1. lấy lượt tải về đã có
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy lượt tải về
                    var downloadCount = "${snapshot.child("downloadCount").value}"

                    Log.d(TAG, "onDataChange: Lượt tải về hiện tại: $downloadCount")

                    if (downloadCount == "" || downloadCount == "null") {
                        downloadCount = "0"
                    }
                    //chuyển đổi thành kiểu Long và +1
                    val newDownloadCount: Long = downloadCount.toLong() + 1
                    Log.d(TAG, "onDataChange: Lượt tải về mới: $newDownloadCount")

                    //setup data
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadCount"] = newDownloadCount

                    //2.Cập nhật lượt tải lên db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Book")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Tăng lượt tải thành công!!")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Tăng lượt tải thất bại... Loi: ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lay du lieu
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val desc = "${snapshot.child("desc").value}"
                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewCount = "${snapshot.child("viewCount").value}"

                    //format date
                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage(
                        "${bookUrl}",
                        "${bookTitle}",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    MyApplication.loadPdfSize(bookUrl, bookTitle, binding.sizeTv)

                    //set data
                    binding.titleTv.text = "${bookTitle}"
                    binding.descTv.text = desc
                    binding.viewsTv.text = viewCount
                    binding.downloadsTv.text = downloadCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: kiểm tra yêu thích")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if(isInMyFavorite){
                        //sách đã được thích
                        Log.d(TAG, "onDataChange: sách đã được thích")

                        //đổi icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_white,0,0)
                        binding.favoriteBtn.text = "Bỏ thích"
                    }else{
                        //Sách chưa được thích
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_border_white,0,0)
                        binding.favoriteBtn.text = "Thích"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite(){
        Log.d(TAG, "addToFavorite: Đang thêm vào yêu thích...")
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //luu thông tin sách được yêu thích vào firebase
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //thành công thêm vào yêu thích
                Log.d(TAG, "addToFavorite: ĐÃ THÊM VÀO YÊU THÍCH")

            }
            .addOnFailureListener {e ->
                Log.d(TAG, "addToFavorite: thêm vào yêu thích thất bại... lỗi: ${e.message}")
                Toast.makeText(this, "Lỗi khi thêm vào yêu thích... Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}