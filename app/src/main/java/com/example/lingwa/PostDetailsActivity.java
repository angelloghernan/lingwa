package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.models.Comment;
import com.example.lingwa.models.Post;
import com.example.lingwa.util.PostInteractions;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.Locale;

import www.sanju.motiontoast.MotionToast;

public class PostDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";
    private int position;

    Context context;
    PostWrapper postWrapper;
    Post post;
    TextView tvPostDetailsUsername;
    TextView tvPostDetailsBody;
    TextView tvPostDetailsTimestamp;
    TextView tvDetailsLikeCount;
    ImageButton ibPostDetailsLike;
    Button btnSubmitComment;
    EditText etCommentBody;

    ImageView ivPostDetailsProfile;
    ImageView ivPDCurrentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        context = this;

        postWrapper = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        position = getIntent().getIntExtra("position", 0);

        tvPostDetailsUsername = findViewById(R.id.tvPostDetailsUsername);
        tvPostDetailsBody = findViewById(R.id.tvPostDetailsBody);
        ivPostDetailsProfile = findViewById(R.id.ivPostDetailsProfile);
        tvPostDetailsTimestamp = findViewById(R.id.tvPostDetailsTimestamp);
        tvDetailsLikeCount = findViewById(R.id.tvDetailsLikeCount);
        ibPostDetailsLike = findViewById(R.id.ibPostDetailsLike);
        ivPDCurrentUserProfile = findViewById(R.id.ivPDCurrentUserProfile);
        etCommentBody = findViewById(R.id.etCommentBody);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);

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

        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = etCommentBody.getText().toString();
                if (body.equals("")) {
                    return;
                }
                Comment comment = new Comment(ParseUser.getCurrentUser(), post);
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error posting comment: " + e.toString());
                            return;
                        }
                        etCommentBody.setText("");
                        MotionToast.Companion.createColorToast((Activity) context,
                                "Success",
                                "Comment posted!",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(context, R.font.helvetica_regular));
                    }
                });
            }
        });

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