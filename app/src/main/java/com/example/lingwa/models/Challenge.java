package com.example.lingwa.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

@ParseClassName("Challenge")
public class Challenge extends ParseObject {
    public static final String KEY_CHALLENGER = "challenger";
    public  static final String KEY_CHALLENGED = "challenged";
    public static final String KEY_CHALLENGER_WORDS = "challengerWords";
    public static final String KEY_CHALLENGED_WORDS = "challengedWords";
    public static final String SUFFIX_WORDS = "Words";
    public static final String KEY_CHALLENGER_READY = "challengerReady";
    public static final String KEY_CHALLENGED_READY = "challengedReady";
    public static final String SUFFIX_READY = "Ready";
    public static final String KEY_CHALLENGER_PROGRESS = "challengerProgress";
    public static final String KEY_CHALLENGED_PROGRESS = "challengedProgress";
    public static final String SUFFIX_PROGRESS = "Progress";
    public static final String KEY_CHALLENGER_ANSWER = "challengerAnswer";
    public static final String KEY_CHALLENGED_ANSWER = "challengedAnswer";
    public static final String SUFFIX_ANSWER = "Answer";
    public static final String SUFFIX_WORDS_SELECTED = "SelectedWords";

    public ParseUser getChallenger() { return getParseUser(KEY_CHALLENGER); };
    public void setChallenger(ParseUser challenger) { put(KEY_CHALLENGER, challenger); }

    public ParseUser getChallenged() { return getParseUser(KEY_CHALLENGED); }
    public void setChallenged(ParseUser challenged) { put(KEY_CHALLENGED, challenged); }

    public String getWord(String fromWho, int index) throws JSONException, NullPointerException {
        return getJSONArray(fromWho + SUFFIX_WORDS).getString(index);
    }

    public JSONArray getWords(String fromWho) {
        return getJSONArray(fromWho + SUFFIX_WORDS);
    }

    public void setWords(String forWho, List<String> words) { addAll(forWho + SUFFIX_WORDS, words); }

    public int getProgress(String fromWho) {
        return getInt(fromWho + SUFFIX_PROGRESS);
    }

    public void setProgress(String forWho, int progress) {
        put(forWho + SUFFIX_PROGRESS, progress);
    }

    // Use either KEY_CHALLENGER or KEY_CHALLENGED
    public boolean getReady(String fromWho) {
        return getBoolean(fromWho + SUFFIX_READY);
    }

    public void setReady(String forWho, boolean value) {
        put(forWho + SUFFIX_READY, value);
    }

    public boolean arePlayerWordsSelected(String fromWho) { return getBoolean(fromWho + SUFFIX_WORDS_SELECTED); }

    public void setArePlayerWordsSelected(String forWho, boolean value) { put(forWho + SUFFIX_WORDS_SELECTED, value); }

    public String getAnswer(String fromWho) {
        return getString(fromWho + SUFFIX_ANSWER);
    }

    public void setAnswer(String toWho, String answer) {
        put(toWho + SUFFIX_ANSWER, answer);
    }
}
