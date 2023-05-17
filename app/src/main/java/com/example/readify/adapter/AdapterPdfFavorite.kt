package com.example.readify.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.disklrucache.DiskLruCache.Value
import com.example.readify.MyApplication
import com.example.readify.activity.PdfDetailActivity
import com.example.readify.databinding.RowPdfFavoriteBinding
import com.example.readify.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite: RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    //view binding
    private lateinit var binding: RowPdfFavoriteBinding

    //context
    private var context: Context

    //Arraylist cho sách
    private var bookArrayList: ArrayList<ModelPdf>

    //constructor
    constructor(context: Context, bookArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.bookArrayList = bookArrayList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        //lấy dữ liệu
        val model = bookArrayList[position]
        loadBookDetails(model, holder)

        //xử lí click khi click vào item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
        //xử lí bỏ thích
        holder.removeFavoriteBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Book")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy thông tin sách
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val desc = "${snapshot.child("desc").value}"
//                    val downloadCount = "${snapshot.child("downloadCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewCount = "${snapshot.child("viewCount").value}"

                    //đưa dữ liệu vào model
                    model.isFavorite = true
                    model.title = title
                    model.desc = desc
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewCount = viewCount.toLong()
//                    model.downloadCount = downloadCount.toLong()


                    //format ngày
                    val date = MyApplication.formatTimestamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId",holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descTv.text = desc
                    holder.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }
    //lớp view holder để quản lí UI của thằng row pdf fav
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        //khởi tạo UI
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavoriteBtn = binding.removeFavoriteBtn
        var descTv = binding.descTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv


    }



}