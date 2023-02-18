package com.example.blogpostapp;

import android.view.View;

import com.google.firebase.firestore.Exclude;

import java.util.UUID;

public class BlogPost {
    private String title, content, UUID, username;
    @Exclude
    private String key;

    public BlogPost(String title, String content, String UUID, String username) {
        this.title = title;
        this.content = content;
        this.UUID = UUID;
        this.username = username;
    }

    public BlogPost() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
