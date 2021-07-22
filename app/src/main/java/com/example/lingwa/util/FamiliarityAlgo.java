package com.example.lingwa.util;

import android.util.Log;

import com.example.lingwa.models.Content;
import com.example.lingwa.models.Word;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FamiliarityAlgo {
    public static final String KEY_RECENT_ARTICLES = "recentArticles";
    private static final String TAG = "FamiliarityAlgo";
    boolean articleFetchFailed = false;

    public List<WordWrapper> calculateQuizOrder(List<WordWrapper> wordWrapperList, int numItems) throws JSONException, ParseException {

        if (numItems > wordWrapperList.size()) {
            List<WordWrapper> newWords = getNewWords(numItems - wordWrapperList.size(), wordWrapperList);
            if (newWords != null) {
                wordWrapperList.addAll(newWords);
            } else {
                articleFetchFailed = true;
            }
        }

        // Sort in descending order of priority
        Collections.sort(wordWrapperList, new SortByPriority());

        if (articleFetchFailed || numItems > wordWrapperList.size()) {
            return wordWrapperList;
        }

        return wordWrapperList.subList(0, numItems - 1);
    }

    public List<WordWrapper> getNewWords(int numWords, List<WordWrapper> wordWrapperList) throws JSONException, ParseException {
        JSONArray jsonArray = ParseUser.getCurrentUser().getJSONArray(KEY_RECENT_ARTICLES);
        String contentId;


        if (jsonArray != null) {
            contentId = jsonArray.getString(jsonArray.length() - 1);
        } else {
            return null;
        }

        Content content = ParseObject.createWithoutData(Content.class, contentId);
        ParseQuery<Word> originatesFromQuery = ParseQuery.getQuery(Word.class);
        originatesFromQuery.whereEqualTo(Word.KEY_ORIGINATES_FROM, content);

        Set<String> wordsAlreadyAdded = new HashSet<>();

        for (int i = 0; i < wordWrapperList.size(); i++) {
            wordsAlreadyAdded.add(wordWrapperList.get(i).word);
        }

        try {
            List<Word> words = originatesFromQuery.find();
            List<WordWrapper> newWords = new ArrayList<>();
            for (int i = 0; i < numWords && i < words.size(); i++) {
                Word word = words.get(i);
                WordWrapper wordWrapper = new WordWrapper(word.getOriginalWord(), word.getObjectId(), "algorithm",
                        content.getObjectId());
                wordWrapper.setParentObjectId("null");
                wordWrapper.setFamiliarityScore(1);
                newWords.add(wordWrapper);
                wordsAlreadyAdded.add(wordWrapper.word);
            }
            if (numWords < words.size()) {
                return newWords;
            } else {
                numWords -= words.size();
            }
        } catch (ParseException e) {
            if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                Log.e(TAG, "Error fetching like-words from article: " + e.toString());
            }
        }

        ParseQuery<Content> contentQuery = ParseQuery.getQuery(Content.class);
        contentQuery.whereEqualTo(Content.KEY_OBJECT_ID, contentId);

        content = contentQuery.getFirst();

        // remove any non-letter characters from the body and split into words
        // note: in the future, this is going to require taking only the first X characters
        // of a given article since it will be too long to do this quickly, but for now this works
        String[] contentBodyWords = Objects.requireNonNull(content.getBody())
                                            .replaceAll("[^\\p{L} ]", " ")
                                            .split(" ");

        Arrays.sort(contentBodyWords, new SortByStringLength());

        List<WordWrapper> newWords = new ArrayList<>();

        for (int i = 0; i < numWords; i++) {
            if (wordsAlreadyAdded.contains(contentBodyWords[i])) {
                numWords++;
                continue;
            }

            WordWrapper wordWrapper = new WordWrapper(contentBodyWords[i], "null", "unsaved",
                    content.getObjectId());
            wordWrapper.setFamiliarityScore(1);
            wordWrapper.setParentObjectId("null");
            newWords.add(wordWrapper);

            wordsAlreadyAdded.add(contentBodyWords[i]);
        }

        return newWords;
    }
}

class SortByPriority implements Comparator<WordWrapper> {
    public static final String BY_ALGORITHM = "algorithm";
    public static final String BY_USER = "user";
    public static final String BY_NOBODY = "unsaved";
    public static final int MAX_FAMILIARITY = 5;
    public static final int MAX_STRUGGLE_INDEX = 5;

    @Override
    public int compare(WordWrapper o1, WordWrapper o2) {
        int o2Priority = calculatePriorityScore(o2);
        int o1Priority = calculatePriorityScore(o1);

        if (o2Priority == o1Priority) {
            return o2.word.length() - o1.word.length();
        }
        return o2Priority - o1Priority;
    }

    // calculate priority score for a given word
    // based on complexity, familiarity, and source
    // currently simple
    public int calculatePriorityScore(WordWrapper wordWrapper) {
        int priorityScore;

        int wordLength = wordWrapper.word.length();

        if (wordWrapper.parentSavedBy.equals(BY_NOBODY)) {
            return wordLength;
        }

        priorityScore = wordLength;
        priorityScore += 5 * (MAX_FAMILIARITY - wordWrapper.getFamiliarityScore());

        if (wordWrapper.parentSavedBy.equals(BY_USER)) {
            priorityScore *= 2;
        }

        if (wordWrapper.getGotRightLastTime()) {
            priorityScore -= 2 * wordWrapper.getStreak() * wordWrapper.getFamiliarityScore();
        } else {
            priorityScore += wordWrapper.getStreak() * wordWrapper.getStruggleIndex();
        }

        return priorityScore;
    }
}

class SortByStringLength implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return o2.length() - o1.length();
    }
}
