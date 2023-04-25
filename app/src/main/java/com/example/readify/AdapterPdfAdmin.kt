package com.example.readify

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.readify.databinding.RowPdfAdminBinding

class AdapterPdfAdmin:RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private var context:Context

    //array list để chứa file pdf
    public var pdfArrayList: ArrayList<ModelPdf>
    private val filterList: ArrayList<ModelPdf>

    //binding
    private lateinit var binding:RowPdfAdminBinding

    //filter object
    private var filter: FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //lấy dữ liệu
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val desc = model.desc
        val pdfUrl = model.url
        val timestamp = model.timestamp

        val formattedDate = MyApplication.formatTimestamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descTv.text = desc
        holder.dateTv.text = formattedDate

        //lấy thể loại nè
        MyApplication.loadCategory(categoryId = categoryId, holder.categoryTv)

        //không cần số trang nên set null cho pages
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        //lấy size pdf nè
        MyApplication.loadPdfSize(pdfUrl,title, holder.sizeTv)

        //xử lí sự kiện click: xóa sách và edit sách
        holder.moreBtn.setOnClickListener {
            moreOptionDialog(model, holder)
        }
        //xử lí click hiển thị PdfDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionDialog(model: ModelPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {
        //lấy tên, id , url của sách
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        val options = arrayOf("Sửa", "Xóa")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Tùy chọn")
            .setItems(options){dialog, position ->
                if(position == 0){
                    //sửa
                    val intent = Intent(context,PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                }
                else if(position == 1){
                    //xóa (tạo hàm trong lớp MyApplication
                    MyApplication.deleteBook(context,bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfAdmin(filterList,this)

        }

        return filter as FilterPdfAdmin
    }
    //view holder
    inner class HolderPdfAdmin(itemView: View): RecyclerView.ViewHolder(itemView){

        //UI view row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descTv = binding.descTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

}