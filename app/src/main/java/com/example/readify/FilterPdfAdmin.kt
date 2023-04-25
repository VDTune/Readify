package com.example.readify

import android.widget.Filter

class FilterPdfAdmin : Filter {

    var filterList: ArrayList<ModelPdf>

    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin):super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint //gia tri tim kiem
        val results = FilterResults()

        //các giá trị tìm kiếm không được null và rỗng
        if(constraint !=null && constraint.isNotEmpty()){
            // chuyển thành lowercase để tránh phân biệt chữ hoa chứ thường
            constraint = constraint.toString().uppercase()
            val filteredModel: ArrayList<ModelPdf> = ArrayList()

            for(i in filterList.indices){
                //xác thực nếu gía trị khớp
                if(filterList[i].title.uppercase().contains(constraint)){
                    //giá trị tìm tương tự với các gía trị trong list
                    filteredModel.add(filterList[i])
                }
            }

            results.count = filteredModel.size
            results.values = filteredModel
        }
        else{
            //giá trị tìm kiếm là null hoặc rỗng, trả về tất cả giá trị của list
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        //apply filter
        adapterPdfAdmin.pdfArrayList = results?.values as ArrayList<ModelPdf>

        //thong bao
        adapterPdfAdmin.notifyDataSetChanged()
    }
}