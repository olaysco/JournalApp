package com.android.olayiwola.journalapp.Data;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by olayiwola on 6/28/2018.
 */

public class JournalEntry {

    private String id;
    private String userId;
    private String title;
    private String content;
    private Date lastModifiedDate;
    @ServerTimestamp
    Date createdDate;

    public JournalEntry(String id, String title, String content, Date lastModifiedDate) {
        this.userId = id;
        this.title = title;
        this.content = content;
        this.lastModifiedDate = lastModifiedDate;
    }

    public JournalEntry() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
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

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }


    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
