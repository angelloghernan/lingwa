package com.example.lingwa.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.lingwa.activities.ChallengePickerActivity;
import com.example.lingwa.activities.LoginActivity;
import com.example.lingwa.activities.MyStuffActivity;
import com.example.lingwa.activities.PostDetailsActivity;
import com.example.lingwa.R;
import com.example.lingwa.adapters.PostAdapter;
import com.example.lingwa.models.Challenge;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.FollowEntry;
import com.example.lingwa.models.Post;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.util.ParseApplication;
import com.example.lingwa.wrappers.PostWrapper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.sanju.motiontoast.MotionToast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

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
    boolean followCheckFinished = false;
    boolean userFollows = false;
    boolean attemptingToChallenge = false;

    Button btnRight;
    Button btnLeft;
    TextView tvProfileUsername;
    TextView tvProfileBio;
    ImageView ivProfilePicture;
    RecyclerView rvMyPosts;
    ProgressBar pbProfile;

    List<Post> postList;
    PostAdapter adapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment -- used for when a user profile is opened
     * from outside of the MainActivity that is not the current user
     *
     * @param userId The id of the user to be displayed
     * @param username Username of the user
     * @param bio Bio of the user
     * @return A new instance of fragment MyProfileFragment.
     */
    public static ProfileFragment newInstance(String userId, String username, String bio, String profilePictureUrl) {
        ProfileFragment fragment = new ProfileFragment();
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
            profilePictureUrl = getArguments().getString(ARG_PROFILE_PICTURE);
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        btnRight = view.findViewById(R.id.btnLogOut);
        btnLeft = view.findViewById(R.id.btnMyStuff);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        rvMyPosts = view.findViewById(R.id.rvMyPosts);
        pbProfile = view.findViewById(R.id.pbProfile);

        // If there was no userId passed, we use the current user's information instead
        if (userId == null) {
            user = ParseUser.getCurrentUser();
            userId = user.getObjectId();
            username = user.getUsername();
            bio = user.getString("bio");
            try {
                // note: currently this always returns null because the getCurrentUser
                // doesn't have parse files attached. If there is time to change, please change
                profilePictureUrl = user.fetchIfNeeded().getParseFile("profilePicture").getUrl();
            } catch (NullPointerException | ParseException e) {
                Log.e(TAG, "error getting profile picture: " + e.toString());
            }
            // Make sure the buttons on the left and right have the correct functionalities
            btnRight.setOnClickListener(logOut);
            btnLeft.setOnClickListener(goToMyStuff);
        } else {
            user = ParseObject.createWithoutData(ParseUser.class, userId);
            btnRight.setText("Challenge");
            btnRight.setTextColor(getResources().getColor(R.color.info_color));
            btnRight.setOnClickListener(challengeUser);
            btnLeft.setText("");
            // check if the currently logged in user follows the user they are viewing
            checkFollowStatus(ParseUser.getCurrentUser(), user);
            btnLeft.setOnClickListener(followUnfollow);
        }

        tvProfileUsername.setText(username);
        tvProfileBio.setText(bio);

        if (profilePictureUrl != null) {
            Glide.with(requireContext())
                    .load(profilePictureUrl)
                    .circleCrop()
                    .into(ivProfilePicture);
        } else {
            Glide.with(requireContext())
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(ivProfilePicture);
        }

        // Set up recyclerview and fetch posts made by this user
        postList = new ArrayList<>();
        adapter = new PostAdapter(view.getContext(), null, postList, callback);
        adapter.setPostsShown(true);
        rvMyPosts.setAdapter(adapter);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(view.getContext()));
        fetchPosts();
    }

    View.OnClickListener followUnfollow = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!followCheckFinished) {
                return;
            }

            if (!userFollows) {
                FollowEntry followEntry = new FollowEntry(ParseUser.getCurrentUser(), user);
                followEntry.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        userFollows = true;
                        btnLeft.setText(R.string.unfollow);
                        ParseApplication.createPushNotification(ParseUser.getCurrentUser().getUsername() + " followed you!",
                                "New Follower", user);
                    }
                });
            } else {
                ParseQuery<FollowEntry> followQuery = ParseQuery.getQuery(FollowEntry.class);
                followQuery.whereEqualTo(FollowEntry.KEY_FOLLOWER, ParseUser.getCurrentUser());
                followQuery.whereEqualTo(FollowEntry.KEY_FOLLOWED, user);
                followQuery.getFirstInBackground(new GetCallback<FollowEntry>() {
                    @Override
                    public void done(FollowEntry entry, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error unfollowing user:  "+ e.toString());
                            return;
                        }

                        entry.deleteInBackground();
                        btnLeft.setText(R.string.follow);
                        userFollows = false;
                    }
                });
            }

        }
    };

    View.OnClickListener challengeUser = v -> {
        if (attemptingToChallenge) {
            return;
        }

        attemptingToChallenge = true;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        List<String> wordList = new ArrayList<>();

        executor.execute(() -> {

            Challenge challenge = new Challenge();
            challenge.setChallenger(ParseUser.getCurrentUser());
            ParseUser challengedPointer = ParseObject.createWithoutData(ParseUser.class, user.getObjectId());
            challenge.setChallenged(challengedPointer);

            // we need to check first and see if both users have at least 10 words saved to start the challenge
            ParseQuery<UserJoinWord> challengerUJWQuery = ParseQuery.getQuery(UserJoinWord.class);
            challengerUJWQuery.include(UserJoinWord.KEY_WORD);
            challengerUJWQuery.whereEqualTo(UserJoinWord.KEY_SAVED_BY, UserJoinWord.KEY_USER);
            challengerUJWQuery.whereEqualTo(UserJoinWord.KEY_USER, ParseUser.getCurrentUser());

            ParseQuery<UserJoinWord> challengedUJWQuery = ParseQuery.getQuery(UserJoinWord.class);
            challengedUJWQuery.include(UserJoinWord.KEY_WORD);
            challengedUJWQuery.whereEqualTo(UserJoinWord.KEY_SAVED_BY, UserJoinWord.KEY_USER);
            challengedUJWQuery.whereEqualTo(UserJoinWord.KEY_USER, challengedPointer);


            try {

                if (challengerUJWQuery.count() < 10 ||
                    challengedUJWQuery.count() < 10) {
                    MotionToast.Companion.createColorToast((Activity) getContext(),
                            "Info",
                            "You and the other user need at least 10 words saved to challenge each other",
                            MotionToast.TOAST_WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(getContext(), R.font.helvetica_regular));
                    return;
                }
                challenge.save();

                List<UserJoinWord> challengerUJWEntries = challengerUJWQuery.find();
                for (int i = 0; i < challengerUJWEntries.size(); i++) {
                    wordList.add(challengerUJWEntries.get(i).getWord().getOriginalWord());
                }

            } catch (ParseException e) {
                Log.e(TAG, "Error challenging user: " + e.toString());
                MotionToast.Companion.createColorToast((Activity) getContext(),
                        "Error",
                        "Error challenging user!",
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(getContext(), R.font.helvetica_regular));
                        return;
            }

            handler.post(() -> {
                Intent intent = new Intent(getContext(), ChallengePickerActivity.class);
                intent.putExtra("initiatedChallenge", true);
                intent.putExtra("challengeId", challenge.getObjectId());
                intent.putExtra("wordList", Parcels.wrap(wordList));
                attemptingToChallenge = false;
                startActivity(intent);
            });
        });
    };

    View.OnClickListener logOut = v -> {
        ParseUser.logOut();
        Intent i = new Intent(v.getContext(), LoginActivity.class);
        startActivity(i);
    };

    View.OnClickListener goToMyStuff = v -> {
        Intent i = new Intent(v.getContext(), MyStuffActivity.class);
        startActivity(i);
    };

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

    private void checkFollowStatus(ParseUser follower, ParseUser followed) {
        ParseQuery<FollowEntry> followEntry = ParseQuery.getQuery(FollowEntry.class);
        followEntry.whereEqualTo(FollowEntry.KEY_FOLLOWER, follower);
        followEntry.whereEqualTo(FollowEntry.KEY_FOLLOWED, followed);

        followEntry.getFirstInBackground(new GetCallback<FollowEntry>() {
            @Override
            public void done(FollowEntry object, ParseException e) {
                followCheckFinished = true;

                if (e != null) {
                    userFollows = false;

                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        btnLeft.setText(R.string.follow);
                        return;
                    }

                    Log.e(TAG, "error checking follower status: " + e.toString());
                    return;
                }

                btnLeft.setText(R.string.unfollow);
                userFollows = true;
            }
        });
    }

    private void fetchPosts() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Query for all the posts of the user we are currently viewing
            ParseUser user = ParseUser.createWithoutData(ParseUser.class, userId);
            ParseQuery<Post> myPostsQuery = ParseQuery.getQuery(Post.class);
            myPostsQuery.whereEqualTo(Post.KEY_AUTHOR, user);
            myPostsQuery.whereDoesNotExist(Post.KEY_REPLYING_TO);
            myPostsQuery.setLimit(20);
            myPostsQuery.include(Post.KEY_AUTHOR);
            myPostsQuery.addDescendingOrder("createdAt");

            try {
                postList = myPostsQuery.find();
                // Check each post to see if it is liked by the current user
                for (int i = 0; i < postList.size(); i++) {
                    Post post = postList.get(i);
                    post.liked = post.isLikedByUser(ParseUser.getCurrentUser());
                    post.likesFetched = true;
                }
                // Clear the adapter's posts and add all the posts we have just fetched
                adapter.clearPosts();
                adapter.addAllPosts(postList);
            } catch (ParseException e) {
                Log.e(TAG, "error getting user's posts: " + e.toString());
            }

            handler.post(() -> {
                // Notify the adapter of the changes so that the RecyclerView can update after
                // the posts have been fetched (handler.post)
                // Additionally make the progressbar (pb) invisible now that the posts have loaded
                adapter.notifyDataSetChanged();
                pbProfile.setVisibility(View.INVISIBLE);
            });
        });
    }

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