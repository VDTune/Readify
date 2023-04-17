package com.example.readify

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.readify.databinding.ActivityCategoryAddBinding
import com.example.readify.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {
    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    // constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size //Hiển thị số lượng item
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        /*lất,thiết lập dữ liệu, xử lí sự kiện*/

        //lay du lieu
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.categoryTv.text = category

        //xu li nut xoa
        holder.deleteBtn.setOnClickListener {
            //xác nhận trước khi xóa
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Xóa")
                .setMessage("Xóa thể loại này?")
                .setPositiveButton("Xóa") { a, d ->
                    Toast.makeText(context, "Đang xóa...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Hủy") { a, d ->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //lấy id thể loại cần xóa
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Không thể xóa... Lỗi: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //khởi tạo giao diện người dùng
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}