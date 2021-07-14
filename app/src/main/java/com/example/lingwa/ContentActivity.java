package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.models.Content;
import com.example.lingwa.wrappers.ContentWrapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class ContentActivity extends AppCompatActivity {

    private static final String TAG = "ContentActivity";
    private final Context context = this;
    Button btnUpload;
    TextView tvBody;
    ContentWrapper contentWrapper;
    List<String> bodyPages;
    int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        contentWrapper = Parcels.unwrap(getIntent().getParcelableExtra("content"));
        // Content content = (Content) Content.createWithoutData("content", contentWrapper.objectId);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bnvContent);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_previous_page:
                        if (currentPage > 0) {
                            currentPage--;
                        } else {
                            return false;
                        }
                        break;
                    case R.id.action_next_page:
                        if (currentPage < bodyPages.size() - 1) {
                            currentPage++;
                        } else {
                            return false;
                        }
                            break;
                    default:
                        return false;
                }
                makeTextClickable(bodyPages.get(currentPage));
                return true;
            }
        });

        tvBody = findViewById(R.id.tvBody);
        final ViewTreeObserver observer = tvBody.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // TODO: In progress -- try to break up the text into pages. This is going to be more difficult than anticipated
                int maxLines = tvBody.getHeight() / tvBody.getLineHeight();
                // convert text size to sp from px so we can calculate the chars per line
                float textSize = pxToSp(tvBody.getTextSize());
                tvBody.setMaxLines(maxLines);
                tvBody.setHighlightColor(Color.LTGRAY);

                int charsPerLine = (int) (tvBody.getWidth() / textSize);
                int totalChars = (charsPerLine * maxLines) - (int) (pxToSp(tvBody.getLineSpacingExtra()) * maxLines);
                bodyPages = getParts(contentWrapper.body, totalChars);
                makeTextClickable(bodyPages.get(0));
                tvBody.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    // Adds ClickableSpan to every word so that the user can tap on them.
    private void makeTextClickable(String text) {
        text = text.trim();
        // Long link movement method needed so that the text is highlighted when pressed and
        // the onClick and onLongClick functions are fired when clicked and held respectively
        tvBody.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        tvBody.setText(text, TextView.BufferType.SPANNABLE);
        // Create a spannable so that we can edit the properties of individual words
        Spannable spannable = (Spannable) tvBody.getText();
        // Use a BreakIterator so we can go over each word one by one
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(text);
        int start = iterator.first();
        // Go over each word one by one until there are no words left (end = BreakIterator.DONE).
        // If the word starts with a letter or digit, add a clickable span to the word.
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String possibleWord = text.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                LongClickableSpan clickSpan = getLongClickableSpan(possibleWord);
                spannable.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    private LongClickableSpan getLongClickableSpan(String spanWord) {
        return new LongClickableSpan() {
            final String word = spanWord;
            @Override
            public void onLongClick(View view) {
                ParseUser.getCurrentUser().addUnique("savedWords", spanWord);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving word: " + e.toString());
                            return;
                        }
                        Toast.makeText(context, "Word saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onClick(@NonNull View widget) {
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

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
    }

    // utility so we can split the array list into x characters
    private static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<String>();
        int len = string.length();
        for (int i=0; i<len; i+=partitionSize)
        {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    private int pxToSp(float px) {
        return (int) (px / getResources().getDisplayMetrics().scaledDensity);
    }
}