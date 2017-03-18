package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/3/13.
 */

public class Note {

    public int noteId;
    public String title;
    public String content;
    public String time;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return noteId;
    }

    public void setId(int id) {
        this.noteId = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
