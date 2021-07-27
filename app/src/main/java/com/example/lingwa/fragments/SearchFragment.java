package com.example.lingwa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lingwa.R;
import com.example.lingwa.adapters.SearchAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    EditText etSearch;
    Button btnSearch;
    RecyclerView rvSearch;
    ProgressBar pbSearch;

    SearchAdapter adapter;
    List<ParseUser> userList;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvSearch = view.findViewById(R.id.rvSearch);
        pbSearch = view.findViewById(R.id.pbSearch);

        userList = new ArrayList<>();
        adapter = new SearchAdapter(view.getContext(), userList, callback);
        rvSearch.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvSearch.setAdapter(adapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbSearch.setVisibility(View.VISIBLE);
                String query = etSearch.getText().toString();
                if (query.equals("")) {
                    return;
                }
                queryForUser(query);
            }
        });

    }

    void queryForUser(String query) {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.whereStartsWith("username", query);
        userQuery.setLimit(10);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error searching for users: " + e.toString());
                }

                userList = users;
                pbSearch.setVisibility(View.INVISIBLE);
                adapter.clear();
                adapter.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }

    SearchAdapter.AdapterCallback callback = new SearchAdapter.AdapterCallback() {
        @Override
        public void onUserSelected(ParseUser user, int position) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            String profilePictureUrl = null;
            try {
                profilePictureUrl = user.getParseFile("profilePicture").getUrl();
            } catch (NullPointerException e) {
                Log.i(TAG, "could not get profile picture for user " + user.getUsername());
            }

            MyProfileFragment fragment = MyProfileFragment
                    .newInstance(user.getObjectId(),
                            user.getUsername(),
                            user.getString("bio"),
                            profilePictureUrl);

            transaction.addToBackStack(null);

            transaction.replace(R.id.flContainer, fragment).commit();
        }
    };
}