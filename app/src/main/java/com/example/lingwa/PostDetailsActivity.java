package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.adapters.PostAdapter;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.Post;
import com.example.lingwa.util.ParseApplication;
import com.example.lingwa.util.PostInteractions;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.sanju.motiontoast.MotionToast;

public class PostDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";
    private int position;

    Context context;
    PostWrapper postWrapper;
    Post post;

    List<Post> commentList;
    PostAdapter adapter;

    RecyclerView rvComments;
    TextView tvPostDetailsUsername;
    TextView tvPostDetailsBody;
    TextView tvPostDetailsTimestamp;
    TextView tvDetailsLikeCount;
    ImageButton ibPostDetailsLike;
    Button btnSubmitComment;
    EditText etCommentBody;
    ProgressBar pbComments;

    ImageView ivPostDetailsProfile;
    ImageView ivPDCurrentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        context = this;

        postWrapper = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        position = getIntent().getIntExtra("position", 0);

        // Initialize views
        rvComments = findViewById(R.id.rvComments);
        tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        tvPostDetailsBody = findViewById(R.id.tvPostDetailsBody);
        ivPostDetailsProfile = findViewById(R.id.ivPostDetailsProfile);
        tvPostDetailsTimestamp = findViewById(R.id.tvPostDetailsTimestamp);
        tvDetailsLikeCount = findViewById(R.id.tvDetailsLikeCount);
        ibPostDetailsLike = findViewById(R.id.ibPostDetailsLike);
        ivPDCurrentUserProfile = findViewById(R.id.ivPDCurrentUserProfile);
        etCommentBody = findViewById(R.id.etCommentBody);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);
        pbComments = findViewById(R.id.pbComments);

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
            PostInteractions interactionsHandler = new PostInteractions(post, postWrapper.liked);
            ibPostDetailsLike.setOnClickListener(interactionsHandler.onLikeButtonClicked);
            post.liked = postWrapper.liked;
        } catch (ParseException e) {
            Log.e(TAG, "Error getting post: " + e.toString());
        }

        if (postWrapper.authorProfilePictureUrl != null) {
            Glide.with(this)
                    .load(postWrapper.authorProfilePictureUrl)
                    .circleCrop()
                    .into(ivPostDetailsProfile);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_profile_picture)
                    .into(ivPostDetailsProfile);
        }

        String userProfileUrl;
        try {
            userProfileUrl = ParseUser.getCurrentUser().getParseFile("profilePicture").getUrl();
        } catch (NullPointerException e) {
            userProfileUrl = null;
        }

        if (userProfileUrl != null) {
            Glide.with(this)
                    .load(userProfileUrl)
                    .circleCrop()
                    .into(ivPDCurrentUserProfile);
        } else {
            Glide.with(this)
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivPDCurrentUserProfile);
        }

        // Set up recycler view, fetch comments to populate recycler view
        commentList = new ArrayList<>();
        adapter = new PostAdapter(context, null, commentList, callback);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(context));
        fetchComments();

        btnSubmitComment.setOnClickListener(makeComment);
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

    private void fetchComments() {
        ParseQuery<Post> commentQuery = ParseQuery.getQuery(Post.class);
        commentQuery.include(Post.KEY_AUTHOR);
        commentQuery.addDescendingOrder(Post.KEY_CREATED_AT);
        commentQuery.whereEqualTo(Post.KEY_REPLYING_TO,
                ParseObject.createWithoutData(Post.class, postWrapper.objectId));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                commentList = commentQuery.find();
                for (int i = 0; i < commentList.size(); i++) {
                    Post comment = commentList.get(i);
                    comment.liked = comment.isLikedByUser(ParseUser.getCurrentUser());
                }
                adapter.clearPosts();
                adapter.addAllPosts(commentList);
            } catch (ParseException e) {
                Log.e(TAG, "error fetching comments: " + e.toString());
                return;
            }

            handler.post(() -> {
                adapter.notifyDataSetChanged();
                pbComments.setVisibility(View.INVISIBLE);
            });

        });
    }

    View.OnClickListener makeComment = v -> {
        String body = etCommentBody.getText().toString();
        if (body.equals("")) {
            return;
        }
        Post comment = new Post(body, ParseUser.getCurrentUser(), post, 0 , 0);
        comment.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "error posting comment: " + e.toString());
                return;
            }
            etCommentBody.setText("");
            post.setNumComments(post.getNumComments() + 1);

            ParseApplication.createPushNotification(
                    ParseUser.getCurrentUser().getUsername() + " commented on your post",
                    "New Comment",
                    post.getAuthor());

            MotionToast.Companion.createColorToast((Activity) context,
                    "Success",
                    "Comment posted!",
                    MotionToast.TOAST_SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context, R.font.helvetica_regular));

            commentList.add(0, comment);
            adapter.addPost(0, comment);
            adapter.notifyItemInserted(0);
            rvComments.scrollToPosition(0);
        });
    };

    // currently unused
    PostAdapter.AdapterCallback callback = new PostAdapter.AdapterCallback() {
        @Override
        public void onContentSelected(int position, Content content) { }
        @Override
        public void onPostSelected(int position, Post post) { }
    };
}