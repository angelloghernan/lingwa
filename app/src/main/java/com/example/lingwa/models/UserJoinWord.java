package com.example.lingwa.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.lingwa.R;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import www.sanju.motiontoast.MotionToast;

@ParseClassName("UserJoinWord")
public class UserJoinWord extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String KEY_WORD = "word";
    public static final String KEY_FAMILIARITY_SCORE = "familiarityScore";
    public static final String KEY_SAVED_BY = "savedBy";
    public static final String KEY_STRUGGLE_INDEX = "struggleIndex";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_GOT_RIGHT_LAST_TIME = "gotRightLastTime";
    private static final String TAG = "UserJoinWord";

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

    public int getStreak () {return getInt(KEY_STREAK); }

    public void setStreak(int num) { put(KEY_STREAK, num); }

    public boolean getGotRightLastTime() {return getBoolean(KEY_GOT_RIGHT_LAST_TIME);}

    public void setGotRightLastTime(boolean val) {put(KEY_GOT_RIGHT_LAST_TIME, val);}


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
