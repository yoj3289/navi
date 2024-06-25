package com.example.naviproj.model;

public class Upload {
    private String documentId;
    private String contents;
    private String name;
    private String image;
    private String collectionId;
    private String rating;
    private String timestamp;
    private String url;


    public Upload(String documentId, String contents, String name, String image, String collectionId, String timestamp, String url) {
        this.documentId = documentId;
        this.contents = contents;
        this.name = name;
        this.image = image;
        this.collectionId = collectionId;
        this.timestamp = timestamp;
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Upload{" +
                "documentId='" + documentId + '\'' +
                ", contents='" + contents + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", collectionId='" + collectionId + '\'' +
                ", rating='" + rating + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
