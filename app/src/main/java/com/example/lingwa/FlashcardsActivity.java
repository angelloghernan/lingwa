package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.FragmentTransitionSupport;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingwa.fragments.FlashcardFragment;
import com.example.lingwa.fragments.QuizFragment;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.FamiliarityAlgo;
import com.example.lingwa.util.Translator;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import www.sanju.motiontoast.MotionToast;

public class FlashcardsActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardsActivity";
    private static final int MAX_STRUGGLE_INDEX = 3;
    ProgressBar pbFlashcardProgress;
    ProgressBar pbWordLoading;
    FrameLayout flFlashcard;
    TextView tvWord;
    EditText etAnswer;
    Button btnSubmit;

    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f );
    protected AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f );

    QuizFragment quizFragment;

    String currentWord;
    String wordTranslation = null;
    int wordIndex = 0;
    boolean flashcardFlipped = false;
    boolean flashcardFlipping = false;
    boolean answeredCorrectly = true;
    boolean cannotFlip = true;
    boolean quizShowing = false;

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

        fadeIn.setDuration(1200);
        fadeOut.setFillAfter(true);

        FamiliarityAlgo familiarityAlgo = new FamiliarityAlgo();

        try {
            wordList = familiarityAlgo.calculateQuizOrder(wordList, 15);
        } catch (JSONException | ParseException e) {
            Log.e(TAG, "error getting words from algo: " + e.toString());
        }

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

                WordWrapper wordWrapper = wordList.get(wordIndex);

                int familiarityScore = wordWrapper.getFamiliarityScore();
                int struggleIndex = wordWrapper.getStruggleIndex();
                int streak = wordWrapper.getStreak();
                boolean gotRightLastTime = wordWrapper.getGotRightLastTime();

                if (answer.equals(wordTranslation)) {
                    if (quizShowing) {
                        if (quizFragment == null) {
                            quizShowing = false;
                            cannotFlip = false;
                            return;
                        }
                        Fragment flashcardFragment = new FlashcardFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                        wordList.get(wordIndex).setStruggleIndex(struggleIndex - 2);

                        transaction.remove(quizFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        tvWord.setVisibility(View.VISIBLE);
                        tvWord.startAnimation(fadeIn);
                        tvWord.setText(currentWord);
                        flFlashcard.setBackgroundResource(R.drawable.rounded_shape);
                        flFlashcard.setAnimation(fadeIn);
                        etAnswer.setText("");
                        quizShowing = false;
                        cannotFlip = false;
                        answeredCorrectly = true;
                        return;
                    } else if (wordList.get(wordIndex).getStruggleIndex() >= MAX_STRUGGLE_INDEX && !answeredCorrectly) {
                        etAnswer.setText("");
                        showQuizFragment();
                        return;
                    }

                    if (answeredCorrectly) {
                        WordWrapper word = wordList.get(wordIndex);
                        if (struggleIndex > 0) {
                            word.setStruggleIndex(struggleIndex - 1);
                        }
                        if(familiarityScore < 5) {
                            word.setFamiliarityScore(familiarityScore + 1);
                        }
                        if (gotRightLastTime) {
                            word.setStreak(streak + 1);
                        } else {
                            word.setStreak(1);
                            word.setGotRightLastTime(true);
                        }
                    }

                    wordIndex++;

                    if (wordIndex > wordList.size() - 1) {
                        MotionToast.Companion.createToast((Activity) context,
                                "Good job!",
                                "Practice complete!",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(context, R.font.helvetica_regular));
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

                // If the user got the answer wrong, then lower their familiarity score,
                // increase their level of struggling, and start a "losing" streak

                if (answeredCorrectly && familiarityScore > 0) {
                    wordList.get(wordIndex).setFamiliarityScore(familiarityScore - 1);
                }

                if (answeredCorrectly && struggleIndex < MAX_STRUGGLE_INDEX) {
                    wordList.get(wordIndex).setStruggleIndex(struggleIndex + 1);
                }

                if (answeredCorrectly && gotRightLastTime) {
                    wordList.get(wordIndex).setStreak(1);
                    wordList.get(wordIndex).setGotRightLastTime(false);
                } else if (!gotRightLastTime){
                    wordList.get(wordIndex).setStreak(streak + 1);
                }

                answeredCorrectly = false;
            }
    };

    View.OnClickListener onFlashcardClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (flashcardFlipping || cannotFlip)
                return;

            flashcardFlipping = true;

            if (!flashcardFlipped) {
                flipFlashcard(180f, wordTranslation);
                answeredCorrectly = false;
            } else {
                flipFlashcard(360f, currentWord);
            }

            WordWrapper word = wordList.get(wordIndex);

            if (word.getStruggleIndex() < MAX_STRUGGLE_INDEX) {
                word.setStruggleIndex(word.getStruggleIndex() + 1);
                wordList.set(wordIndex, word);
            }


            flashcardFlipped = !flashcardFlipped;
        }
    };

    /*
    private void showQuiz() {
        // If there is no translation for this word yet
        if (wordTranslation == null) {
            pbWordLoading.setVisibility(View.VISIBLE);
            pbWordLoading.setActivated(true);
            Translator.translateWord(currentWord, "es", "en", new Translator.TranslatorCallback() {
                @Override
                public void onTranslationSuccess(String translation) {
                    wordTranslation = translation.toLowerCase();
                    pbWordLoading.setVisibility(View.INVISIBLE);
                    pbWordLoading.setActivated(false);
                    showQuizFragment();
                }

                @Override
                public void onTranslationFailure(Exception e) {
                    Log.e(TAG, "on failure to translate word: " + e.toString());
                }
            });
        }

        else {
            showQuizFragment();
        }
    }
    */

    void showQuizFragment() {
        quizFragment = QuizFragment.newInstance(currentWord, wordTranslation);

        tvWord.setVisibility(View.INVISIBLE);
        flFlashcard.setBackground(null);

        flFlashcard.setAnimation(fadeIn);

        cannotFlip = true;
        quizShowing = true;

        flFlashcard.setRotationY(0f);
        tvWord.setRotationY(0f);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.flFlashcard, quizFragment);
        transaction.commit();
    }

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
        // Show the loading indeterminate progress bar
        tvWord.setVisibility(View.INVISIBLE);
        pbWordLoading.setVisibility(View.VISIBLE);
        pbWordLoading.setActivated(true);
        cannotFlip = true;

        Translator.translateWord(currentWord, "es", "en", new Translator.TranslatorCallback() {
            @Override
            public void onTranslationSuccess(String translation) {
                // Make sure the flashcard is unrotated and reset everything
                // Make sure the loading progress bar is invisible
                flFlashcard.setRotationY(0f);
                tvWord.setVisibility(View.VISIBLE);
                tvWord.setRotationY(0f);
                tvWord.setText(currentWord);
                answeredCorrectly = true;
                wordTranslation = translation.toLowerCase();
                pbWordLoading.setVisibility(View.INVISIBLE);
                pbWordLoading.setActivated(false);
                cannotFlip = false;
            }

            @Override
            public void onTranslationFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    void updateDatabase() {
        new Thread(() -> {
            List<UserJoinWord> ujwEntryList = new ArrayList<>();
            ParseUser currentUser = ParseUser.getCurrentUser();

            for (int i = 0; i < wordIndex; i++) {
                WordWrapper wordWrapper = wordList.get(i);
                UserJoinWord ujwEntry;

                // if this word is an automatically created word (no entry for it as indicated by no parent object id),
                // make a new UserJoinWord entry and Word entry for it.
                // else, use the parent object id to get the entry
                if (wordWrapper.getParentObjectId().equals("null")) {
                    ParseQuery<Word> wordQuery = ParseQuery.getQuery(Word.class);
                    wordQuery.whereEqualTo(Word.KEY_ORIGINAL_WORD, wordWrapper.word);
                    Word wordEntry;
                    try {
                        wordEntry = wordQuery.getFirst();
                    } catch (ParseException e) {
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            wordEntry = new Word(wordWrapper.word, wordWrapper.originatesFromId);
                        } else {
                            Log.e(TAG, "Error making new word entry: " + e.toString());
                            continue;
                        }
                    }
                    ujwEntry = new UserJoinWord(currentUser, wordEntry,
                            wordWrapper.getFamiliarityScore(), "algorithm");
                } else {
                    ujwEntry = ParseUser.createWithoutData(UserJoinWord.class,
                            wordWrapper.getParentObjectId());
                }

                ujwEntry.setFamiliarityScore(wordWrapper.getFamiliarityScore());
                ujwEntry.setStruggleIndex(wordWrapper.getStruggleIndex());
                ujwEntry.setGotRightLastTime(wordWrapper.getGotRightLastTime());
                ujwEntry.setStreak(wordWrapper.getStreak());
                ujwEntryList.add(ujwEntry);
            }
            try {
                ParseObject.saveAll(ujwEntryList);
            } catch (ParseException e) {
                Log.e(TAG, "Error saving words " + e.toString());
            }

            Log.d(TAG, "Saved words successfully");

        }).start();
    }

    void createReturnIntent() {
        Intent intent = new Intent();
        // For some reason, Parceler can't parcel sublists, so this is done
        // to make sure that the word list can be passed through
        ArrayList<WordWrapper> tempWordList = new ArrayList<>(wordList);
        intent.putExtra("wordList", Parcels.wrap(tempWordList));
        setResult(RESULT_OK, intent);
    }
}