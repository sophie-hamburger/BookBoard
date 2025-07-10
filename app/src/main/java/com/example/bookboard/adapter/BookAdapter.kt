package com.example.bookboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookboard.databinding.ItemBookBinding
import com.example.bookboard.model.BookPost
import com.squareup.picasso.Picasso
import java.io.File

class BookAdapter(
    private val onItemClick: (BookPost) -> Unit
) : ListAdapter<BookPost, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        private val binding: ItemBookBinding,
        private val onItemClick: (BookPost) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: BookPost) {
            binding.apply {
                tvTitle.text = post.title
                tvAuthor.text = post.author
                tvReview.text = post.review
                ratingBar.rating = post.rating
                
                // Load image if path exists
                if (post.imagePath.isNotEmpty()) {
                    val imageFile = File(post.imagePath)
                    if (imageFile.exists()) {
                        Picasso.get()
                            .load(imageFile)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(ivBookCover)
                        ivBookCover.visibility = android.view.View.VISIBLE
                    } else {
                        ivBookCover.visibility = android.view.View.GONE
                    }
                } else {
                    ivBookCover.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onItemClick(post)
                }
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<BookPost>() {
        override fun areItemsTheSame(oldItem: BookPost, newItem: BookPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookPost, newItem: BookPost): Boolean {
            return oldItem == newItem
        }
    }
}