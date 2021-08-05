package com.example.lingwa.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.lingwa.R;
import com.example.lingwa.adapters.BookAdapter;
import com.example.lingwa.models.Content;
import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.example.lingwa.util.FileUtils;
import com.example.lingwa.util.epubparser.BookSection;
import com.example.lingwa.wrappers.ContentWrapper;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import www.sanju.motiontoast.MotionToast;

public class MyStuffActivity extends AppCompatActivity {

    private static final String TAG = "MyStuffActivity";
    public static final int FLASHCARD_REQUEST_CODE = 10;
    private static final int UPLOAD_REQUEST_CODE = 11;

    boolean wordsLoaded = false;

    ListView lvSavedWords;
    Button btnPractice;
    Button btnUpload;
    ProgressBar pbStuffLoading;
    RecyclerView rvBooks;
    Context context = this;
    List<UserJoinWord> ujwEntryList = null;
    ArrayList<String> displayedWords = null;
    ArrayList<WordWrapper> wordList = new ArrayList<>();

    BookAdapter bookAdapter;
    ArrayList<ContentWrapper> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stuff);
        lvSavedWords = findViewById(R.id.lvSavedWords);
        btnPractice = findViewById(R.id.btnPractice);
        btnUpload = findViewById(R.id.btnUpload);
        pbStuffLoading = findViewById(R.id.pbStuffLoading);
        rvBooks = findViewById(R.id.rvBooks);
        pbStuffLoading.setActivated(false);

        bookAdapter = new BookAdapter(this, bookList, callback);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvBooks.setLayoutManager(layoutManager);
        rvBooks.setAdapter(bookAdapter);

        displayedWords = new ArrayList<>();

        displayUserSavedWords();


        btnPractice.setOnClickListener(startPractice);
        btnUpload.setOnClickListener(uploadBook);

        displayUserBooks();
    }

    // When the practice button is clicked, make sure there are words available for the user before
    // starting an intent to sent them to the flashcards activity
    View.OnClickListener startPractice = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!wordsLoaded) {
                return;
            }

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
            final EditText titleInput = new EditText(this);
            titleInput.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Book Title");
            builder.setView(titleInput);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String title = titleInput.getText().toString();
                Uri uri = data.getData();
                FileUtils fileUtils = new FileUtils();
                String path = fileUtils.getPath(context, uri);
                readyBookContent(title, path);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }

    private void displayUserSavedWords() {
        ParseQuery<UserJoinWord> ujwQuery = ParseQuery.getQuery(UserJoinWord.class);
        ujwQuery.whereEqualTo(UserJoinWord.KEY_USER, ParseUser.getCurrentUser());
        ujwQuery.include("word");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                ujwEntryList = ujwQuery.find();
            } catch (ParseException e) {
                Log.e(TAG, e.toString());
                return;
            }

            for (int i = 0; i < ujwEntryList.size(); i++) {
                UserJoinWord ujwEntry = ujwEntryList.get(i);
                if (ujwEntry.getSavedBy().equals("user")) {
                    displayedWords.add(Objects.requireNonNull(ujwEntry.getWord().getOriginalWord()));
                }
            }

            if (displayedWords.size() < 1) {
                displayedWords.add("No words saved!");
                displayedWords.add("Press and hold on words while reading to save words.");
            }

            // add these words to the wordwrapper list to pass to intent later on
            for (int i = 0; i < ujwEntryList.size(); i++) {
                UserJoinWord ujwEntry = ujwEntryList.get(i);
                WordWrapper wordWrapper = WordWrapper.fromUJW(ujwEntry);
                wordList.add(wordWrapper);
            }

            handler.post(() -> {
                wordsLoaded = true;
                ArrayAdapter<String> savedWordsAdapter =
                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedWords);
                lvSavedWords.setAdapter(savedWordsAdapter);
            });
        });
    }

    private void readyBookContent(String title, String path) {
        ParseFile epubFile = new ParseFile(new File(path));
        pbStuffLoading.setVisibility(View.VISIBLE);
        Content bookContent = new Content();
        bookContent.setAttachment(epubFile);
        bookContent.setUploader(ParseUser.getCurrentUser());
        bookContent.setContentType(Content.TYPE_BOOK);
        bookContent.setTitle(title);
        bookContent.saveInBackground(e -> {
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
        });
    }

    private void displayUserBooks() {
        ParseQuery<Content> bookQuery = ParseQuery.getQuery(Content.class);
        bookQuery.whereEqualTo(Content.KEY_UPLOADER, ParseUser.getCurrentUser());
        bookQuery.whereEqualTo(Content.KEY_TYPE, Content.TYPE_BOOK);
        bookQuery.findInBackground((books, e) -> {
            if (e != null) {
                Log.e(TAG, "Error getting user's books: " + e.toString());
                return;
            }

            for (int i = 0; i < books.size(); i++) {
                ContentWrapper contentWrapper = ContentWrapper.fromContent(books.get(i));
                bookList.add(contentWrapper);
            }

            bookAdapter.notifyDataSetChanged();
        });
    }

    BookAdapter.AdapterCallback callback = (position, book) -> {
      // do stuff when book item clicked
        try {
            pbStuffLoading.setVisibility(View.VISIBLE);
            Intent intent = new Intent(context, ContentActivity.class);

            File bookFile = book.fetchIfNeeded().getParseFile(Content.KEY_ATTACHMENT).getFile();

            ContentWrapper contentWrapper = new ContentWrapper();
            contentWrapper.epubPath = bookFile.getAbsolutePath();
            contentWrapper.contentType = Content.TYPE_BOOK;
            contentWrapper.objectId = book.getObjectId();
            contentWrapper.attachmentUrl = book.getParseFile(Content.KEY_ATTACHMENT).getUrl();
            contentWrapper.title = book.getTitle();

            intent.putExtra("content", Parcels.wrap(contentWrapper));
            pbStuffLoading.setVisibility(View.INVISIBLE);
            startActivity(intent);
        } catch (ParseException | NullPointerException e) {
            Log.e(TAG, "Failed to open book: " + e.toString());
            pbStuffLoading.setVisibility(View.INVISIBLE);
        }
    };

    // permissions currently incomplete but working for purposes of app
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