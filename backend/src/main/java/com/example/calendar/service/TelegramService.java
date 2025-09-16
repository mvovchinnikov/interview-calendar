package com.example.calendar.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramService {
    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    private final RestTemplate restTemplate;
    private final String botToken;

    public TelegramService(RestTemplateBuilder builder, @Value("${telegram.bot-token:}") String botToken) {
        this.restTemplate = builder.build();
        this.botToken = botToken;
    }

    public void sendMessage(String chatId, String message) {
        if (!StringUtils.hasText(botToken) || !StringUtils.hasText(chatId)) {
            log.info("Skipping Telegram notification: bot token or chat id not configured");
            return;
        }
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, Object> payload = Map.of("chat_id", chatId, "text", message);
        try {
            restTemplate.postForObject(url, payload, Void.class);
        } catch (RestClientException ex) {
            log.warn("Failed to send Telegram notification: {}", ex.getMessage());
        }
    }
}
