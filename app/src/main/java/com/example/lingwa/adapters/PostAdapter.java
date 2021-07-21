package com.example.lingwa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.lingwa.R;
import com.example.lingwa.models.Content;
import com.parse.ParseFile;

import java.util.List;

import me.samlss.broccoli.Broccoli;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    Context context;
    List<Content> contentPosts;
    AdapterCallback callback;

    public PostAdapter(Context context, List<Content> contentPosts, AdapterCallback callback) {
        this.context = context;
        this.contentPosts = contentPosts;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Content content = contentPosts.get(position);
        holder.bind(content);
    }

    @Override
    public int getItemCount() {
        return contentPosts.size();
    }

    public void clear() {
        contentPosts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Content> list) {
        contentPosts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivContentImage;
        TextView tvContentDescription;
        TextView tvContentTitle;
        TextView tvContentAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this::onClick);
            ivContentImage = itemView.findViewById(R.id.ivContentImage);
            tvContentDescription = itemView.findViewById(R.id.tvContentDescription);
            tvContentTitle = itemView.findViewById(R.id.tvContentTitle);
            tvContentAuthor = itemView.findViewById(R.id.tvContentAuthor);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                final Content content = contentPosts.get(position);
                callback.onPostSelected(position, content);
            }

        }

        public void bind(Content content) {
            ParseFile contentImage = content.getThumbnail();

            if (contentImage != null) {
                Glide.with(context)
                        .load(contentImage.getUrl())
                        .transform(new RoundedCorners(10))
                        .into(ivContentImage);
            }

            tvContentTitle.setText(content.getTitle());
            tvContentAuthor.setText(content.getAuthor());
            tvContentDescription.setText(content.getDescription());
        }
    }

    public interface AdapterCallback {
        void onPostSelected(int position, Content content);
    }
}
