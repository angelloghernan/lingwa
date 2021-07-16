package com.example.lingwa.wrappers;

import org.parceler.Parcel;

@Parcel
public class WordWrapper {
    public String word;
    public String objectId;

    public WordWrapper() {

    }
    public WordWrapper(String word, String objectId) {
        this.word = word;
        this.objectId = objectId;
    }
}
