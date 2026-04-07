package com.Mike12138210.SmartNote.controller;

import com.Mike12138210.SmartNote.entity.Note;
import com.Mike12138210.SmartNote.service.impl.NoteService;
import com.Mike12138210.SmartNote.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 查看笔记
    @GetMapping("/{id}")
    public Result<Note> getNoteDetail(@PathVariable Long id){
        Note note = noteService.getNoteDetail(id);
        return Result.success(note);
    }
}
