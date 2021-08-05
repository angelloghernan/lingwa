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
import com.example.lingwa.wrappers.ContentWrapper;
import com.parse.ParseFile;
import com.parse.fcm.ParseFCM;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import nl.siegmann.epublib.domain.Book;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    List<ContentWrapper> bookList;
    Context context;
    AdapterCallback callback;

    public BookAdapter(Context context, List<ContentWrapper> bookList, AdapterCallback callback) {
        this.context = context;
        this.bookList = bookList;
        this.callback = callback;
    }

    @NonNull
    @NotNull
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BookAdapter.ViewHolder holder, int position) {
        ContentWrapper bookWrapper = bookList.get(position);
        holder.bind(bookWrapper);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void clear() {
        bookList.clear();
    }

    public void addAll(List<ContentWrapper> bookList) {
        this.bookList.addAll(bookList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvBookTitle;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            itemView.setOnClickListener(this::onClick);
        }

        public void bind(ContentWrapper bookWrapper) {
            tvBookTitle.setText(bookWrapper.title);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                ContentWrapper bookWrapper = bookList.get(getAdapterPosition());
                Content book = Content.createWithoutData(Content.class, bookWrapper.objectId);
                callback.onBookSelected(position, book);
            }
        }
    }

    public interface AdapterCallback {
        void onBookSelected(int position, Content book);
    }
}
