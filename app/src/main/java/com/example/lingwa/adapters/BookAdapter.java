package com.example.lingwa.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lingwa.R;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.Post;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import nl.siegmann.epublib.domain.Book;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    List<Content> bookList;
    Context context;
    AdapterCallback callback;

    public BookAdapter(Context context, List<Content> bookList, AdapterCallback callback) {
        this.context = context;
        this.bookList = bookList;
        this.callback = callback;
    }

    @NonNull
    @NotNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_book, parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BookAdapter.ViewHolder holder, int position) {
        Content book = bookList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvBookTitle;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
        }

        public void bind(Content book) {
            tvBookTitle.setText(book.getTitle());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Content book = bookList.get(getAdapterPosition());
                callback.onBookSelected(position, book);
            }
        }
    }

    public interface AdapterCallback {
        void onBookSelected(int position, Content book);
    }
}
