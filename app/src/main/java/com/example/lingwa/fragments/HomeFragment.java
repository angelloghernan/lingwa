package com.example.lingwa.fragments;

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
import android.widget.ProgressBar;

import com.example.lingwa.ContentActivity;
import com.example.lingwa.R;
import com.example.lingwa.adapters.PostAdapter;
import com.example.lingwa.models.Content;
import com.example.lingwa.wrappers.ContentWrapper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import me.samlss.broccoli.Broccoli;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected PostAdapter adapter;
    protected List<Content> contentList;

    RecyclerView rvHomeFeed;
    ProgressBar pbPostLoading;

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
        rvHomeFeed = view.findViewById(R.id.rvHomeFeed);
        pbPostLoading = view.findViewById(R.id.pbPostLoading);
        contentList = new ArrayList<>();
        queryForContent();
        adapter = new PostAdapter(view.getContext(), contentList, callback);

        rvHomeFeed.setAdapter(adapter);
        rvHomeFeed.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    // Get the last 20 content posts so it can be used in the adapter and displayed
    // in the recycler view (rvHomeFeed)
    private void queryForContent() {
        pbPostLoading.setVisibility(View.VISIBLE);
        pbPostLoading.setActivated(true);
        ParseQuery<Content> contentQuery = ParseQuery.getQuery(Content.class);
        contentQuery.setLimit(20);
        contentQuery.addDescendingOrder("createdAt");

        contentQuery.findInBackground(new FindCallback<Content>() {
            @Override
            public void done(List<Content> content, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting content posts ", e);
                    return;
                }
                pbPostLoading.setVisibility(View.INVISIBLE);
                pbPostLoading.setActivated(false);
                adapter.clear();
                adapter.addAll(content);
                adapter.notifyDataSetChanged();
            }
        });
    }

    PostAdapter.AdapterCallback callback = new PostAdapter.AdapterCallback() {
        @Override
        public void onPostSelected(int position, Content content) {
            // Wrap up everything in the selected post and send this information to the content activity
            Intent intent = new Intent(getContext(), ContentActivity.class);

            ContentWrapper parcelableWrapper = new ContentWrapper(content.getObjectId(),
                    content.getTitle(), content.getAuthor());
            parcelableWrapper.contentType = content.getContentType();
            if (parcelableWrapper.contentType == Content.TYPE_ARTICLE) {
                parcelableWrapper.attachmentUrl = content.getAttachment().getUrl();
            } else {
                parcelableWrapper.body = content.getBody();
            }
            parcelableWrapper.thumbnailUrl = content.getThumbnail().getUrl();

            intent.putExtra("content", Parcels.wrap(parcelableWrapper));
            startActivity(intent);
        }
    };
}