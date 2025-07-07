package com.example.bookboard

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookboard.model.BookPost

class BookAdapter(
    private var bookList: List<BookPost>,
    private val onItemClick: (BookPost) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val bookTitle: TextView = view.findViewById(R.id.bookTitle)
        val bookReview: TextView = view.findViewById(R.id.bookAuthor) // using same ID for review text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = bookList.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.bookTitle.text = book.title
        holder.bookReview.text = book.review

        if (book.base64Image.isNotEmpty()) {
            val imageBytes = Base64.decode(book.base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.bookImage.setImageBitmap(bitmap)
        } else {
            holder.bookImage.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    fun updateList(newList: List<BookPost>) {
        bookList = newList
        notifyDataSetChanged()
    }
}