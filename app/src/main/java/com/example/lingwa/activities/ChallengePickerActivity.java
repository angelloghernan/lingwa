package com.example.lingwa.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import com.example.lingwa.R;
import com.example.lingwa.adapters.WordPickerAdapter;
import com.example.lingwa.models.Challenge;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.sanju.motiontoast.MotionToast;

public class ChallengePickerActivity extends AppCompatActivity {

    private static final String TAG = "ChallengePickerActivity";
    RecyclerView rvCardHolder;
    Button btnSelectWords;
    WordPickerAdapter adapter;
    List<String> wordList;
    List<Boolean> checkedPositions;
    boolean initiatedChallenge;
    String challengeId;
    String identity;
    String opponentIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_picker);

        wordList = Parcels.unwrap(getIntent().getParcelableExtra("wordList"));
        initiatedChallenge = getIntent().getBooleanExtra("initiatedChallenge", false);
        challengeId = getIntent().getStringExtra("challengeId");

        if (initiatedChallenge) {
            identity = "challenger";
            opponentIdentity = "challenged";
        } else {
            identity = "challenged";
            opponentIdentity = "challenger";
        }

        rvCardHolder = findViewById(R.id.rvCardHolder);
        btnSelectWords = findViewById(R.id.btnSelectWords);

        checkedPositions = new ArrayList<>();

        for (int i = 0; i < wordList.size(); i++) {
            checkedPositions.add(false);
        }

        adapter = new WordPickerAdapter(this, wordList, checkedPositions);

        rvCardHolder.setLayoutManager(new GridLayoutManager(this, 2));
        rvCardHolder.setAdapter(adapter);

        btnSelectWords.setOnClickListener(v -> {
            if (adapter.getNumCardsSelected() != 10) {
                MotionToast.Companion.createColorToast((Activity) this,
                        "Info",
                        "Please select 10 words to start the challenge",
                        MotionToast.TOAST_INFO,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(this, R.font.helvetica_regular));
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                // save the selected words and add to their opponent's word list for the challenge
                ParseQuery<Challenge> challengeQuery = ParseQuery.getQuery(Challenge.class);
                challengeQuery.whereEqualTo(Challenge.KEY_OBJECT_ID, challengeId);
                Challenge challenge;
                boolean opponentAccepted = false;
                boolean opponentHasSelected = false;

                try {
                    challenge = challengeQuery.getFirst();
                    List<String> checkedWordList = new ArrayList<>();
                    for (int i = 0; i < wordList.size(); i++) {
                        if (checkedPositions.get(i)) {
                            checkedWordList.add(wordList.get(i));
                        }
                    }
                    challenge.setWords(opponentIdentity, checkedWordList);
                    challenge.setArePlayerWordsSelected(identity, true);
                    opponentAccepted = challenge.getReady(opponentIdentity);
                    opponentHasSelected = challenge.arePlayerWordsSelected(opponentIdentity);
                    challenge.save();
                } catch (ParseException e) {
                    Log.e(TAG, "Error finding and saving challenge: " + e.toString());
                    return;
                }

                // note: consider making a challenge wrapper for later refactoring if there is time!
                Intent intent = new Intent(this, ChallengeActivity.class);
                intent.putExtra("initiatedChallenge", initiatedChallenge);
                intent.putExtra("challengeId", challengeId);
                intent.putExtra("identity", identity);
                intent.putExtra("opponentIdentity", opponentIdentity);
                intent.putExtra("opponentAccepted", opponentAccepted);
                intent.putExtra("opponentHasSelected", opponentHasSelected);

                handler.post(() -> {
                    startActivity(intent);
                });
            });
        });
    }
}