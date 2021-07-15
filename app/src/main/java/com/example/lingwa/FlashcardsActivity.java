package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.util.Translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;

public class FlashcardsActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardsActivity";
    ArrayList<String> savedWords;
    ProgressBar pbFlashcardProgress;
    FrameLayout flFlashcard;
    TextView tvWord;
    EditText etAnswer;
    Button btnSubmit;

    String currentWord;
    String wordTranslation = null;
    int wordIndex = 0;
    boolean flashcardFlipped = false;
    boolean flashcardFlipping = false;
    Context context = this;

    final long FLIP_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        savedWords = getIntent().getStringArrayListExtra("savedWords");
        pbFlashcardProgress = findViewById(R.id.pbFlashcardProgress);
        flFlashcard = findViewById(R.id.flFlashcard);
        tvWord = findViewById(R.id.tvWord);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);

        Collections.shuffle(savedWords);
        currentWord = savedWords.get(0);
        nextWord();

        btnSubmit.setOnClickListener(submit);
        flFlashcard.setOnClickListener(flipFlashcard);


    }

    View.OnClickListener submit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordTranslation == null)
                    return;

                String answer = etAnswer.getText().toString().toLowerCase();

                if (answer.equals(wordTranslation)) {
                    wordIndex++;
                    if (wordIndex > savedWords.size() - 1) {
                        Toast.makeText(context, "Practice complete!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    wordTranslation = null;
                    int progress = (int) (((float) wordIndex / (float) savedWords.size()) * 100);
                    flashcardFlipped = false;
                    pbFlashcardProgress.setProgress(progress);
                    currentWord = savedWords.get(wordIndex);
                    etAnswer.setText("");
                    nextWord();
                }
            }
    };

    View.OnClickListener flipFlashcard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Animation animation = flFlashcard.getAnimation();
            if (flashcardFlipping)
                return;

            flashcardFlipping = true;

            if (!flashcardFlipped) {
                flashcardFlipped = true;
                flFlashcard.animate().rotationY(180f).setDuration(FLIP_DURATION).start();
                tvWord.animate().rotationY(180f).setDuration(FLIP_DURATION).start();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvWord.setText(wordTranslation);
                        flashcardFlipping = false;
                    }
                }, FLIP_DURATION/2);
            } else {
                flashcardFlipped = false;
                flFlashcard.animate().rotationY(360f).setDuration(FLIP_DURATION).start();
                tvWord.animate().rotationY(360f).setDuration(FLIP_DURATION).start();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvWord.setText(currentWord);
                        flashcardFlipping = false;
                    }
                }, FLIP_DURATION/2);
            }
        }
    };

    private void nextWord() {
        Translator.translateWord(currentWord, "es", "en", new Translator.TranslatorCallback() {
            @Override
            public void onTranslationSuccess(String translation) {
                flFlashcard.setRotationY(0f);
                tvWord.setRotationY(0f);
                tvWord.setText(currentWord);
                wordTranslation = translation.toLowerCase();
            }

            @Override
            public void onTranslationFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }
}