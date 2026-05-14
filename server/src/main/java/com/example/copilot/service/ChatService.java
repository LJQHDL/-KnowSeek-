package com.example.copilot.service;

import com.example.copilot.dto.request.CreateChatSessionRequest;
import com.example.copilot.dto.request.CreateMessageRequest;
import com.example.copilot.dto.response.ChatReplyResponse;
import com.example.copilot.dto.response.ChatSessionResponse;
import com.example.copilot.dto.response.MessageResponse;

import java.util.List;

public interface ChatService {

    ChatSessionResponse createSession(Long userId, CreateChatSessionRequest request);

    List<ChatSessionResponse> listSessions(Long userId);

    ChatReplyResponse createUserMessage(Long userId, Long sessionId, CreateMessageRequest request);

    List<MessageResponse> listMessages(Long userId, Long sessionId);
}
