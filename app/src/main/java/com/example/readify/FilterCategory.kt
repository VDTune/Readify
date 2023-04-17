package com.example.readify

import android.widget.Filter


class FilterCategory : Filter {
    private var filterList: ArrayList<ModelCategory>

    private var adapterCategory: AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory):super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //kiểm tra giá trị có null hoặc rỗng hay không
        if (constraint != null && constraint.isNotEmpty()) {
            /*điều kiện tìm kiếm khi khác null và rỗng*/
            //set uppercase để tránh phân biệt chữ hoa chứx thường
            constraint = constraint.toString().uppercase()
            val filteredModel: ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(constraint)) {
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        } else {
            /*điều kiện tìm kiếm khi rỗng*/
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterCategory.categoryArrayList = results?.values as ArrayList<ModelCategory>

        //thông báo thay đổi
        adapterCategory.notifyDataSetChanged()
    }


}