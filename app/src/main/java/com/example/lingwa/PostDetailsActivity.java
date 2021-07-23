package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.models.Post;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class PostDetailsActivity extends AppCompatActivity {

    PostWrapper postWrapper;
    TextView tvPostDetailsUsername;
    TextView tvPostDetailsBody;
    TextView tvpostDetailsTimestamp;
    ImageView tvPostDetailsProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postWrapper = Parcels.unwrap(getIntent().getParcelableExtra("post"));

        tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        tvPostDetailsBody = findViewById(R.id.tvPostDetailsBody);
        tvPostDetailsProfile = findViewById(R.id.ivPostDetailsProfile);
        tvpostDetailsTimestamp = findViewById(R.id.tvPostDetailsTimestamp);

        tvPostDetailsUsername.setText(postWrapper.authorUsername);
        tvPostDetailsBody.setText(postWrapper.body);
        tvpostDetailsTimestamp.setText(Post.toReadableTimestamp(postWrapper.timestamp));

        if (postWrapper.authorProfilePictureUrl != null) {
            Glide.with(this)
                    .load(postWrapper.authorProfilePictureUrl)
                    .circleCrop()
                    .into(tvPostDetailsProfile);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_profile_picture)
                    .into(tvPostDetailsProfile);
        }

    }
}