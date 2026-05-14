package com.example.copilot.service;

import com.example.copilot.config.AiProperties;
import com.example.copilot.dto.response.LlmAnswerResult;
import com.example.copilot.dto.response.RetrievedChunkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Service
public class LlmAnswerService {

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final AiProperties aiProperties;

    public LlmAnswerService(ObjectProvider<ChatModel> chatModelProvider, AiProperties aiProperties) {
        this.chatModelProvider = chatModelProvider;
        this.aiProperties = aiProperties;
    }

    public LlmAnswerResult generateAnswer(String userQuestion, List<RetrievedChunkResponse> chunks, String fallbackAnswer) {
        if (!aiProperties.isEnabled()) {
            return new LlmAnswerResult(prefixFallback(fallbackAnswer), 0, 0);
        }

        try {
            ChatModel chatModel = resolveChatModel();
            if (chatModel == null) {
                log.warn("No chat model bean is available, fallback answer will be used.");
                return new LlmAnswerResult(prefixFallback(fallbackAnswer), 0, 0);
            }

            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(aiProperties.getSystemPrompt()),
                            new UserMessage(buildUserPrompt(userQuestion, chunks))
                    )
            );
            ChatResponse chatResponse = chatModel.call(prompt);
            String content = chatResponse.getResult().getOutput().getText();
            if (content == null || content.isBlank()) {
                return new LlmAnswerResult(prefixFallback(fallbackAnswer), 0, 0);
            }

            int promptTokens = 0;
            int completionTokens = 0;
            if (chatResponse.getMetadata() != null) {
                var usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    promptTokens = (int) usage.getPromptTokens();
                    completionTokens = (int) usage.getCompletionTokens();
                }
            }

            return new LlmAnswerResult(content, promptTokens, completionTokens);
        } catch (Exception ex) {
            log.warn("LLM chat call failed, fallback answer will be used. message={}", ex.getMessage(), ex);
            return new LlmAnswerResult(prefixFallback(fallbackAnswer), 0, 0);
        }
    }

    private ChatModel resolveChatModel() {
        List<ChatModel> candidates = chatModelProvider.orderedStream().toList();
        if (candidates.isEmpty()) {
            return null;
        }

        for (ChatModel candidate : candidates) {
            String className = candidate.getClass().getName().toLowerCase();
            if (className.contains("deepseek")) {
                return candidate;
            }
        }

        return candidates.get(0);
    }

    private String buildUserPrompt(String userQuestion, List<RetrievedChunkResponse> chunks) {
        StringJoiner contextJoiner = new StringJoiner("\n\n");
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunkResponse chunk = chunks.get(i);
            contextJoiner.add("[引用片段 " + (i + 1) + "]\n"
                    + "chunkId=" + chunk.chunkId()
                    + ", documentId=" + chunk.documentId()
                    + ", chunkIndex=" + chunk.chunkIndex()
                    + "\n"
                    + chunk.snippet());
        }

        return """
                请基于以下知识库片段回答用户问题。

                %s

                用户问题：
                %s

                输出要求：
                1. 只能依据以上片段回答。
                2. 如果无法回答，明确说不知道。
                3. 尽量在答案中提及引用片段编号，例如“根据引用片段1”。
                """.formatted(contextJoiner, userQuestion);
    }

    private String prefixFallback(String fallbackAnswer) {
        return aiProperties.getFallbackMessage() + "\n\n" + fallbackAnswer;
    }
}
