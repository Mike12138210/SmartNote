package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.dto.NotePatchRequest;
import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.entity.NoteHistory;
import com.Mike12138210.SmartNote.mapper.NoteHistoryMapper;
import com.Mike12138210.SmartNote.mapper.NoteMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NoteService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteHistoryMapper noteHistoryMapper;

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

    // 查询笔记详情（同时记录浏览历史）
    public Note getNoteDetail(Long noteId){
        Long userId = getCurrentUserId();
        if(userId == null){throw new RuntimeException("用户未登录，请稍后重试。");}

        Note note = noteMapper.selectById(noteId);
        if (note == null) {throw new RuntimeException("该笔记不存在或已被删除，请重试。");}

        if(!note.getUserId().equals(userId)){
            if("仅自己可见".equals(note.getPermission())){
                throw new RuntimeException("对不起，您无权查看此笔记。");
            }
            // if("仅好友可见".equals(note.getPermission())){}
        }

        NoteHistory history = new NoteHistory();
        history.setUserId(userId);
        history.setNoteId(noteId);
        history.setViewTime(LocalDateTime.now());
        noteHistoryMapper.insert(history);

        return note;
    }

    // 编辑笔记
    public Note patchNote(NotePatchRequest request){
        Long userId = getCurrentUserId();

        Note note = noteMapper.selectById(request.getNoteId());
        if(note == null){
            throw new RuntimeException("笔记不存在，请稍后重试");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权编辑此笔记。");
        }

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }

        note.setUpdateTime(LocalDateTime.now()); // 自动填充未生效，手动修改更新时间
        noteMapper.updateById(note);

        return noteMapper.selectById(note.getNoteId());
    }

    public void deleteNote(Long noteId){
        Long userId = getCurrentUserId();
        if(userId == null){
            throw new RuntimeException("未登录。");
        }

        Note note = noteMapper.selectById(userId);
        if(note == null){
            throw new RuntimeException("笔记不存在或已被删除，请稍后重试。");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权删除此笔记。");
        }

        int row = noteMapper.deleteById(noteId);
        if(row == 0){
            throw new RuntimeException("删除失败，请重试。");
        }
    }
}