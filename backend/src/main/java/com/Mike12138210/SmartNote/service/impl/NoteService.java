package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.dto.NotePatchRequest;
import com.Mike12138210.SmartNote.dto.NotePermissionRequest;
import com.Mike12138210.SmartNote.entity.Friend;
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
import java.util.*;

@Service
public class NoteService {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteHistoryMapper noteHistoryMapper;
    @Autowired
    private FriendService friendService;

    // 新增笔记
    public void createNote(Note note){
        Long userId = ThreadLocalUtil.get();
        if(userId  == null){throw new RuntimeException("用户未登录，请稍后重试");}

        note.setUserId(userId);
        note.setPermission("仅自己可见"); // 默认状态
        noteMapper.insert(note);
    }

    // 分页查询当前用户笔记
    public Page<Note> listUserNotes(int pageNum,int pageSize,String title,String tag){
        Long userId = ThreadLocalUtil.get();
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
        Long userId = ThreadLocalUtil.get();
        if(userId == null){throw new RuntimeException("用户未登录，请稍后重试。");}

        Note note = noteMapper.selectById(noteId);
        if (note == null) {throw new RuntimeException("该笔记不存在或已被删除，请重试。");}

        if(!note.getUserId().equals(userId)){
            String permission = note.getPermission();
            if("仅自己可见".equals(permission)){
                throw new RuntimeException("对不起，您无权查看此笔记。");
            }else if("仅好友可见".equals(permission)){
                if(!friendService.isFriend(userId,note.getUserId())){
                    throw new RuntimeException("对不起，您无权查看此笔记。");
                }
            }
        }

        NoteHistory history = new NoteHistory();
        history.setUserId(userId);
        history.setNoteId(noteId);
        history.setViewTime(LocalDateTime.now());
        noteHistoryMapper.insert(history);

        return note;
    }

    // 查询公开笔记
    public Note getPublicNote(Long noteId){
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDeleted() == 1) {
            throw new RuntimeException("该笔记不存在或已被删除，请重试。");
        }
        if(!"所有人可见".equals(note.getPermission())){
            throw new RuntimeException("该笔记未公开，无法访问。");
        }
        return note;
    }

    // 查看浏览历史
    public List<Note> getRecentHistory(int limit) {
        Long currentUserId = ThreadLocalUtil.get();
        if(currentUserId == null){
            throw new RuntimeException("用户未登录，请稍后重试。");
        }

        // 查询该用户浏览历史，按时间倒序，限制条数
        LambdaQueryWrapper<NoteHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(NoteHistory::getUserId,currentUserId)
                .orderByDesc(NoteHistory::getViewTime)
                .last(" LIMIT " + limit);
        List<NoteHistory> histories = noteHistoryMapper.selectList(historyWrapper);

        // 如果没有历史记录，直接返回空列表
        if(histories.isEmpty()){
            return new ArrayList<>();
        }

        // 收集所有浏览过的笔记的ID
        List<Long> noteIds = new ArrayList<>();
        for(NoteHistory h : histories){
            noteIds.add(h.getNoteId());
        }

        // 根据ID批量查询笔记
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getNoteId,noteIds);
        List<Note> notes = noteMapper.selectList(noteWrapper);

        //将笔记列表转换成Map，方便按ID快速查找
        Map<Long, Note> noteMap = new HashMap<>();
        for(Note n : notes){
            noteMap.put(n.getNoteId(),n);
        }

        // 根据笔记ID构造最终返回的笔记列表
        List<Note> result = new ArrayList<>();
        for(NoteHistory h : histories){
            Note note = noteMap.get(h.getNoteId());
            if(note == null){ // 笔记可能已被删除
                continue;
            }
            if(note.getDeleted() != null && note.getDeleted() == 1){
                continue;
            }
            if(note.getUserId().equals(currentUserId) || "所有人可见" .equals(note.getPermission())){ // 权限过滤
                result.add(note);
            }
        }

        return result;
    }

    // 编辑笔记
    public Note patchNote(NotePatchRequest request){
        Long currentUserId = ThreadLocalUtil.get();

        Note note = noteMapper.selectById(request.getNoteId());
        if(note == null){
            throw new RuntimeException("笔记不存在，请稍后重试");
        }
        if(!note.getUserId().equals(currentUserId)){
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

    // 删除笔记
    public void deleteNote(Long noteId){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("用户未登录，请稍后重试。");
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

    // 修改笔记权限
    public void updateNotePermission(Long noteId,String permission){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("用户未登录，请稍后重试");
        }

        Note note = noteMapper.selectById(noteId);
        if(note == null){
            throw new RuntimeException("笔记不存在，请稍后重试。");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权修改此笔记的权限");
        }
        if(!Arrays.asList("仅自己可见","仅好友可见","所有人可见").contains(permission)){
            throw new RuntimeException("权限值无效，必须是’仅自己可见‘’仅好友可见‘或’所有人可见‘");
        }
        note.setPermission(permission);
        noteMapper.updateById(note);
    }
}