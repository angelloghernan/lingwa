package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lingwa.util.Translator;

import java.util.ArrayList;
import java.util.Collections;

public class FlashcardsActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardsActivity";
    ArrayList<String> savedWords;
    ProgressBar pbFlashcardProgress;
    TextView tvWord;
    EditText etAnswer;
    Button btnSubmit;
    String currentWord;
    String wordTranslation = null;
    int wordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        savedWords = getIntent().getStringArrayListExtra("savedWords");
        pbFlashcardProgress = findViewById(R.id.pbFlashcardProgress);
        tvWord = findViewById(R.id.tvWord);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);

        Collections.shuffle(savedWords);
        currentWord = savedWords.get(0);
        nextWord();

        btnSubmit.setOnClickListener(submit);

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
                        return;
                    }
                    wordTranslation = null;
                    pbFlashcardProgress.setProgress((int) (wordIndex / savedWords.size()));
                    currentWord = savedWords.get(wordIndex);
                    etAnswer.setText("");
                    nextWord();
                }
            }
    };

    private void nextWord() {
        Translator.translateWord(currentWord, "es", "en", new Translator.TranslatorCallback() {
            @Override
            public void onTranslationSuccess(String translation) {
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