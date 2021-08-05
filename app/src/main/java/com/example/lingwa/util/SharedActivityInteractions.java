package com.example.lingwa.util;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.lingwa.models.UserJoinWord;
import com.example.lingwa.models.Word;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SharedActivityInteractions {

    private static final String TAG = "SharedInteractions";
    public static final int NUM_WORDS = 10;

    // After a challenge, prompt the user to save any words they failed to get right on their first try
    // (they can always refuse to save any if they wish not to)
    public static void promptUserToSaveOpponentWords(Context context, List<String> failedWords) {
        if (failedWords == null || failedWords.size() == 0) {
            return;
        }

        boolean[] checkedItems = new boolean[NUM_WORDS];
        String[] words = failedWords.toArray(new String[0]);
        List<String> selectedWords = new ArrayList<>();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Do you want to save any of these words?");
        dialogBuilder.setMultiChoiceItems(words, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedWords.add(words[which]);
            } else {
                selectedWords.remove(words[which]);
            }
        });
        dialogBuilder.setPositiveButton("Save", (dialog, which) -> {
            if (selectedWords.size() == 0) {
                return;
            }

            ParseQuery<UserJoinWord> innerQuery = ParseQuery.getQuery(UserJoinWord.class);
            innerQuery.whereEqualTo(UserJoinWord.KEY_USER, ParseUser.getCurrentUser());
            innerQuery.include(UserJoinWord.KEY_WORD);

            ParseQuery<Word> wordQuery = ParseQuery.getQuery(Word.class);
            wordQuery.whereContainedIn(Word.KEY_ORIGINAL_WORD, selectedWords);
            wordQuery.whereDoesNotMatchKeyInQuery(Word.KEY_OBJECT_ID, "word.objectId", innerQuery);
            wordQuery.findInBackground((capturedWords, e) -> {
                if (e != null && e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    Log.e(TAG, "Error getting words when saving: " + e.toString());
                    return;
                }
                List<UserJoinWord> entriesToSave = new ArrayList<>();
                for (int i = 0; i < capturedWords.size(); i++) {
                    UserJoinWord ujwEntry = new UserJoinWord(ParseUser.getCurrentUser(),
                            capturedWords.get(i),
                            0,
                            "user");
                    entriesToSave.add(ujwEntry);
                }
                UserJoinWord.saveAllInBackground(entriesToSave);
            });
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialogBuilder.show();
        }
}
