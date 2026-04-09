package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.dto.NotePatchRequest;
import com.Mike12138210.SmartNote.dto.NotePermissionRequest;
import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.service.impl.NoteService;
import com.Mike12138210.SmartNote.utils.Result;
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
    public Result<Note> getPublicNote(@PathVariable Long noteId){
        Note note = noteService.getPublicNote(noteId);
        return Result.success(note);
    }

    // 查看笔记
    @GetMapping("/{noteId}")
    public Result<Note> getNoteDetail(@PathVariable Long noteId){
        Note note = noteService.getNoteDetail(noteId);
        return Result.success(note);
    }

    // 查看浏览记录
    @GetMapping("/history/recent")
    public Result<List<Note>> getRecentHistory(@RequestParam(defaultValue = "10")int limit){
        List<Note> recentNote = noteService.getRecentHistory(limit);
        return Result.success(recentNote);
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
    public Result<?> updateNotePermission(@PathVariable Long noteId,@RequestBody @Valid NotePermissionRequest request){
        noteService.updateNotePermission(noteId, request.getPermission());
        return Result.success(null);
    }
}