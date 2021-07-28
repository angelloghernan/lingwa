package com.example.lingwa.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lingwa.ContentActivity;
import com.example.lingwa.PostDetailsActivity;
import com.example.lingwa.R;
import com.example.lingwa.adapters.PostAdapter;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.FollowEntry;
import com.example.lingwa.models.Post;
import com.example.lingwa.wrappers.ContentWrapper;
import com.example.lingwa.wrappers.PostWrapper;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.samlss.broccoli.Broccoli;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    public static final String KEY_POST_LIST = "postList";
    public static final String KEY_CONTENT_LIST = "contentList";
    public static final String KEY_RECYCLER_VIEW_STATE = "rvState";
    public static final int POST_DETAILS_REQUEST = 101;

    protected PostAdapter adapter;
    protected List<Content> contentList;
    protected List<Post> postList;
    boolean viewRestored = false;

    Parcelable postListState;
    Parcelable contentListState;
    Parcelable rvState;

    RecyclerView rvHomeFeed;
    ProgressBar pbPostLoading;
    TextView tvNoPosts;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        /*
        if (savedInstanceState != null) {
            contentList = Parcels.unwrap(savedInstanceState.getParcelable(KEY_CONTENT_LIST));
            postList = Parcels.unwrap(savedInstanceState.getParcelable(KEY_POST_LIST));
            rvState = Parcels.unwrap(savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE));
            viewRestored = true;
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        rvState = rvHomeFeed.getLayoutManager().onSaveInstanceState();
        contentListState = Parcels.wrap(contentList);
        postListState = Parcels.wrap(postList);
        outState.putParcelable(KEY_CONTENT_LIST, contentListState);
        outState.putParcelable(KEY_POST_LIST, postListState);
        outState.putParcelable(KEY_RECYCLER_VIEW_STATE, rvState);
         */
    }

    @Override
    public void onViewStateRestored(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        /*
        if (savedInstanceState != null) {
            postListState = savedInstanceState.getParcelable(KEY_POST_LIST);
            contentListState = savedInstanceState.getParcelable(KEY_CONTENT_LIST);
            rvState = savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE);
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pbPostLoading = view.findViewById(R.id.pbPostLoading);
        contentList = new ArrayList<>();
        postList = new ArrayList<>();
        queryForContentAndPosts();

        tvNoPosts = view.findViewById(R.id.tvNoPosts);
        rvHomeFeed = view.findViewById(R.id.rvHomeFeed);

        adapter = new PostAdapter(view.getContext(), contentList, postList, callback);
        rvHomeFeed.setAdapter(adapter);
        rvHomeFeed.setLayoutManager(new LinearLayoutManager(view.getContext()));

        TabLayout tlHomeTabs = view.findViewById(R.id.tlHomeTabs);
        tlHomeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        tvNoPosts.setVisibility(View.INVISIBLE);
                        adapter.setPostsShown(false);
                        rvHomeFeed.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        break;

                    case 0:
                        if (postList.size() == 0) {
                            tvNoPosts.setVisibility(View.VISIBLE);
                        }
                        adapter.setPostsShown(true);
                        rvHomeFeed.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // Get the last 20 content posts so it can be used in the adapter and displayed
    // in the recycler view (rvHomeFeed)
    private void queryForContentAndPosts() {
        pbPostLoading.setVisibility(View.VISIBLE);
        pbPostLoading.setActivated(true);
        ParseQuery<Content> contentQuery = ParseQuery.getQuery(Content.class);
        contentQuery.setLimit(10);
        contentQuery.addDescendingOrder(ParseObject.KEY_CREATED_AT);

        ParseQuery<FollowEntry> innerFollowQuery = ParseQuery.getQuery(FollowEntry.class);
        innerFollowQuery.whereEqualTo(FollowEntry.KEY_FOLLOWER, ParseUser.getCurrentUser());

        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.setLimit(10);
        postQuery.include(Post.KEY_AUTHOR);
        postQuery.whereMatchesKeyInQuery(Post.KEY_AUTHOR, FollowEntry.KEY_FOLLOWED, innerFollowQuery);
        postQuery.whereDoesNotExist(Post.KEY_REPLYING_TO);
        postQuery.addDescendingOrder(ParseObject.KEY_CREATED_AT);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                contentList = contentQuery.find();
                postList = postQuery.find();
            } catch (ParseException e) {
                Log.e(TAG, e.toString());
            }

            for (int i = 0; i < postList.size(); i++) {
                Post post = postList.get(i);
                post.liked = post.isLikedByUser(ParseUser.getCurrentUser());
                post.likesFetched = true;
            }

            adapter.clearContent();
            adapter.clearPosts();
            adapter.addAllPosts(postList);
            adapter.addAllContent(contentList);

            handler.post(() -> {
                if (postList.size() == 0) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
                pbPostLoading.setVisibility(View.INVISIBLE);
                pbPostLoading.setActivated(false);
            });
        });
    }

    PostAdapter.AdapterCallback callback = new PostAdapter.AdapterCallback() {
        @Override
        public void onContentSelected(int position, Content content) {
            // Wrap up everything in the selected post and send this information to the content activity
            Intent intent = new Intent(getContext(), ContentActivity.class);

            ContentWrapper parcelableWrapper = ContentWrapper.fromContent(content);

            intent.putExtra("content", Parcels.wrap(parcelableWrapper));
            startActivity(intent);
        }

        @Override
        public void onPostSelected(int position, Post post) {
            Intent intent = new Intent(getContext(), PostDetailsActivity.class);

            PostWrapper postWrapper = PostWrapper.fromPost(post);

            intent.putExtra("post", Parcels.wrap(postWrapper));
            intent.putExtra("position", position);
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