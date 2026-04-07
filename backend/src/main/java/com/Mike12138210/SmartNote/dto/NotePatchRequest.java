package com.Mike12138210.SmartNote.dto;

import jakarta.validation.constraints.NotNull;

public class NotePatchRequest {
    @NotNull(message = "笔记ID不能为空")
    private Long id;
    private String title;
    private String content;
    private String tags; // 可选

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
