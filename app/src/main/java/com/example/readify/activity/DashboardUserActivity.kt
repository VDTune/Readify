package com.example.readify.activity
import com.example.readify.activity.ProfileActivity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.readify.BooksUserFragment
import com.example.readify.R
import com.example.readify.databinding.ActivityDashboardUserBinding

import com.example.readify.model.ModelCategory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        //xử lí nút đăng xuất
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            signOutGoogle()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        }

        //xử lí nút hiển thị profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        //nút chuyển tới đăng nhập khi chưa có tài khoản
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                //them data vao model
                val modelAll = ModelCategory("01", "Tất cả", 1, "")
                val modelMostViewed = ModelCategory("01", "Theo lượt xem", 1, "")
//                val modelMostDownloaded = ModelCategory("01", "Theo lượt tải", 1, "")

                //them vao lis
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
//                categoryArrayList.add(modelMostDownloaded)

                //them vao viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
//                viewPagerAdapter.addFragment(
//                    BooksUserFragment.newInstance(
//                        "${modelMostDownloaded.id}",
//                        "${modelMostDownloaded.category}",
//                        "${modelMostDownloaded.uid}"
//                    ), modelMostDownloaded.category
//                )
                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //tai xuong tu firebase
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)

                    //them vao list
                    categoryArrayList.add(model!!)

                    //them vao viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    //resfresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentList.add(fragment)

            fragmentTitleList.add(title)
        }

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //chưa đăng nhặp, người dùng có thể ở lại user dashboard khi chưa đăng nhâp
            binding.subTitleTv.text = "Bạn chưa đăng nhập"

            //ẩn nút đăng xuất và xem profile khi chưa đăng nhập
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
            binding.loginBtn.visibility = View.VISIBLE
//            binding.subTitleTv.visibility = View.GONE
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = "${snapshot.child("name").value}"
                        binding.titleTv.text = name
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            //lấy email
            val email = firebaseUser.email
            //Hiện email người dùng bằng text view
            binding.subTitleTv.text = email

            //hiển thị nút đăng xuất và xem profile khi đăng nhập
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
            binding.loginBtn.visibility = View.GONE
        }

    }

    private fun signOutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}