package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class ContentActivity extends AppCompatActivity {

    private static final String TAG = "ContentActivity";
    private final Context context = this;
    Button btnUpload;
    TextView tvBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        btnUpload = findViewById(R.id.btnUpload);
        tvBody = findViewById(R.id.tvBody);

        // TODO: Allow user to import their own book to read
        /*
        EpubReader epubReader = new EpubReader();
        try {
            Book book = epubReader.readEpub(new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } */

        // TODO: Allow user to upload their own book
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        makeTextClickable();
    }

    // Adds ClickableSpan to every word so that the user can tap on them.
    private void makeTextClickable() {
        // Currently using sample text to check.
        String sampleText = "Hola, como estas".trim();
        // Link movement method needed so that the text is highlighted when pressed.
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());
        tvBody.setText(sampleText, TextView.BufferType.SPANNABLE);
        // Create a spannable so that we can edit the properties of individual words
        Spannable spannable = (Spannable) tvBody.getText();
        // Use a BreakIterator so we can go over each word one by one
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(sampleText);
        int start = iterator.first();
        // Go over each word one by one until there are no words left (end = BreakIterator.DONE).
        // If the word starts with a letter or digit, add a clickable span to the word.
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String possibleWord = sampleText.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord);
                spannable.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    private ClickableSpan getClickableSpan(String spanWord) {
        return new ClickableSpan() {
            final String word = spanWord;

            @Override
            public void onClick(@NonNull View widget) {
                // TODO: implement translation pop-up using Google Translate API
                // Toast.makeText(context, "Clicked on word: " + word, Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                Translator.translateWord(word, "es", "en", new Translator.TranslatorCallback() {
                    @Override
                    public void onTranslationSuccess(String translation) {
                        builder.setMessage(translation)
                                .setTitle("Translation");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onTranslationFailure(Exception e) {
                        Log.e(TAG, "on failure to translate word: " + e);
                    }
                });



            }

            // TODO: change draw state so that text does not look like a link
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }
}