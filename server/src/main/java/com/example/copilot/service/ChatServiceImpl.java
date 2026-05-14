package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.common.MessageRoleEnum;
import com.example.copilot.config.AiProperties;
import com.example.copilot.dto.request.CreateChatSessionRequest;
import com.example.copilot.dto.request.CreateMessageRequest;
import com.example.copilot.dto.response.ChatReplyResponse;
import com.example.copilot.dto.response.ChatSessionResponse;
import com.example.copilot.dto.response.LlmAnswerResult;
import com.example.copilot.dto.response.MessageResponse;
import com.example.copilot.dto.response.RetrievalResult;
import com.example.copilot.dto.response.RetrievedChunkResponse;
import com.example.copilot.entity.ChatSession;
import com.example.copilot.entity.KnowledgeBase;
import com.example.copilot.entity.Message;
import com.example.copilot.entity.RetrievalLog;
import com.example.copilot.exception.ForbiddenException;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.ChatSessionMapper;
import com.example.copilot.mapper.KnowledgeBaseMapper;
import com.example.copilot.mapper.MessageMapper;
import com.example.copilot.mapper.RetrievalLogMapper;
import com.example.copilot.rag.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final MessageMapper messageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final RetrievalLogMapper retrievalLogMapper;
    private final RetrievalService retrievalService;
    private final LlmAnswerService llmAnswerService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(ChatSessionMapper chatSessionMapper,
                           MessageMapper messageMapper,
                           KnowledgeBaseMapper knowledgeBaseMapper,
                           RetrievalLogMapper retrievalLogMapper,
                           RetrievalService retrievalService,
                           LlmAnswerService llmAnswerService,
                           AiProperties aiProperties,
                           ObjectMapper objectMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.messageMapper = messageMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.retrievalLogMapper = retrievalLogMapper;
        this.retrievalService = retrievalService;
        this.llmAnswerService = llmAnswerService;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ChatSessionResponse createSession(Long userId, CreateChatSessionRequest request) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(request.knowledgeBaseId());
        if (kb == null) {
            throw new NotFoundException("知识库不存在");
        }
        if (!kb.getOwnerId().equals(userId)) {
            throw new ForbiddenException("无权在该知识库下创建会话");
        }

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setKnowledgeBaseId(request.knowledgeBaseId());
        session.setTitle(request.title());
        session.setCreatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return toResponse(session);
    }

    @Override
    public List<ChatSessionResponse> listSessions(Long userId) {
        return chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ChatReplyResponse createUserMessage(Long userId, Long sessionId, CreateMessageRequest request) {
        ChatSession session = requireOwnedSession(userId, sessionId);
        Message userMessage = new Message();
        userMessage.setSessionId(session.getId());
        userMessage.setRole(MessageRoleEnum.USER.toDbValue());
        userMessage.setContent(request.content());
        userMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMessage);

        RetrievalResult retrievalResult = retrievalService.retrieveTopChunks(sessionId, request.content(), aiProperties.getRetrievalTopK());
        Message assistantMessage = new Message();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole(MessageRoleEnum.ASSISTANT.toDbValue());
        String fallbackAnswer = generateAssistantReply(retrievalResult.chunks());
        LlmAnswerResult llmResult = llmAnswerService.generateAnswer(request.content(), retrievalResult.chunks(), fallbackAnswer);
        assistantMessage.setContent(llmResult.content());
        assistantMessage.setCitationsJson(buildCitationsJson(retrievalResult.chunks()));
        assistantMessage.setLatencyMs(retrievalResult.latencyMs());
        assistantMessage.setPromptTokens(llmResult.promptTokens());
        assistantMessage.setCompletionTokens(llmResult.completionTokens());
        assistantMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(assistantMessage);
        saveRetrievalLog(assistantMessage.getId(), request.content(), retrievalResult);

        return new ChatReplyResponse(
                toResponse(userMessage),
                toResponse(assistantMessage),
                retrievalResult.chunks()
        );
    }

    @Override
    public List<MessageResponse> listMessages(Long userId, Long sessionId) {
        requireOwnedSession(userId, sessionId);
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                        .eq(Message::getSessionId, sessionId)
                        .orderByAsc(Message::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ChatSession requireOwnedSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new NotFoundException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException("无权访问该会话");
        }
        return session;
    }

    private ChatSessionResponse toResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getKnowledgeBaseId(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole(),
                message.getContent(),
                message.getCitationsJson(),
                message.getLatencyMs(),
                message.getPromptTokens(),
                message.getCompletionTokens(),
                message.getCreatedAt()
        );
    }

    private String generateAssistantReply(List<RetrievedChunkResponse> retrievedChunks) {
        if (retrievedChunks.isEmpty()) {
            return "当前知识库中没有检索到相关内容，暂时无法基于文档回答该问题。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("这是一个基于当前知识库片段生成的临时回答：\n\n");
        for (int i = 0; i < retrievedChunks.size(); i++) {
            sb.append("参考片段 ").append(i + 1).append("：\n").append(retrievedChunks.get(i).snippet()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildCitationsJson(List<RetrievedChunkResponse> retrievedChunks) {
        try {
            return objectMapper.writeValueAsString(retrievedChunks);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void saveRetrievalLog(Long messageId, String query, RetrievalResult result) {
        RetrievalLog log = new RetrievalLog();
        log.setMessageId(messageId);
        log.setQueryText(query);
        log.setRetrievedChunksJson(buildRetrievedChunksJson(result.chunks()));
        log.setTopK(result.chunks().size());
        log.setLatencyMs(result.latencyMs());
        log.setCreatedAt(LocalDateTime.now());
        retrievalLogMapper.insert(log);
    }

    private String buildRetrievedChunksJson(List<RetrievedChunkResponse> chunks) {
        try {
            return objectMapper.writeValueAsString(chunks);
        } catch (Exception e) {
            return "[]";
        }
    }
}
