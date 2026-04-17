package com.Mike12138210.SmartNote.service.impl;

import com.Mike12138210.SmartNote.dto.NotePatchRequest;
import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.entity.NoteHistory;
import com.Mike12138210.SmartNote.mapper.NoteHistoryMapper;
import com.Mike12138210.SmartNote.mapper.NoteMapper;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.HistoryVO;
import com.Mike12138210.SmartNote.vo.NoteAnalysisVO;
import com.Mike12138210.SmartNote.vo.PublicNoteVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {
    public static final Logger log = LoggerFactory.getLogger(NoteService.class);

    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private NoteHistoryMapper noteHistoryMapper;
    @Autowired
    private FriendService friendService;
    @Autowired
    private AiService aiService;
    @Autowired
    private RateLimitService rateLimitService;

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
                .eq(Note::getDeleted,0)
                .orderByDesc(Note::getUpdateTime); // 按更新时间倒序排序

        if(StringUtils.hasText(title)){
            wrapper.like(Note::getTitle,title); //包含该title即可
        }
        if(StringUtils.hasText(tag)){
            wrapper.like(Note::getTags,tag); // 包含该标签即可
        }

        return noteMapper.selectPage(page,wrapper); // 查询总数和当前页数据
    }

    // 查询笔记详情（同时记录浏览历史）
    public Note getNoteDetail(Long noteId){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){throw new RuntimeException("用户未登录，请稍后重试");}

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId,noteId)
                .eq(Note::getDeleted,0);
        Note note = noteMapper.selectOne(wrapper);

        if (note == null) {throw new RuntimeException("该笔记不存在或已被删除，请重试");}

        if(!note.getUserId().equals(userId)){
            String permission = note.getPermission();
            if("仅自己可见".equals(permission)){
                throw new RuntimeException("对不起，您无权查看此笔记");
            }else if("仅好友可见".equals(permission)){
                if(!friendService.isFriend(userId,note.getUserId())){
                    throw new RuntimeException("对不起，您无权查看此笔记");
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

    // 查看“所有人可见”笔记
    public PublicNoteVO getPublicNote(Long noteId){
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId,noteId)
                .eq(Note::getDeleted,0);
        Note note = noteMapper.selectOne(wrapper);

        if (note == null || note.getDeleted() == 1) {
            throw new RuntimeException("该笔记不存在或已被删除，请重试");
        }
        if(!"所有人可见".equals(note.getPermission())){
            throw new RuntimeException("该笔记未公开，无法访问");
        }
        return new PublicNoteVO(note);
    }

    // 查看浏览历史
    public List<HistoryVO> getRecentHistory(int limit) {
        Long currentUserId = ThreadLocalUtil.get();
        if(currentUserId == null){
            throw new RuntimeException("用户未登录，请稍后重试");
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
        List<Long> noteIds = histories.stream()
                .map(NoteHistory::getNoteId)
                .collect(Collectors.toList());

        // 根据ID批量查询笔记
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getNoteId,noteIds)
                .eq(Note::getDeleted,0);
        List<Note> notes = noteMapper.selectList(noteWrapper);

        //将笔记列表转换成Map，方便按ID快速查找
        Map<Long,Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getNoteId,note -> note));

        // 根据笔记ID构造最终返回的笔记列表
        List<HistoryVO> result = new ArrayList<>();
        for(NoteHistory history : histories){
            Note note = noteMap.get(history.getNoteId());
            if(note != null){
                result.add(new HistoryVO(note, history.getViewTime()));
            }
        }

        return result;
    }

    // 编辑笔记
    public Note patchNote(NotePatchRequest request){
        Long currentUserId = ThreadLocalUtil.get();

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId,request.getNoteId())
                .eq(Note::getDeleted,0);
        Note note = noteMapper.selectOne(wrapper);

        if(note == null){
            throw new RuntimeException("笔记不存在，请稍后重试");
        }
        if(!note.getUserId().equals(currentUserId)){
            throw new RuntimeException("对不起，您无权编辑此笔记");
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
        noteMapper.updateById(note);

        return noteMapper.selectById(note.getNoteId());
    }

    // 删除笔记
    public void deleteNote(Long noteId){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("用户未登录，请稍后重试");
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId, noteId)
                .eq(Note::getDeleted, 0);
        Note note = noteMapper.selectOne(wrapper);

        if(note == null){
            throw new RuntimeException("笔记不存在或已被删除，请稍后重试");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权删除此笔记");
        }

        note.setDeleted(1);
        note.setDeleteTime(LocalDateTime.now());
        noteMapper.updateById(note);
    }

    // 修改笔记权限
    public void updateNotePermission(Long noteId,String permission){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("用户未登录，请稍后重试");
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId, noteId)
                .eq(Note::getDeleted, 0);
        Note note = noteMapper.selectOne(wrapper);
        if(note == null){
            throw new RuntimeException("笔记不存在，请稍后重试");
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

    // 调用AI分析笔记
    public NoteAnalysisVO analyzeNote(Long noteId,boolean force){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("用户未登录，请稍后重试");
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getNoteId, noteId)
                .eq(Note::getDeleted, 0);
        Note note = noteMapper.selectOne(wrapper);
        if(note == null){
            throw new RuntimeException("对不起，该笔记不存在");
        }

        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权分析此笔记");
        }
        if (!force && note.getAiSummary() != null && note.getAiKeyPoints() != null) {
            // 已有结果且不强制，直接返回已有结果
            List<String> keyPoints = null;
            try {
                keyPoints = new ObjectMapper().readValue(note.getAiKeyPoints(), List.class);
            } catch (Exception e) { /* 忽略 */ }
            return new NoteAnalysisVO(note.getAiSummary(), keyPoints);
        }

        rateLimitService.check(userId);
        // 调用AI服务
        Map<String, Object> aiResult = aiService.generateSummaryAndKeyPoints(note.getContent());
        String summary = (String) aiResult.get("summary");
        List<String> keyPoints = (List<String>) aiResult.get("keyPoints");
        // 将keyPoints列表转为JSON字符串存储
        String keyPointsJson = null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            keyPointsJson = mapper.writeValueAsString(keyPoints);
        }catch(Exception e){
            throw new RuntimeException("对不起，储存要点失败");
        }

        note.setAiSummary(summary);
        note.setAiKeyPoints(keyPointsJson);
        noteMapper.updateById(note);
        return new NoteAnalysisVO(summary,keyPoints);
    }

    // 查看回收站列表
    public List<Note> getRecycleBin(Long userId){
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId,userId)
                .eq(Note::getDeleted,1)
                .orderByDesc(Note::getDeleteTime); // 按删除时间倒序
        return noteMapper.selectList(wrapper);
    }

    // 还原回收站中的笔记
    public void restoreNote(Long noteId,Long userId){
        Note note = noteMapper.selectById(noteId);
        if(note == null){
            throw new RuntimeException("对不起，该笔记不存在");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权进行此操作");
        }
        if(note.getDeleted() != 1){
            throw new RuntimeException("对不起，该笔记不在回收站中");
        }

        // 使用 LambdaUpdateWrapper 将 deleted 改回 0，清空 delete_time
        LambdaUpdateWrapper<Note> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Note::getNoteId, noteId)
                .set(Note::getDeleted, 0)
                .set(Note::getDeleteTime, null);
        noteMapper.update(null, updateWrapper);
    }

    // 定时任务
    @Scheduled(fixedDelay = 60000)  // 每分钟执行一次
    public void autoClearRecycleBin(){
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(5);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getDeleted,1)
                .le(Note::getDeleteTime,expireTime);
        List<Note> expireNotes = noteMapper.selectList(wrapper);
        for(Note note : expireNotes){
            noteMapper.deleteById(note.getNoteId());
            log.info("自动清空回收站笔记：{} （ID={}）",note.getTitle(),note.getNoteId());
        }
    }

    // 彻底删除回收站笔记
    public void permanentDeleteNote(Long noteId,Long userId){
        Note note = noteMapper.selectById(noteId);
        if(note == null){
            throw new RuntimeException("对不起，该笔记不存在");
        }
        if(!note.getUserId().equals(userId)){
            throw new RuntimeException("对不起，您无权进行此操作");
        }
        if(note.getDeleted() != 1){
            throw new RuntimeException("对不起，该笔记不在回收站中");
        }
        noteMapper.deleteById(noteId);
    }
}