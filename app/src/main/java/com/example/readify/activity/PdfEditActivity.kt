package com.example.readify.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.readify.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {
    //binding
    private lateinit var binding: ActivityPdfEditBinding

    private companion object {
        private const val TAG = "EDIT_PDF_TAG"
    }

    //id sách lấy từ start intent AdapterPdfAdmin
    private var bookId = ""

    private var pdfUri: Uri? = null

    //dialog tiến trình
    private lateinit var progressDialog: ProgressDialog

    //arraylist để chứa tên thể loại
    private lateinit var categoryTitleArrayList: ArrayList<String>

    //arraylist để chứa id thể loại
    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Chỉ một lát")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //xử lí nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lí chọn thể loại
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //xử lí click pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //xử lí nút submit
        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book Info")

        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy thông tin sách
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val desc = snapshot.child("desc").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descEt.setText(desc)

                    Log.d(TAG, "onDataChange: loading book...")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Category")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //lay d lieu
                                val category = snapshot.child("category").value
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var desc = ""
    private fun validateData() {
        //lấy dữ liệu
        title = binding.titleEt.text.toString().trim()
        desc = binding.descEt.text.toString().trim()

        //xác thực dữ liệu
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show()
        } else if (desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thể loại", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Vui lòng chọn URI...", Toast.LENGTH_SHORT).show()}
        else {
            updatePdf()
        }

    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Bat dau cap nhat thong tin pdf...")

        //show tiến trình
        progressDialog.setMessage("Cập nhật thông tin sách...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        //setup data
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["desc"] = "$desc"
        hashMap["categoryId"] = "$selectedCategoryId"

        //bắt đầu cập nhật:
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updatePdf: Cập nhật thành công!")
                progressDialog.dismiss()
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updatePdf: Cập nhật thất bại... Lỗi: ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Cập nhật thất bại... Lỗi: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        //hiển thị danh sách chọn thể loại

        //tạo mảng string từ arraylist
        val categoryArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoryArray[i] = categoryTitleArrayList[i]
        }

        //hop thoai thong bao
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn Thể Loại")
            .setItems(categoryArray) { dialog, position ->
                //xử lí click chọn thể loại
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //đưa giá trị ra textview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading category...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list trước khi thêm dữ liệu vào
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category Title $category")

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
                val fileName = getFileName(pdfUri)
                binding.pdfTitle.setText("$fileName")
            } else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Hủy bỏ", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun getFileName(uri: Uri?): String? {
        var result: String? = null
        if (uri != null) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
                it.close()
            }
        }
        return result
    }
}