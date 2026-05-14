package com.example.copilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.copilot.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
