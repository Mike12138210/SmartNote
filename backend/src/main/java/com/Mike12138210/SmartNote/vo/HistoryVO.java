package com.Mike12138210.SmartNote.vo;

import com.Mike12138210.SmartNote.entity.Note;

import java.time.LocalDateTime;

public class HistoryVO {
    private Long noteId;
    private String title;
    private String tags;
    private LocalDateTime viewTime;

    public HistoryVO(Note note, LocalDateTime viewTime) {
        this.noteId = note.getNoteId();
        this.title = note.getTitle();
        this.tags = note.getTags();
        this.viewTime = viewTime;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getViewTime() {
        return viewTime;
    }

    public void setViewTime(LocalDateTime viewTime) {
        this.viewTime = viewTime;
    }
}
