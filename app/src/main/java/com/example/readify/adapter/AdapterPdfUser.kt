package com.example.readify.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.readify.filter.FilterPdfUser
import com.example.readify.MyApplication
import com.example.readify.R
import com.example.readify.activity.PdfDetailActivity
import com.example.readify.databinding.RowPdfUserBinding
import com.example.readify.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {
    private var context: Context

    public var pdfArrayList: ArrayList<ModelPdf>
    public var filterList: ArrayList<ModelPdf>


    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser((binding.root))
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //lấy data, set data, xử lí sự kiện

        //lay data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val desc = model.desc
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //chuyen doi timestamp
        val date = MyApplication.formatTimestamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descTv.text = desc
        holder.dateTv.text = date

//        loadUserDetail(model, holder)

        MyApplication.loadPdfFromUrlSinglePage(
            url,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        ) //không cần số trnag nên cứ bỏ qua

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        //xu li su kien click
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    private fun loadUserDetail(model: ModelPdf, holder: AdapterPdfUser.HolderPdfUser) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.titleTv.text = name

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getFilter(): Filter {
        if(filter == null ){
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //khởi tạo thành phần giao diện
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descTv = binding.descTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }


}