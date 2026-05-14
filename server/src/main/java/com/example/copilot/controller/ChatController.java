package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.request.CreateChatSessionRequest;
import com.example.copilot.dto.request.CreateMessageRequest;
import com.example.copilot.dto.response.ChatReplyResponse;
import com.example.copilot.dto.response.ChatSessionResponse;
import com.example.copilot.dto.response.MessageResponse;
import com.example.copilot.security.UserPrincipal;
import com.example.copilot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/sessions")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ApiResponse<ChatSessionResponse> createSession(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody CreateChatSessionRequest request) {
        return ApiResponse.success(chatService.createSession(principal.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<ChatSessionResponse>> listSessions(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(chatService.listSessions(principal.getId()));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<ChatReplyResponse> createMessage(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody CreateMessageRequest request) {
        return ApiResponse.success(chatService.createUserMessage(principal.getId(), id, request));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<List<MessageResponse>> listMessages(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable Long id) {
        return ApiResponse.success(chatService.listMessages(principal.getId(), id));
    }
}
