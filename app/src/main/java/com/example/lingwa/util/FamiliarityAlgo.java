package com.example.lingwa.util;

import com.example.lingwa.models.Content;
import com.example.lingwa.models.Word;
import com.example.lingwa.wrappers.WordWrapper;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FamiliarityAlgo {
    public static final String KEY_RECENT_ARTICLES = "recentArticles";
    private static final String TAG = "FamiliarityAlgo";
    boolean articleFetchFailed = false;

    public List<WordWrapper> calculateQuizOrder(List<WordWrapper> wordWrapperList, int numItems) throws JSONException, ParseException {

        if (numItems > wordWrapperList.size()) {
            List<WordWrapper> newWords = getNewWords(numItems - wordWrapperList.size());
            if (newWords != null) {
                wordWrapperList.addAll(newWords);
            } else {
                articleFetchFailed = true;
            }
        }

        int size = wordWrapperList.size();

        List<WordWrapper> newList = new ArrayList<>(size);

        // Sort in descending order of familiarity
        Collections.sort(wordWrapperList, new SortByPriority());

        int remainder = size % 3;

        int divider = (int) size / 3;

        for (int i = 0; i < divider; i++) {
            // get a familiar word
            newList.add(wordWrapperList.get(i));
            // get a less familiar word
            newList.add(wordWrapperList.get(divider + i));
            // get an unfamiliar word
            newList.add(wordWrapperList.get((divider * 2) + i));
        }

        for (int i = 0; i < remainder; i++) {
            // add the two least familiar words at the end if
            // there are any left
            newList.add(wordWrapperList.get(size - (i + 1)));
        }

        if (articleFetchFailed) {
            return newList;
        }

        return newList.subList(0, numItems - 1);
    }

    public List<WordWrapper> getNewWords(int numWords) throws JSONException, ParseException {
        JSONArray jsonArray = ParseUser.getCurrentUser().getJSONArray(KEY_RECENT_ARTICLES);
        String contentId;
        if (jsonArray != null) {
            contentId = jsonArray.getString(0);
        } else {
            return null;
        }

        ParseQuery<Content> contentQuery = ParseQuery.getQuery(Content.class);
        contentQuery.whereEqualTo(Content.KEY_OBJECT_ID, contentId);

        Content content = contentQuery.getFirst();

        // remove any non-letter characters from the body and split into words
        // note: in the future, this is going to require taking only the first X characters
        // of a given article since it will be too long to do this quickly, but for now this works
        String[] contentBodyWords = Objects.requireNonNull(content.getBody())
                                            .replaceAll("[^\\p{L} ]", " ")
                                            .split(" ");

        Arrays.sort(contentBodyWords, new SortByStringLength());

        List<WordWrapper> newWords = new ArrayList<>();

        for (int i = 0; i < numWords; i++) {
            Word word = new Word(contentBodyWords[i]);
            word.setObjectId("null");
            WordWrapper wordWrapper = new WordWrapper(contentBodyWords[i], "null", "unsaved");
            wordWrapper.setFamiliarityScore(1);
            wordWrapper.setParentObjectId("null");
            newWords.add(wordWrapper);
        }

        return newWords;
    }
}

class SortByPriority implements Comparator<WordWrapper> {
    public static final String BY_ALGORITHM = "algorithm";
    public static final String BY_USER = "user";
    public static final String BY_NOBODY = "unsaved";

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
        int priorityScore = 1;

        if (wordWrapper.parentSavedBy.equals(BY_USER)) {
            priorityScore++;
        } else if (wordWrapper.parentSavedBy.equals(BY_NOBODY)) {
            priorityScore--;
        }

        int wordLength = wordWrapper.word.length();

        priorityScore *= wordLength;
        priorityScore -= wordLength * wordWrapper.getFamiliarityScore();

        return priorityScore;
    }
}

class SortByStringLength implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return o2.length() - o1.length();
    }
}
