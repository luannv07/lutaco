package vn.id.luannv.lutaco.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final ChatClient chatClient;

    public GeminiService(ChatClient.Builder builder) {
        // Khởi tạo ChatClient từ Builder
        this.chatClient = builder.build();
    }

    public String askGemini(String message) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}