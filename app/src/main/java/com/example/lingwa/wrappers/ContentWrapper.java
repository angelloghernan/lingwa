package com.example.lingwa.wrappers;

import com.example.lingwa.models.Content;

import org.parceler.Parcel;

@Parcel
public class ContentWrapper {
    public static final String TYPE_ARTICLE = "article";

    public String title;
    public String author;
    public String body = null;
    public String attachmentUrl;
    public String thumbnailUrl = null;
    public String contentType;
    public String objectId;
    public String epubPath = null;


    public ContentWrapper() {
        // required empty default constructor for Parcel
    }

    public ContentWrapper(String objectId, String title, String author) {
        this.objectId = objectId;
        this.title = title;
        this.author = author;
    }

    public static ContentWrapper fromContent(Content content) {
        ContentWrapper contentWrapper = new ContentWrapper();

        contentWrapper.objectId = content.getObjectId();
        contentWrapper.title = content.getTitle();
        contentWrapper.author = content.getAuthor();
        contentWrapper.body = content.getBody();
        contentWrapper.contentType = content.getContentType();
        contentWrapper.thumbnailUrl = content.getThumbnail().getUrl();

        if (contentWrapper.contentType.equals(TYPE_ARTICLE)) {
            contentWrapper.body = content.getBody();
        } else {
            contentWrapper.attachmentUrl = content.getAttachment().getUrl();
        }

        return contentWrapper;
    }
}
