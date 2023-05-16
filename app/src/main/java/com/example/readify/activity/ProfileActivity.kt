package com.example.readify.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.readify.MyApplication
import com.example.readify.R
import com.example.readify.adapter.AdapterPdfFavorite
import com.example.readify.databinding.ActivityProfileBinding
import com.example.readify.model.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth

    //lấy người dùng hiện tại
    private lateinit var firebaseUser: FirebaseUser

    //arraylist để chứa dánh sách sách
    private lateinit var bookArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    //tiến trình
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBookCountTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = firebaseAuth.currentUser!!

        //khởi tạo tiến trình
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Đợi một lát...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfo()
        loadfavoriteBooks()

        //xử lí nút back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //xử lí nút sửa profile
        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        //xử lí click xác thực
        binding.accountStatusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified) {
                //đã xác thực
                Toast.makeText(this, "Tài khoản đã được xác thực", Toast.LENGTH_SHORT).show()
            } else {
                //chưa xác thực, show hộp thoại trước khi xác thực
                emailVerifiDialog()
            }
        }
    }

    private fun emailVerifiDialog() {
        //show hộp thoại xác nhận
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác thực Email")
            .setMessage("Bạn muốn nhận mã xác thực Email: ${firebaseUser.email}?")
            .setPositiveButton("GỬI") { d, e ->
                sendEmailVerification()
            }
            .setNegativeButton("HỦY") { d, e ->
                d.dismiss()
            }
            .show()

    }

    private fun sendEmailVerification() {
        //show tiến trình
        progressDialog.setMessage("Đang gửi xác thực đến Email: ${firebaseUser.email}")
        progressDialog.show()

        //gửi hướng dẫn
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                //gửi thành công
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Gửi xác thực thành công đến Email: ${firebaseUser.email}",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Gửi xác thực không thành công... Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadUserInfo() {
        //kiểm tra người dùng đã xác thực chưa
        if (firebaseUser.isEmailVerified) {
            binding.accountStatusTv.text = "Đã xác thực"
        } else {
            binding.accountStatusTv.text = "Chưa xác thực"
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //lấy thông tin người dùng
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //đổi timestamp
                    val formattedDate = MyApplication.formatTimestamp(timestamp.toLong())


                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //set ảnh
                    try {
                        Glide.with(this@ProfileActivity).load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadfavoriteBooks() {
        //khởi tạo arraylist
        bookArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookArrayList.clear()
                    for (ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"

                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        bookArrayList.add(modelPdf)
                    }
                    //số sách đã thích
                    binding.favoriteBookCountTv.text = "${bookArrayList.size}"

                    //setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, bookArrayList)

                    //set adapter cho recyclerview
                    binding.favoriteRv.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}