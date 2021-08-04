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
        // Initialize view variables
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvSearch = view.findViewById(R.id.rvSearch);
        pbSearch = view.findViewById(R.id.pbSearch);

        // Set up recyclerview
        userList = new ArrayList<>();
        adapter = new SearchAdapter(view.getContext(), userList, callback);
        rvSearch.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvSearch.setAdapter(adapter);

        // When the search button is clicked, query for users with matching (beginning)
        // of username
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
        // Query for users with matching start of username as the query
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.whereStartsWith("username", query);
        userQuery.setLimit(10);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error searching for users: " + e.toString());
                    return;
                }

                // Make the progress bar (pb) invisible and update the adapter
                // and RecyclerView.
                pbSearch.setVisibility(View.INVISIBLE);
                userList = users;
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
            transaction.addToBackStack(null);

            // If the selected user is the currently logged in user, then open their profile as usual
            if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                transaction.replace(R.id.flContainer, new ProfileFragment()).commit();
                return;
            }

            // else, create a new instance of the fragment with the selected user's information
            String profilePictureUrl = null;
            try {
                profilePictureUrl = user.getParseFile("profilePicture").getUrl();
            } catch (NullPointerException e) {
                Log.i(TAG, "could not get profile picture for user " + user.getUsername());
            }

            ProfileFragment fragment = ProfileFragment
                    .newInstance(user.getObjectId(),
                            user.getUsername(),
                            user.getString("bio"),
                            profilePictureUrl);

            transaction.replace(R.id.flContainer, fragment).commit();
        }
    };
}