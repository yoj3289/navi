package com.example.naviproj.model;

import android.provider.ContactsContract;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class DiaryPage {

    private String collectionId;
    private String documentId;
    private String name;
    private String title;
    private String image;
    private String contents;
    private String timestamp;
    private String url;
    private String emoji;

    @ServerTimestamp
    private Date time;


    public DiaryPage() {
    }

    public DiaryPage(String collectionId, String documentId, String name, String title, String image, String contents, String timestamp, String url, Date time) {
        this.collectionId = collectionId;
        this.documentId = documentId;
        this.name = name;
        this.title = title;
        this.image = image;
        this.contents = contents;
        this.timestamp = timestamp;
        this.url = url;
        this.time = time;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return url;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    @Override
    public String toString() {
        return "DiaryPage{" +
                "collectionId='" + collectionId + '\'' +
                ", documentId='" + documentId + '\'' +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", contents='" + contents + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", url='" + url + '\'' +
                ", time=" + time +
                '}';
    }
}