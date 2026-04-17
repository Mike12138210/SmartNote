package com.Mike12138210.SmartNote.vo;

import com.Mike12138210.SmartNote.entity.Note;

public class PublicNoteVO {
    private Long noteId;
    private String title;
    private String content;
    private String tags;
    private String permission;

    public PublicNoteVO(Note note) {
        this.noteId = note.getNoteId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.tags = note.getTags();
        this.permission = note.getPermission();
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
