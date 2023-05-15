package com.example.readify.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.readify.MyApplication
import com.example.readify.R
import com.example.readify.databinding.RowCommentBinding
import com.example.readify.model.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment> {

    //context
    val context: Context

    //arraylist chứ bình luận
    val commentArrayList: ArrayList<ModelComment>

    //viewbinding
    private lateinit var binding: RowCommentBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    //constructor
    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        //khởi tạo firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        //lấy dữ liệu
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        //format ngày
        val date = MyApplication.formatTimestamp(timestamp.toLong())


        //set dữ liệu
        holder.dateTv.text = date
        holder.commentTv.text = comment

        loadUserDetail(model, holder)

        //xử lí click, hiện thông báo xác nhận xóa
        holder.itemView.setOnClickListener {
            //điều kiện để xóa bình luận
            //1. phải đăng nhập
            //2. uid cmt muốn xóa phải trùng với uid người dùng hiện tại
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid){
                deleteCommentDialog(model, holder)
            }

        }

    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        //hộp thoại thông báo
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Xóa bình luận")
            .setMessage("Bạn có muốn xóa bình luận này?")
            .setPositiveButton("XÓA"){d, e ->

                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Book")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Bình luận đã được xóa!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Xóa thất bại... Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("HỦY"){d,e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetail(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    }catch (e: Exception){
                        //trường hợp ảnh profile trống -> set ảnh mặt định
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    //viewHolder
    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView){
        //khởi tạo UI cho thằng row_comment
        val profileIv: ImageView = binding.profileIv
        val nameTv: TextView = binding.nameTv
        val dateTv: TextView = binding.dateTv
        val commentTv: TextView = binding.commentTv

    }


}