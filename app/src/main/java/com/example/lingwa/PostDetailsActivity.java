package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.models.Post;
import com.example.lingwa.util.PostInteractions;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";
    int position;
    PostWrapper postWrapper;
    Post post;
    TextView tvPostDetailsUsername;
    TextView tvPostDetailsBody;
    TextView tvPostDetailsTimestamp;
    TextView tvDetailsLikeCount;
    ImageButton ibPostDetailsLike;

    ImageView tvPostDetailsProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postWrapper = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        position = getIntent().getIntExtra("position", 0);

        tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        tvPostDetailsBody = findViewById(R.id.tvPostDetailsBody);
        tvPostDetailsProfile = findViewById(R.id.ivPostDetailsProfile);
        tvPostDetailsTimestamp = findViewById(R.id.tvPostDetailsTimestamp);
        tvDetailsLikeCount = findViewById(R.id.tvDetailsLikeCount);
        ibPostDetailsLike = findViewById(R.id.ibPostDetailsLike);

        tvPostDetailsUsername.setText(postWrapper.authorUsername);
        tvPostDetailsBody.setText(postWrapper.body);
        tvPostDetailsTimestamp.setText(Post.toReadableTimestamp(postWrapper.timestamp));
        tvDetailsLikeCount.setText(String.format(Locale.ENGLISH, "%d", postWrapper.numLikes));

        ibPostDetailsLike.setSelected(postWrapper.liked);
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.include(Post.KEY_AUTHOR);
        postQuery.whereEqualTo(Post.KEY_OBJECT_ID, postWrapper.objectId);

        try {
            post = postQuery.getFirst();
            PostInteractions interactionsHandler = new PostInteractions(post);
            ibPostDetailsLike.setOnClickListener(interactionsHandler.onLikeButtonClicked);
            post.liked = postWrapper.liked;
        } catch (ParseException e) {
            Log.e(TAG, "Error getting post: " + e.toString());
        }

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        PostWrapper postWrapper = PostWrapper.fromPost(post);
        intent.putExtra("postWrapper", Parcels.wrap(postWrapper));
        intent.putExtra("position", position);
        setResult(RESULT_OK, intent);
        finish();
    }
}