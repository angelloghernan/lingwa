package com.example.lingwa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.github.mertakdut.BookSection;
import com.github.mertakdut.Reader;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;
import com.google.gson.JsonArray;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.BookProcessor;
import nl.siegmann.epublib.epub.EpubReader;
import www.sanju.motiontoast.MotionToast;

public class MyStuffActivity extends AppCompatActivity {

    private static final String TAG = "MyStuffActivity";
    public static final int FLASHCARD_REQUEST_CODE = 10;
    private static final int UPLOAD_REQUEST_CODE = 11;
    ListView lvSavedWords;
    Button btnPractice;
    Button btnUpload;
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
        btnUpload = findViewById(R.id.btnUpload);
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
            UserJoinWord ujwEntry = ujwEntryList.get(i);
            Word word = ujwEntry.getWord();
            WordWrapper wordWrapper = new WordWrapper(word.getOriginalWord(), word.getObjectId(),
                    ujwEntry.getSavedBy(), word.getOriginatesFrom().getObjectId());
            wordWrapper.setFamiliarityScore(ujwEntry.getFamiliarityScore());
            wordWrapper.setParentObjectId(ujwEntry.getObjectId());
            wordWrapper.setStruggleIndex(ujwEntry.getStruggleIndex());
            wordWrapper.setStreak(ujwEntry.getStreak());
            wordWrapper.setGotRightLastTime(ujwEntry.getGotRightLastTime());
            wordList.add(wordWrapper);
        }

        btnPractice.setOnClickListener(startPractice);
        // btnUpload.setOnClickListener(uploadBook);
    }
    View.OnClickListener startPractice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ParseUser.getCurrentUser().getJSONArray("recentArticles") == null) {
                MotionToast.Companion.createToast((Activity) context,
                        "Warning",
                        "Please read an article before practicing!",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(context, R.font.helvetica_regular));
                return;
            }
            pbStuffLoading.setVisibility(View.VISIBLE);
            pbStuffLoading.setActivated(true);
            Intent intent = new Intent(context, FlashcardsActivity.class);
            intent.putExtra("wordList", Parcels.wrap(wordList));
            startActivityForResult(intent, FLASHCARD_REQUEST_CODE);
        }
    };

    /*
    View.OnClickListener uploadBook = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent  = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*\/*");
            startActivityForResult(intent, UPLOAD_REQUEST_CODE);
        }
    };
     */

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