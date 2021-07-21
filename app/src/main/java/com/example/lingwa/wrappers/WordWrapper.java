package com.example.lingwa.wrappers;

import org.parceler.Parcel;

@Parcel
public class WordWrapper {
    public String word;
    public String objectId;
    public String parentSavedBy;
    public String originatesFromId;
    String parentObjectId = "null";
    int familiarityScore;
    int struggleIndex = 0;

    public WordWrapper() {

    }
    public WordWrapper(String word, String objectId, String parentSavedBy, String originatesFromId) {
        this.word = word;
        this.objectId = objectId;
        this.parentSavedBy = parentSavedBy;
        this.originatesFromId = originatesFromId;
    }

    public void setFamiliarityScore(int familiarityScore) {
        this.familiarityScore = familiarityScore;
    }

    public void setStruggleIndex(int struggleIndex) {
        this.struggleIndex = struggleIndex;
    }

    public int getStruggleIndex() {
        return this.struggleIndex;
    }

    public int getFamiliarityScore() {
        return this.familiarityScore;
    }

    public void setParentObjectId(String parentObjectId) {
        this.parentObjectId = parentObjectId;
    }

    public String getParentObjectId() {
        return this.parentObjectId;
    }
}
