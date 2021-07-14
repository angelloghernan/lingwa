package com.example.lingwa.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Content")
public class Content extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_THUMBNAIL = "thumbnail";
    public static final String KEY_UPLOADER = "uploader";
    public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TYPE = "type";
    public static final String KEY_BODY = "body";
    public static final String KEY_ATTACHMENT = "attachment";

    public static final String TYPE_BOOK = "book";
    public static final String TYPE_ARTICLE = "article";

    public String getTitle() { return getString(KEY_TITLE);}

    public void setTitle(String title) { put(KEY_TITLE, title); }

    public String getContentType() { return getString(KEY_TYPE); }

    // Type can either be TYPE_BOOK or TYPE_ARTICLE
    public void setContentType(String type) { put(KEY_TYPE, type); }

    // There should only be a body if the content is an article
    public String getBody() { return getString(KEY_BODY); }

    public void setBody(String body) { put(KEY_BODY, body); }

    // There should only be an attachment if the content is a book
    public ParseFile getAttachment() { return getParseFile(KEY_ATTACHMENT); }

    public void setAttachment(ParseFile attachment) { put(KEY_ATTACHMENT, attachment); }

    public String getAuthor() { return getString(KEY_AUTHOR); }

    public void setAuthor(String author) { put(KEY_AUTHOR, author); }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getThumbnail() {
        return getParseFile(KEY_THUMBNAIL);
    }

    public void setThumbnail(ParseFile parseFile) {
        put(KEY_THUMBNAIL, parseFile);
    }

    public ParseUser getUploader() { return getParseUser(KEY_UPLOADER); }

    public void setUploader(ParseUser parseUser) { put(KEY_UPLOADER, parseUser); }
}
