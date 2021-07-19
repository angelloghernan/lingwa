package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.wrappers.WordWrapper;
import com.google.gson.JsonArray;
import com.parse.ParseException;
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
    ListView lvSavedWords;
    Button btnPractice;
    Context context = this;
    List<UserJoinWord> userWords = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stuff);
        lvSavedWords = findViewById(R.id.lvSavedWords);
        btnPractice = findViewById(R.id.btnPractice);

        ParseQuery<UserJoinWord> ujwQuery = ParseQuery.getQuery(UserJoinWord.class);
        ujwQuery.whereEqualTo(UserJoinWord.KEY_USER, ParseUser.getCurrentUser());
        ujwQuery.include("word");

        try {
            userWords = ujwQuery.find();
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }

        ArrayList<String> savedWords = new ArrayList<>();
        if (userWords != null) {
            for (int i = 0; i < userWords.size(); i++) {
                    savedWords.add(Objects.requireNonNull(userWords.get(i).getWord().getOriginalWord()));
            }
        }

        ArrayAdapter<String> savedWordsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedWords);
        lvSavedWords.setAdapter(savedWordsAdapter);

        btnPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FlashcardsActivity.class);
                ArrayList<WordWrapper> wordList = new ArrayList<>();
                List<String> ujwObjectIds = new ArrayList<>();

                for (int i = 0; i < userWords.size(); i++) {
                    Word word = userWords.get(i).getWord();
                    WordWrapper wordWrapper = new WordWrapper(word.getOriginalWord(), word.getObjectId());
                    wordWrapper.setFamiliarityScore(userWords.get(i).getFamiliarityScore());
                    wordWrapper.setParentObjectId(userWords.get(i).getObjectId());
                    wordList.add(wordWrapper);

                    ujwObjectIds.add(userWords.get(i).getObjectId());
                }

                intent.putExtra("wordList", Parcels.wrap(wordList));
                startActivity(intent);
            }
        });

    }
}