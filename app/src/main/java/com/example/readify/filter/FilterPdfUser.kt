package com.example.readify.filter

import android.widget.Filter
import com.example.readify.adapter.AdapterPdfUser
import com.example.readify.model.ModelPdf


class FilterPdfUser: Filter {
    var filterList: ArrayList<ModelPdf>

    var adapterPdfUse: AdapterPdfUser

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUse: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUse = adapterPdfUse
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val result = FilterResults()
        if (constraint != null && constraint.isNotEmpty()){

            constraint = constraint.toString().uppercase()
            val filteredModel = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                if (filterList[i].title.uppercase().contains(constraint)){
                    filteredModel.add(filterList[i])
                }
            }
            result.count = filteredModel.size
            result.values = filteredModel

        }else{
            result.count = filterList.size
            result.values = filterList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence, result: FilterResults) {
        adapterPdfUse.pdfArrayList = result.values as ArrayList<ModelPdf>

        adapterPdfUse.notifyDataSetChanged()
    }


}