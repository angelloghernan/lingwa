package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Word")
public class Word extends ParseObject {
    public static final String KEY_ORIGINAL_WORD = "originalWord";
    public static final String KEY_TRANSLATION = "translation";

    public Word() {

    }

    public Word(String originalWord) {
        this.setOriginalWord(originalWord);
    }

    public String getOriginalWord() {
        return getString(KEY_ORIGINAL_WORD);
    }

    public void setOriginalWord(String word) {
        put(KEY_ORIGINAL_WORD, word);
    }

    public String getTranslation() {
        return getString(KEY_TRANSLATION);
    }

    public void setTranslation(String translation) {
        put(KEY_TRANSLATION, translation);
    }



}
