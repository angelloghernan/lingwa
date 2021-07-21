package com.example.lingwa.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("UserJoinWord")
public class UserJoinWord extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_WORD = "word";
    public static final String KEY_FAMILIARITY_SCORE = "familiarityScore";
    public static final String KEY_SAVED_BY = "savedBy";
    public static final String KEY_STRUGGLE_INDEX = "struggleIndex";

    public UserJoinWord() {

    }

    public UserJoinWord(ParseUser user, Word word, int familiarityScore, String savedBy) {
        this.setWord(word);
        this.setUser(user);
        this.setFamiliarityScore(familiarityScore);
        this.setSavedBy(savedBy);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public int getStruggleIndex() {return getInt(KEY_STRUGGLE_INDEX); }

    public void setStruggleIndex(int amount) { put(KEY_STRUGGLE_INDEX, amount); }


    public Word getWord() {
        Object value = get(KEY_WORD);
        if (!(value instanceof Word)) {
            return null;
        }
        return (Word) value;
    }

    public void setWord(Word word) {
        put(KEY_WORD, word);
    }

    public int getFamiliarityScore() {
        return getInt(KEY_FAMILIARITY_SCORE);
    }

    public void setFamiliarityScore(int score) {
        put(KEY_FAMILIARITY_SCORE, score);
    }

    public String getSavedBy() {
        return getString(KEY_SAVED_BY);
    }

    public void setSavedBy(String savedBy) {
        put(KEY_SAVED_BY, savedBy);
    }
}
