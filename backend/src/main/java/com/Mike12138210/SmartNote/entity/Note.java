package com.Mike12138210.SmartNote.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("note")
public class Note {
    // noteId,userId,title,content,tags,permission,aiSummary,aiKeyPoints,createTime,updateTime
    @TableId(value = "note_id", type = IdType.AUTO)
    private Long noteId;
    private Long userId;
    private String title;
    private String content; // markdown
    private String tags; // 逗号分隔
    private String permission; // 仅自己可见,部分好友可见,所有人可见
    private String aiSummary;
    private String aiKeyPoints;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(update = "now()")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    public Long getNoteId() {return noteId;}

    public void setNoteId(Long noteId) {this.noteId = noteId;}

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public String getTags() {return tags;}

    public void setTags(String tags) {this.tags = tags;}

    public String getPermission() {return permission;}

    public void setPermission(String permission) {this.permission = permission;}

    public String getAiSummary() {return aiSummary;}

    public void setAiSummary(String aiSummary) {this.aiSummary = aiSummary;}

    public String getAiKeyPoints() {return aiKeyPoints;}

    public void setAiKeyPoints(String aiKeyPoints) {this.aiKeyPoints = aiKeyPoints;}

    public LocalDateTime getCreateTime() {return createTime;}

    public void setCreateTime(LocalDateTime createTime) {this.createTime = createTime;}

    public LocalDateTime getUpdateTime() {return updateTime;}

    public void setUpdateTime(LocalDateTime updateTime) {this.updateTime = updateTime;}

    public Integer getDeleted() {return deleted;}

    public void setDeleted(Integer deleted) {this.deleted = deleted;}
}