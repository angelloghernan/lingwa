package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.LongClickLinkMovementMethod;
import com.example.lingwa.util.LongClickableSpan;
import com.example.lingwa.util.Paginator;
import com.example.lingwa.util.Translator;
import com.example.lingwa.wrappers.ContentWrapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class ContentActivity extends AppCompatActivity {

    private static final String TAG = "ContentActivity";
    public static final String KEY_RECENT_ARTICLES = "recentArticles";
    private final Context context = this;
    Button btnUpload;
    TextView tvBody;
    ContentWrapper contentWrapper;
    Paginator paginator;
    int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        contentWrapper = Parcels.unwrap(getIntent().getParcelableExtra("content"));
        ParseUser.getCurrentUser().addUnique(KEY_RECENT_ARTICLES, contentWrapper.objectId);
        ParseUser.getCurrentUser().saveInBackground();
        // Content content = (Content) Content.createWithoutData("content", contentWrapper.objectId);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bnvContent);
        Menu menu = bottomNavigationView.getMenu();
        menu.setGroupCheckable(0, false, true);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_previous_page:
                        if (currentPage > 0) {
                            currentPage--;
                            break;
                        }
                        return false;
                    case R.id.action_next_page:
                        if (currentPage < paginator.size() - 1) {
                            currentPage++;
                            break;
                        }
                        return false;
                    default:
                        return false;
                }
                makeTextClickable(paginator.get(currentPage).toString());
                return true;
            }
        });

        tvBody = findViewById(R.id.tvBody);
        final ViewTreeObserver observer = tvBody.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paginator = new Paginator(contentWrapper.body,
                        tvBody.getWidth(),
                        tvBody.getHeight(),
                        tvBody.getPaint(),
                        tvBody.getLineSpacingMultiplier(),
                        tvBody.getLineSpacingExtra(),
                        tvBody.getIncludeFontPadding());

                tvBody.setHighlightColor(Color.LTGRAY);
                makeTextClickable(paginator.get(0).toString());
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

    // This is a mess of callbacks, must fix later
    private LongClickableSpan getLongClickableSpan(String spanWord) {
        return new LongClickableSpan() {
            final String word = spanWord;

            // On a long click, make an entry into the UserJoinWord table using the User and span's word.
            // If such an entry already exists, inform the user.
            @Override
            public void onLongClick(View view) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                // Make an inner query on the Word table for words equal to this span's word
                ParseQuery<Word> innerQuery = ParseQuery.getQuery(Word.class);
                innerQuery.whereEqualTo(Word.KEY_ORIGINAL_WORD, word);
                // Then, run a query to see if there are any entries with this information already.
                ParseQuery<UserJoinWord> ujwQuery = ParseQuery.getQuery(UserJoinWord.class);
                ujwQuery.whereEqualTo(UserJoinWord.KEY_USER, currentUser);
                ujwQuery.whereMatchesQuery(UserJoinWord.KEY_WORD, innerQuery);
                ujwQuery.whereEqualTo(UserJoinWord.KEY_SAVED_BY, "user");
                ujwQuery.getFirstInBackground(new GetCallback<UserJoinWord>(){
                    @Override
                    public void done(UserJoinWord ujw, ParseException e) {
                        if (e != null && e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                            Log.e(TAG, "Error checking UserJoinWord table: " + e.toString());
                            return;
                        }
                        if (ujw != null) {
                            Toast.makeText(context, "This word is already in your word list",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // If the entry doesn't exist, check if the word already exists in the Word table
                        // (should be quick because we already requested this data)
                        // If it exists, point to the entry. If not, create a new entry
                        // Then, save the UserJoinWord table entry.
                        innerQuery.getFirstInBackground(new GetCallback<Word>() {
                            @Override
                            public void done(Word wordEntry, ParseException e) {
                                if (e != null && e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                                    Log.e(TAG, "Error checking Word table: " + e.toString());
                                }
                                if (wordEntry == null) {
                                    wordEntry = new Word(word);
                                }

                                UserJoinWord ujwEntry = new UserJoinWord(currentUser, wordEntry, 0, "user");
                                // callback only to inform user if the save was successful
                                ujwEntry.saveInBackground(saveUjwCallback);
                            }
                        });
                    }
                });
            }

            // On a regular click, use the Translator API to translate the word and
            // let the user see the translation in an AlertDialog.
            @Override
            public void onClick(@NonNull View widget) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                Translator.translateWord(word, "es", "en", new Translator.TranslatorCallback() {
                    @Override
                    public void onTranslationSuccess(String translation) {
                        builder.setMessage(translation)
                                .setTitle("Translation");
                        AlertDialog dialog = builder.create();
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            dialog.show();
                        }
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

    /* Lets user know if the UserJoinWord entry was successfully saved with a Toast. */
    SaveCallback saveUjwCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.toString());
                    return;
                }

                Toast.makeText(context, "Word saved", Toast.LENGTH_SHORT).show();
            }
    };
}