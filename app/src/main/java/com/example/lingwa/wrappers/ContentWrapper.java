package com.example.lingwa.wrappers;

import org.parceler.Parcel;

@Parcel
public class ContentWrapper {
    public String title;
    public String author;
    public String body;
    public String attachmentUrl;
    public String thumbnailUrl;
    public String contentType;
    public String objectId;

    public ContentWrapper() {
        // required empty default constructor for Parcel
    }

    public ContentWrapper(String objectId, String title, String author) {
        this.objectId = objectId;
        this.title = title;
        this.author = author;
    }
}
