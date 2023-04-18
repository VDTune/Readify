package com.example.readify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.readify.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {
    //binding
    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //id, tên thể loại
    private var categoryId = ""
    private var category = ""

    //arraylist chứa sách
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //lấy từ intent
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //đặt thể loại pdf
        binding.subTitleTv.text = category

        //tải lên pdf
        loadPdfList()

        //tim kiem
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfAdmin.filter?.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        //xử lý nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadPdfList() {
        //khởi tạo araylist
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear danh sach truoc khi them vao
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //Lấy dữ liệu nè
                        val model = ds.getValue(ModelPdf::class.java)

                        //thêm vào danh sách nè
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange:${model.title} ${model.categoryId} ")
                        }

                    }

                    //setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.bookRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}