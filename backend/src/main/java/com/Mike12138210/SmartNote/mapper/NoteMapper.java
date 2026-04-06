package com.Mike12138210.SmartNote.mapper;

import com.Mike12138210.SmartNote.entity.Note;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}