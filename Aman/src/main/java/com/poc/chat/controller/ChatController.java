package com.poc.chat.controller;

import com.poc.chat.model.ChatMessage;
import com.poc.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // Handles: client sends to /app/chat.sendMessage
    // Broadcasts to: /topic/public
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return chatMessage;
    }

    // Handles: client sends to /app/chat.addUser
    // Broadcasts join notification to: /topic/public
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {

        // Store username in WebSocket session
        headerAccessor.getSessionAttributes()
                .put("username", chatMessage.getSender());

        chatMessage.setType(MessageType.JOIN);
        chatMessage.setContent(chatMessage.getSender() + " joined the chat");
        chatMessage.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return chatMessage;
    }
}