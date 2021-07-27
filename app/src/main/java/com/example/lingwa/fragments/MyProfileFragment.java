package com.example.lingwa.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.LoginActivity;
import com.example.lingwa.MyStuffActivity;
import com.example.lingwa.PostDetailsActivity;
import com.example.lingwa.R;
import com.example.lingwa.adapters.PostAdapter;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.Post;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.samlss.broccoli.Broccoli;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment {

    private static final String ARG_USERID = "userid";
    public static final String ARG_USERNAME = "username";
    public static final String ARG_BIO = "bio";
    public static final String ARG_PROFILE_PICTURE = "profilePicture";
    private static final String TAG = "MyProfileFragment";
    public static final int POST_DETAILS_REQUEST = 101;

    private String userId;
    private String username;
    private String bio;
    private String profilePictureUrl = null;

    private ParseUser user;
    boolean useCurrentUser = true;

    Button btnLogOut;
    Button btnMyStuff;
    TextView tvProfileUsername;
    TextView tvProfileBio;
    ImageView ivProfilePicture;
    RecyclerView rvMyPosts;
    List<Post> postList;
    PostAdapter adapter;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId The id of the user to be displayed
     * @param username Username of the user
     * @param bio Bio of the user
     * @return A new instance of fragment MyProfileFragment.
     */
    public static MyProfileFragment newInstance(String userId, String username, String bio, String profilePictureUrl) {
        MyProfileFragment fragment = new MyProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERID, userId);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_BIO, bio);
        args.putString(ARG_PROFILE_PICTURE, profilePictureUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USERID);
            username = getArguments().getString(ARG_USERNAME);
            bio = getArguments().getString(ARG_BIO);
        } else {
            userId = username = bio = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnMyStuff = view.findViewById(R.id.btnMyStuff);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        rvMyPosts = view.findViewById(R.id.rvMyPosts);

        if (userId == null) {
            ParseUser user = ParseUser.getCurrentUser();
            userId = user.getObjectId();
            username = user.getUsername();
            bio = user.getString("bio");
            try {
                profilePictureUrl = user.getParseFile("profilePicture").getUrl();
            } catch (NullPointerException e) {
                Log.e(TAG, "error getting profilepicture: " + e.toString());
            }
        }

        tvProfileUsername.setText(username);
        tvProfileBio.setText(bio);
        if (profilePictureUrl != null) {
            Glide.with(requireContext())
                    .load(user.getParseFile("profilePicture").getUrl())
                    .circleCrop()
                    .into(ivProfilePicture);
        } else {
            Glide.with(requireContext())
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture);
        }


        postList = new ArrayList<>();
        adapter = new PostAdapter(view.getContext(), null, postList, callback);
        adapter.setPostsShown(true);
        rvMyPosts.setAdapter(adapter);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(view.getContext()));
        fetchPosts();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent i = new Intent(view.getContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        btnMyStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(), MyStuffActivity.class);
                startActivity(i);
            }
        });
    }

    private void fetchPosts() {
        ParseUser user = ParseUser.createWithoutData(ParseUser.class, userId);
        ParseQuery<Post> myPostsQuery = ParseQuery.getQuery(Post.class);
        myPostsQuery.whereEqualTo(Post.KEY_AUTHOR, user);
        myPostsQuery.setLimit(20);
        myPostsQuery.include(Post.KEY_AUTHOR);
        myPostsQuery.addDescendingOrder("createdAt");
        myPostsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error getting user's posts: " + e.toString());
                    return;
                }

                postList.addAll(posts);
                adapter.clearPosts();
                adapter.addAllPosts(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    PostAdapter.AdapterCallback callback = new PostAdapter.AdapterCallback() {
        @Override
        public void onContentSelected(int position, Content content) {
        }

        @Override
        public void onPostSelected(int position, Post post) {
            Intent intent = new Intent(getContext(), PostDetailsActivity.class);

            PostWrapper postWrapper = PostWrapper.fromPost(post);

            intent.putExtra("post", Parcels.wrap(postWrapper));
            startActivityForResult(intent, POST_DETAILS_REQUEST);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        if (requestCode == POST_DETAILS_REQUEST) {
            PostWrapper postWrapper = Parcels.unwrap(data.getParcelableExtra("postWrapper"));
            int position = data.getIntExtra("position", 0);
            postList.get(position).updateFromWrapper(postWrapper);
            adapter.notifyItemChanged(position);
        }
    }
}