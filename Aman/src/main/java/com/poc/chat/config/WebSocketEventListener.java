package com.poc.chat.config;

import com.poc.chat.model.ChatMessage;
import com.poc.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor
                .getSessionAttributes()
                .get("username");

        if (username != null) {
            ChatMessage chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .content(username + " left the chat")
                    .timestamp(LocalDateTime.now().format(FORMATTER))
                    .build();

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}