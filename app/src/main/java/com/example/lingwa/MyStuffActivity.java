package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
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

import com.example.lingwa.models.Content;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.FileUtils;
import com.example.lingwa.util.epubparser.BookSection;
import com.example.lingwa.util.epubparser.Reader;
import com.example.lingwa.util.epubparser.exception.OutOfPagesException;
import com.example.lingwa.util.epubparser.exception.ReadingException;
import com.example.lingwa.wrappers.ContentWrapper;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
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


        // Create a list of word wrappers so we can pass it to an intent later on
        // Todo: make the initialization process a static function in the WordWrapper class (WordWrapper.fromWord)
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
        btnUpload.setOnClickListener(uploadBook);
    }

    // When the practice button is clicked, make sure there are words available for the user before
    // starting an intent to sent them to the flashcards activity
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

    View.OnClickListener uploadBook = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // ActivityResultContracts.RequestPermission requestPermission = new ActivityResultContracts.RequestPermission();
            methodRequiresPermission();
            Intent intent  = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, UPLOAD_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        pbStuffLoading.setVisibility(View.INVISIBLE);
        pbStuffLoading.setActivated(false);


        if (requestCode == FLASHCARD_REQUEST_CODE) {
            // get the new word (wrapper) list so we can use the familiarity scores
            wordList = Parcels.unwrap(data.getParcelableExtra("wordList"));
        } else if (requestCode == UPLOAD_REQUEST_CODE) {
            ContentResolver resolver = context.getContentResolver();

            Uri uri = data.getData();
            FileUtils fileUtils = new FileUtils();
            String path = fileUtils.getPath(this, uri);

            ParseFile epubFile = new ParseFile(new File(path));
            pbStuffLoading.setVisibility(View.VISIBLE);
            Content bookContent = new Content();
            bookContent.setAttachment(epubFile);
            bookContent.setUploader(ParseUser.getCurrentUser());
            bookContent.setContentType(Content.TYPE_BOOK);
            bookContent.setTitle("Test");
            bookContent.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "error saving epub: " + e.toString());
                        return;
                    }
                    Intent intent = new Intent(context, ContentActivity.class);
                    ContentWrapper contentWrapper = new ContentWrapper();
                    contentWrapper.epubPath = path;
                    contentWrapper.contentType = Content.TYPE_BOOK;
                    contentWrapper.objectId = bookContent.getObjectId();
                    contentWrapper.attachmentUrl = null;
                    contentWrapper.title = bookContent.getTitle();

                    intent.putExtra("content", Parcels.wrap(contentWrapper));

                    pbStuffLoading.setVisibility(View.INVISIBLE);

                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(1)
    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return;
        }
        // Do not have permissions, request them now
        EasyPermissions.requestPermissions(this, "Lingwa needs to read external storage to upload epub books.",
                1, perms);
    }
}