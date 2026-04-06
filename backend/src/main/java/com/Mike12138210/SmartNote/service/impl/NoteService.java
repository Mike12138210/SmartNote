package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.mapper.NoteMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class NoteService {
    @Autowired
    private NoteMapper noteMapper;

    // 获取当前登录用户的ID
    private Long getCurrentUserId(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        if(claims == null){
            return null;
        }
        Object userId = claims.get("userId");
        if(userId == null){
            return null;
        }
        return ((Number)userId).longValue();
    }

    // 新增笔记
    public void createNote(Note note){
        Long userId = getCurrentUserId();
        if(userId  == null){throw new RuntimeException("用户未登录，请稍后重试");}

        note.setUserId(userId);
        note.setPermission("仅自己可见"); // 默认状态
        noteMapper.insert(note);
    }

    // 分页查询当前用户笔记
    public Page<Note> listUserNotes(int pageNum,int pageSize,String title,String tag){
        Long userId = getCurrentUserId();
        if(userId  == null){throw new RuntimeException("用户未登录，请稍后重试");}

        Page<Note> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId,userId)
                .orderByDesc(Note::getUpdateTime); // 按更新时间排序

        if (title != null && !title.isEmpty()) {
            if(StringUtils.hasText(title)){
                wrapper.like(Note::getTitle,title); //包含该title即可
            }
        }
        if (tag != null && !tag.isEmpty()) {
            if(StringUtils.hasText(tag)){
                wrapper.like(Note::getTags,tag); // 包含该标签即可
            }
        }
        return noteMapper.selectPage(page,wrapper); // 查询总数和当前页数据
    }
}