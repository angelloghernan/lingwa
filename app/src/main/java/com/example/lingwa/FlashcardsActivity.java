package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.FamiliarityAlgo;
import com.example.lingwa.util.Translator;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class FlashcardsActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardsActivity";
    ProgressBar pbFlashcardProgress;
    ProgressBar pbWordLoading;
    FrameLayout flFlashcard;
    TextView tvWord;
    EditText etAnswer;
    Button btnSubmit;

    String currentWord;
    String wordTranslation = null;
    int wordIndex = 0;
    boolean flashcardFlipped = false;
    boolean flashcardFlipping = false;
    boolean answeredCorrectly = true;
    Context context = this;

    List<WordWrapper> wordList;

    final long FLIP_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        wordList = Parcels.unwrap(getIntent().getParcelableExtra("wordList"));

        pbFlashcardProgress = findViewById(R.id.pbFlashcardProgress);
        pbWordLoading = findViewById(R.id.pbWordLoading);
        flFlashcard = findViewById(R.id.flFlashcard);
        tvWord = findViewById(R.id.tvWord);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);

        FamiliarityAlgo familiarityAlgo = new FamiliarityAlgo();

        wordList = familiarityAlgo.calculateQuizOrder(wordList);

        currentWord = wordList.get(0).word;
        showNextWord();

        btnSubmit.setOnClickListener(submit);
        flFlashcard.setOnClickListener(onFlashcardClicked);


    }

    @Override
    public void onBackPressed() {
        updateDatabase();
        createReturnIntent();
        finish();
    }

    View.OnClickListener submit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordTranslation == null)
                    return;

                String answer = etAnswer.getText().toString().toLowerCase();

                int familiarityScore = wordList.get(wordIndex).getFamiliarityScore();

                if (answer.equals(wordTranslation)) {
                    if (answeredCorrectly && familiarityScore < 5) {
                        wordList.get(wordIndex).setFamiliarityScore(familiarityScore + 1);
                    }

                    wordIndex++;

                    if (wordIndex > wordList.size() - 1) {
                        Toast.makeText(context, "Practice complete!", Toast.LENGTH_SHORT).show();
                        updateDatabase();
                        createReturnIntent();
                        finish();
                        return;
                    }
                    wordTranslation = null;
                    int progress = (int) (((float) wordIndex / (float) wordList.size()) * 100);
                    flashcardFlipped = false;
                    pbFlashcardProgress.setProgress(progress);
                    currentWord = wordList.get(wordIndex).word;
                    etAnswer.setText("");

                    showNextWord();
                    return;
                }

                else if (answeredCorrectly && familiarityScore > 0) {
                    wordList.get(wordIndex).setFamiliarityScore(familiarityScore - 1);
                }

                answeredCorrectly = false;
            }
    };

    View.OnClickListener onFlashcardClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (flashcardFlipping)
                return;

            flashcardFlipping = true;

            if (!flashcardFlipped) {
                flipFlashcard(180f, wordTranslation);
                answeredCorrectly = false;
            } else {
                flipFlashcard(360f, currentWord);
            }

            flashcardFlipped = !flashcardFlipped;
        }
    };

    private void flipFlashcard(float degrees, String newText) {
        flFlashcard.animate().rotationY(degrees).setDuration(FLIP_DURATION).start();
        tvWord.animate().rotationY(degrees).setDuration(FLIP_DURATION).start();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                tvWord.setText(newText);
                flashcardFlipping = false;
            }
        }, FLIP_DURATION/2);
    }

    private void showNextWord() {
        tvWord.setVisibility(View.INVISIBLE);
        pbWordLoading.setVisibility(View.VISIBLE);
        pbWordLoading.setActivated(true);

        Translator.translateWord(currentWord, "es", "en", new Translator.TranslatorCallback() {
            @Override
            public void onTranslationSuccess(String translation) {
                flFlashcard.setRotationY(0f);
                tvWord.setVisibility(View.VISIBLE);
                tvWord.setRotationY(0f);
                tvWord.setText(currentWord);
                answeredCorrectly = true;
                wordTranslation = translation.toLowerCase();
                pbWordLoading.setVisibility(View.INVISIBLE);
                pbWordLoading.setActivated(false);
            }

            @Override
            public void onTranslationFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    void updateDatabase() {
        List<UserJoinWord> ujwEntryList = new ArrayList<>();

        for (int i = 0; i < wordList.size(); i++) {
            WordWrapper word = wordList.get(i);
            UserJoinWord ujwEntry = ParseObject.createWithoutData(UserJoinWord.class,
                    word.getParentObjectId());
            ujwEntry.setFamiliarityScore(word.getFamiliarityScore());
            ujwEntryList.add(ujwEntry);
        }
        ParseObject.saveAllInBackground(ujwEntryList, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void createReturnIntent() {
        Intent intent = new Intent();
        intent.putExtra("wordList", Parcels.wrap(wordList));
        setResult(RESULT_OK, intent);
    }
}