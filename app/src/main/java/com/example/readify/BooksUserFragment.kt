package com.example.readify

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.readify.adapter.AdapterPdfUser
import com.example.readify.databinding.FragmentBooksUserBinding
import com.example.readify.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()

            //đưa data vào bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //lấy đối số đã truyène vào phương thức newInstance
        val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)
        Log.d(TAG, "onCreateView: Category: $category")
        if(category == "Tất cả"){
            //hiển thị tất cả sách
            loadAllBooks()
        }else{
            //hiển thị thể loại được chọn
            loadCategorizedBooks()
        }

        //tìm kiếm
        binding.searchEt.addTextChangedListener{object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: search exeption: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        }

        }
        return binding.root
    }

    private fun loadAllBooks() {
    //khoi tao list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list truoc khi nhap du lieu
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //them vao list
                    pdfArrayList.add(model!!)
                }

                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

//    private fun loadMostViewedDownloadBooks(orderBy: String) {
//        pdfArrayList = ArrayList()
//        val ref = FirebaseDatabase.getInstance().getReference("Book")
//        ref.orderByChild(orderBy).limitToLast(10) //hiển thị 10 sách xem hoặc tải nhiều nhất
//            .addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                //clear list truoc khi nhap du lieu
//                pdfArrayList.clear()
//                for(ds in snapshot.children){
//                    //get data
//                    val model = ds.getValue(ModelPdf::class.java)
//                    //them vao list
//                    pdfArrayList.add(model!!)
//                }
//
//                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
//                binding.booksRv.adapter = adapterPdfUser
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list truoc khi nhap du lieu
                    pdfArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //them vao list
                        pdfArrayList.add(model!!)
                    }

                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}