package com.example.lingwa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.wrappers.WordWrapper;
import com.google.gson.JsonArray;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyStuffActivity extends AppCompatActivity {

    private static final String TAG = "MyStuffActivity";
    public static final int FLASHCARD_REQUEST_CODE = 10;
    ListView lvSavedWords;
    Button btnPractice;
    ProgressBar pbStuffLoading;
    Context context = this;
    List<UserJoinWord> ujwEntryList = null;
    ArrayList<String> displayedWords = null;
    ArrayList<WordWrapper> wordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stuff);
        lvSavedWords = findViewById(R.id.lvSavedWords);
        btnPractice = findViewById(R.id.btnPractice);
        pbStuffLoading = findViewById(R.id.pbStuffLoading);
        pbStuffLoading.setActivated(false);

        ParseQuery<UserJoinWord> ujwQuery = ParseQuery.getQuery(UserJoinWord.class);
        ujwQuery.whereEqualTo(UserJoinWord.KEY_USER, ParseUser.getCurrentUser());
        ujwQuery.include("word");

        try {
            ujwEntryList = ujwQuery.find();
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }

        displayedWords = new ArrayList<>();
        if (ujwEntryList != null) {
            for (int i = 0; i < ujwEntryList.size(); i++) {
                    UserJoinWord ujwEntry = ujwEntryList.get(i);
                    if (ujwEntry.getSavedBy().equals("user")) {
                        displayedWords.add(Objects.requireNonNull(ujwEntry.getWord().getOriginalWord()));
                    }
            }
        }

        if (displayedWords.size() < 1) {
            displayedWords.add("No words saved!");
            displayedWords.add("Press and hold on words while reading to save words.");
        }

        ArrayAdapter<String> savedWordsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedWords);
        lvSavedWords.setAdapter(savedWordsAdapter);


        for (int i = 0; i < ujwEntryList.size(); i++) {
            Word word = ujwEntryList.get(i).getWord();
            WordWrapper wordWrapper = new WordWrapper(word.getOriginalWord(), word.getObjectId(),
                    ujwEntryList.get(i).getSavedBy(), word.getOriginatesFrom().getObjectId());
            wordWrapper.setFamiliarityScore(ujwEntryList.get(i).getFamiliarityScore());
            wordWrapper.setParentObjectId(ujwEntryList.get(i).getObjectId());
            wordList.add(wordWrapper);
        }

        btnPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParseUser.getCurrentUser().getJSONArray("recentArticles") == null) {
                    Toast.makeText(context, "Please read an article before practicing!", Toast.LENGTH_SHORT).show();
                    return;
                }
                pbStuffLoading.setVisibility(View.VISIBLE);
                pbStuffLoading.setActivated(true);
                Intent intent = new Intent(context, FlashcardsActivity.class);
                intent.putExtra("wordList", Parcels.wrap(wordList));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pbStuffLoading.setVisibility(View.INVISIBLE);
        pbStuffLoading.setActivated(false);
        if (resultCode == Activity.RESULT_OK && requestCode == FLASHCARD_REQUEST_CODE && data != null) {
            // get the new word (wrapper) list so we can use the familiarity scores
            wordList = Parcels.unwrap(data.getParcelableExtra("wordList"));
        }
    }
}