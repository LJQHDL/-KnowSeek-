package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.copilot.dto.response.LogMessageResponse;
import com.example.copilot.dto.response.LogRetrievalResponse;
import com.example.copilot.entity.ChatSession;
import com.example.copilot.entity.KnowledgeBase;
import com.example.copilot.entity.Message;
import com.example.copilot.entity.RetrievalLog;
import com.example.copilot.mapper.ChatSessionMapper;
import com.example.copilot.mapper.KnowledgeBaseMapper;
import com.example.copilot.mapper.MessageMapper;
import com.example.copilot.mapper.RetrievalLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final MessageMapper messageMapper;
    private final RetrievalLogMapper retrievalLogMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public LogService(MessageMapper messageMapper,
                      RetrievalLogMapper retrievalLogMapper,
                      ChatSessionMapper chatSessionMapper,
                      KnowledgeBaseMapper knowledgeBaseMapper) {
        this.messageMapper = messageMapper;
        this.retrievalLogMapper = retrievalLogMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    public List<LogMessageResponse> queryMessages(int page, int pageSize, Long knowledgeBaseId,
                                                   LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .orderByDesc(Message::getCreatedAt);

        if (knowledgeBaseId != null) {
            List<Long> sessionIds = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                            .eq(ChatSession::getKnowledgeBaseId, knowledgeBaseId)
                            .select(ChatSession::getId))
                    .stream().map(ChatSession::getId).toList();
            if (sessionIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(Message::getSessionId, sessionIds);
        }
        if (startDate != null) {
            wrapper.ge(Message::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(Message::getCreatedAt, endDate);
        }

        Page<Message> result = messageMapper.selectPage(Page.of(page, pageSize), wrapper);

        Map<Long, ChatSession> sessionMap = buildSessionMap(result.getRecords());
        Map<Long, KnowledgeBase> kbMap = buildKbMap(sessionMap.values());

        return result.getRecords().stream()
                .map(m -> {
                    ChatSession s = sessionMap.get(m.getSessionId());
                    KnowledgeBase kb = s != null ? kbMap.get(s.getKnowledgeBaseId()) : null;
                    return new LogMessageResponse(
                            m.getId(), m.getSessionId(),
                            s != null ? s.getKnowledgeBaseId() : null,
                            kb != null ? kb.getName() : null,
                            m.getRole(), m.getContent(), m.getCitationsJson(),
                            m.getLatencyMs(), m.getPromptTokens(), m.getCompletionTokens(),
                            m.getCreatedAt()
                    );
                })
                .toList();
    }

    public long countMessages(Long knowledgeBaseId, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (knowledgeBaseId != null) {
            List<Long> sessionIds = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                            .eq(ChatSession::getKnowledgeBaseId, knowledgeBaseId)
                            .select(ChatSession::getId))
                    .stream().map(ChatSession::getId).toList();
            if (sessionIds.isEmpty()) {
                return 0;
            }
            wrapper.in(Message::getSessionId, sessionIds);
        }
        if (startDate != null) {
            wrapper.ge(Message::getCreatedAt, startDate);
        }
        if (endDate != null) {
            wrapper.le(Message::getCreatedAt, endDate);
        }
        return messageMapper.selectCount(wrapper);
    }

    public List<LogRetrievalResponse> queryRetrievals(int page, int pageSize) {
        LambdaQueryWrapper<RetrievalLog> wrapper = new LambdaQueryWrapper<RetrievalLog>()
                .orderByDesc(RetrievalLog::getCreatedAt);
        Page<RetrievalLog> result = retrievalLogMapper.selectPage(Page.of(page, pageSize), wrapper);
        return result.getRecords().stream()
                .map(r -> new LogRetrievalResponse(
                        r.getId(), r.getMessageId(), r.getQueryText(),
                        r.getRetrievedChunksJson(), r.getTopK(), r.getLatencyMs(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    public long countRetrievals() {
        return retrievalLogMapper.selectCount(null);
    }

    private Map<Long, ChatSession> buildSessionMap(List<Message> messages) {
        List<Long> sessionIds = messages.stream().map(Message::getSessionId).distinct().toList();
        if (sessionIds.isEmpty()) return Map.of();
        return chatSessionMapper.selectBatchIds(sessionIds).stream()
                .collect(Collectors.toMap(ChatSession::getId, Function.identity()));
    }

    private Map<Long, KnowledgeBase> buildKbMap(java.util.Collection<ChatSession> sessions) {
        List<Long> kbIds = sessions.stream().map(ChatSession::getKnowledgeBaseId).distinct().toList();
        if (kbIds.isEmpty()) return Map.of();
        return knowledgeBaseMapper.selectBatchIds(kbIds).stream()
                .collect(Collectors.toMap(KnowledgeBase::getId, Function.identity()));
    }
}
