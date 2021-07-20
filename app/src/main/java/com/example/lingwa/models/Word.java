package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Word")
public class Word extends ParseObject {
    public static final String KEY_ORIGINAL_WORD = "originalWord";
    public static final String KEY_TRANSLATION = "translation";
    public static final String KEY_ORIGINATES_FROM = "originatesFrom";

    public Word() {

    }

    public Word(String originalWord, Content originatesFrom) {
        this.setOriginalWord(originalWord);
        this.setOriginatesFrom(originatesFrom);
    }

    public Word(String originalWord, String originatesFromId) {
        this.setOriginalWord(originalWord);
        this.setOriginatesFrom(originatesFromId);
    }

    public String getOriginalWord() {
        return getString(KEY_ORIGINAL_WORD);
    }

    public void setOriginalWord(String word) {
        put(KEY_ORIGINAL_WORD, word);
    }

    public Content getOriginatesFrom() {
        Object content = get(KEY_ORIGINATES_FROM);
        if (!(content instanceof Content)) {
            return null;
        }
        return (Content) content;
    }

    public void setOriginatesFrom(Content content) {
        put(KEY_ORIGINATES_FROM, content);
    }

    public void setOriginatesFrom(String contentId) {
        Content content = ParseObject.createWithoutData(Content.class, contentId);
        put(KEY_ORIGINATES_FROM, content);
    }

    public String getTranslation() {
        return getString(KEY_TRANSLATION);
    }

    public void setTranslation(String translation) {
        put(KEY_TRANSLATION, translation);
    }



}
