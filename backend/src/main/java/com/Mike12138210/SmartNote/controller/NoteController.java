package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.NotePatchRequest;
import com.Mike12138210.SmartNote.dto.NotePermissionRequest;
import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.service.impl.NoteService;
import com.Mike12138210.SmartNote.utils.Result;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import com.Mike12138210.SmartNote.vo.HistoryVO;
import com.Mike12138210.SmartNote.vo.NoteAnalysisVO;
import com.Mike12138210.SmartNote.vo.PublicNoteVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    @Autowired
    public NoteService noteService;

    // 新增笔记
    @PostMapping
    public Result<?> createNote(@RequestBody Note note){
        noteService.createNote(note);
        return Result.success(null);
    }

    // 分页查询笔记
    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "10") int pageSize,
                          @RequestParam(required = false) String title,
                          @RequestParam(required = false) String tag){
        return Result.success(noteService.listUserNotes(pageNum,pageSize,title,tag));
    }

    // 查看“所有人可见”笔记
    @GetMapping("/public/{noteId}")
    public Result<PublicNoteVO> getPublicNote(@PathVariable Long noteId){
        PublicNoteVO publicNote = noteService.getPublicNote(noteId);
        return Result.success(publicNote);
    }

    // 查看笔记
    @GetMapping("/{noteId}")
    public Result<Note> getNoteDetail(@PathVariable Long noteId){
        Note note = noteService.getNoteDetail(noteId);
        return Result.success(note);
    }

    // 查看浏览记录
    @GetMapping("/history/recent")
    public Result<List<HistoryVO>> getRecentHistory(@RequestParam(defaultValue = "10")int limit){
        List<HistoryVO> recentNotes = noteService.getRecentHistory(limit);
        return Result.success(recentNotes);
    }

    // 编辑笔记
    @PatchMapping
    public Result<Note> patchNote(@Valid @RequestBody NotePatchRequest request){
        Note updatedNote = noteService.patchNote(request);
        return Result.success(updatedNote);
    }

    // 删除笔记
    @DeleteMapping("/{noteId}")
    public Result<?> deleteNote(@PathVariable Long noteId){
        noteService.deleteNote(noteId);
        return Result.success(null);
    }

    // 修改笔记权限
    @PutMapping("/{noteId}/permission")
    public Result<?> updateNotePermission(@PathVariable Long noteId,
                                          @RequestBody @Valid NotePermissionRequest request){
        noteService.updateNotePermission(noteId, request.getPermission());
        return Result.success(null);
    }

    // AI分析笔记
    @PostMapping("/{noteId}/analyze")
    public Result<NoteAnalysisVO> analyzeNote(@PathVariable Long noteId,
                                              @RequestParam(required = false,defaultValue = "false") boolean force){
        NoteAnalysisVO result = noteService.analyzeNote(noteId,force);
        return Result.success("AI分析完成，已保存摘要和要点",result);
    }

    // 查看回收站
    @GetMapping("/recycle")
    public Result<List<Note>> getRecycleBin() {
        Long userId = ThreadLocalUtil.get();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        List<Note> recycleNotes = noteService.getRecycleBin(userId);
        return Result.success(recycleNotes);
    }

    // 还原回收站中的笔记
    @PutMapping("/recycle/{noteId}/restore")
    public Result<?> restoreNotes(@PathVariable Long noteId){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("未登录");
        }
        noteService.restoreNote(noteId,userId);
        return Result.success("还原成功",null);
    }
    // 彻底删除
    @DeleteMapping("/recycle/{noteId}/permanent")
    public Result<?> permanentDeleteNote(@PathVariable Long noteId){
        Long userId = ThreadLocalUtil.get();
        if(userId == null){
            throw new RuntimeException("未登录");
        }
        noteService.permanentDeleteNote(noteId,userId);
        return Result.success("该笔记已被彻底删除", null);
    }
}