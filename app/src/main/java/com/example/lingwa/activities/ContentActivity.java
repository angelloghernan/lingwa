package com.example.lingwa.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.R;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.LongClickLinkMovementMethod;
import com.example.lingwa.util.LongClickableSpan;
import com.example.lingwa.util.Paginator;
import com.example.lingwa.util.Translator;
import com.example.lingwa.util.epubparser.BookSection;
import com.example.lingwa.util.epubparser.NavPoint;
import com.example.lingwa.util.epubparser.Reader;
import com.example.lingwa.util.epubparser.exception.OutOfPagesException;
import com.example.lingwa.util.epubparser.exception.ReadingException;
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
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowOrientationRules;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import org.parceler.Parcels;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import www.sanju.motiontoast.MotionToast;

public class ContentActivity extends AppCompatActivity {

    private static final String TAG = "ContentActivity";
    public static final String KEY_RECENT_ARTICLES = "recentArticles";
    private final Context context = this;
    Button btnUpload;
    TextView tvBody;
    ContentWrapper contentWrapper;
    Paginator paginator;
    LongClickLinkMovementMethod bodyMovementMethod;
    Button btnTest;
    Reader reader;
    int currentPage = 0;
    int readerIndex = 1;
    boolean isEpub = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);


        // Get the toolbar and hide it so that we can show the custom toolbar with a back button
        // Back button needed for mobile devices without a built-in toolbar on the bottom with a back button
        // (ie: newest Google Pixel models) since otherwise it is quite easily to accidentally hit a word
        Toolbar toolbar = (Toolbar) findViewById(R.id.tbContentToolbar);
        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_arrow_back_24, getTheme()));
        getSupportActionBar().hide();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bodyMovementMethod = (LongClickLinkMovementMethod) LongClickLinkMovementMethod.getInstance();

        contentWrapper = Parcels.unwrap(getIntent().getParcelableExtra("content"));

        if (contentWrapper.epubPath != null) {
            isEpub = true;
            reader = new Reader();
            reader.setIsIncludingTextContent(true);
            // currently using an arbitrarily large number of characters to ensure that each chapter is loaded
            // individually in sections (it cuts off in chapters regardless of max chars).
            // Not the best in ALL cases, but at least for the scope of this project it works well enough
            reader.setMaxContentPerSection(1000000);
            try {
                reader.setFullContent(contentWrapper.epubPath);
            } catch (ReadingException e) {
                Log.e(TAG, "error reading epub file: " + e.toString());
            }
        }

        // Add this article's object id to the user's list of recently read articles
        ParseUser.getCurrentUser().addUnique(KEY_RECENT_ARTICLES, contentWrapper.objectId);
        ParseUser.getCurrentUser().saveInBackground();

        // Find the bottom navigation view and make sure the forward/back buttons cannot be highlighted
        BottomNavigationView bottomNavigationView = findViewById(R.id.bnvContent);
        Menu menu = bottomNavigationView.getMenu();
        menu.setGroupCheckable(0, false, true);

        // Set up forward/back buttons to change the pages
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
                        // used for loading epub books in sections
                        // unused in articles
                        if (readerIndex > 0) {
                            readerIndex--;
                            paginator.changeSection(readerIndex);
                            currentPage = paginator.size() - 1;
                        }
                        return false;
                    case R.id.action_next_page:
                        if (currentPage < paginator.size() - 1) {
                            currentPage++;
                            break;
                        }
                        readerIndex++;
                        paginator.changeSection(readerIndex);
                        currentPage = 0;
                        break;
                    default:
                        return false;
                }
                makeTextClickable(paginator.get(currentPage).toString());
                return true;
            }
        });

        tvBody = findViewById(R.id.tvBody);

        // When the views are constructed/visible, create the pages for the given text
        // using the Paginator class.
        final ViewTreeObserver observer = tvBody.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                String text = "";
                if (isEpub) {
                    try {
                        BookSection section = reader.readSection(readerIndex);
                        text = section.getSectionTextContent();
                    } catch (ReadingException | OutOfPagesException e) {
                        Log.e(TAG, "error reading epub: " + e.toString());
                    }
                } else {
                    text = contentWrapper.body;
                }
                paginator = new Paginator(text,
                        tvBody.getWidth(),
                        tvBody.getHeight(),
                        tvBody.getPaint(),
                        tvBody.getLineSpacingMultiplier(),
                        tvBody.getLineSpacingExtra(),
                        tvBody.getIncludeFontPadding(),
                        reader);

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
        tvBody.setMovementMethod(bodyMovementMethod);
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
            final ClickableSpan span = this;

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
                            MotionToast.Companion.createColorToast((Activity) context,
                                    "Oops!",
                                    "There was an error saving this word.",
                                    MotionToast.TOAST_ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.SHORT_DURATION,
                                    ResourcesCompat.getFont(context, R.font.helvetica_regular));
                            return;
                        }
                        if (ujw != null) {
                            MotionToast.Companion.createColorToast((Activity) context,
                                    "Info",
                                    "This word is already in your word list",
                                    MotionToast.TOAST_INFO,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.SHORT_DURATION,
                                    ResourcesCompat.getFont(context, R.font.helvetica_regular));
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
                                    wordEntry = new Word(word, contentWrapper.objectId);
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
                Translator.translateWord(word, "es", "en", new Translator.TranslatorCallback() {
                    @Override
                    public void onTranslationSuccess(String translation) {
                        if (translation == null) {
                            return;
                        }

                        // If the application is still running (must check), then show a popup Balloon
                        // with the translation displayed, centered where the clicked word is.
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            SpannableString completeText = (SpannableString) tvBody.getText();
                            ArrowOrientation arrowOrientation = ArrowOrientation.BOTTOM;

                            int arrowSize = 10;
                            Rect rect = getSpanCoordinateRect(tvBody, span);
                            int y = rect.top;

                            if (rect.top - arrowSize < 0) {
                                arrowOrientation = ArrowOrientation.TOP;
                                y = rect.bottom + rect.height() + arrowSize;
                            }

                            float arrowPosition = 0.3f;

                            // fixes visual bug where if a translation is short enough
                            // (roughly two letters or less on most screens),
                            // the balloon arrow will appear malformed
                            if (translation.length() <= 2) {
                                arrowPosition = 0.5f;
                            }

                            Balloon translationDisplay = new Balloon.Builder(context)
                                    .setArrowSize(arrowSize)
                                    .setArrowOrientation(arrowOrientation)
                                    .setWidth(BalloonSizeSpec.WRAP)
                                    .setCornerRadius(3f)
                                    .setBackgroundColorResource(R.color.info_color)
                                    .setTextColorResource(R.color.white)
                                    .setTextSize(18f)
                                    .setText(translation)
                                    .setArrowPosition(arrowPosition)
                                    .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                                    .setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
                                    .setBalloonAnimation(BalloonAnimation.CIRCULAR)
                                    .build();

                            double clickedTextStartOffset = completeText.getSpanStart(span);
                            double clickedTextStartX = tvBody.getLayout().getPrimaryHorizontal((int) clickedTextStartOffset);

                            translationDisplay.show(widget, (int) clickedTextStartX, y);
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

                MotionToast.Companion.createColorToast((Activity) context,
                        "Success",
                        "Word saved successfully!",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(context, R.font.helvetica_regular));
            }
    };

    // Create an (approximate) Rect around the word's line so we can tell where to center the popup translation Balloon
    // Used currently just to get the y-value (rectangle is created around line, not the word specifically as it does not matter)
    private Rect getSpanCoordinateRect(TextView textView, ClickableSpan span) {
        Rect textViewRect = new Rect();

        SpannableString completeText = (SpannableString) textView.getText();
        Layout textViewLayout = textView.getLayout();

        double clickedTextStartOffset = completeText.getSpanStart(span);
        int lineStartOffset = textViewLayout.getLineForOffset((int) clickedTextStartOffset);
        textViewLayout.getLineBounds(lineStartOffset, textViewRect);

        return textViewRect;
    }
}