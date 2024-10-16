package com.getname.controller;

import com.getname.config.TelegramProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class WeatherBot extends TelegramWebhookBot {

    private final UpdateDispatcher updateDispatcher;

    private final TelegramProperties telegramProperties;

    @PostConstruct
    private void init() {
        updateDispatcher.initBot(this);

        try {
            var webhook = SetWebhook.builder()
                    .url(telegramProperties.getUri())
                    .build();
            this.setWebhook(webhook);
        } catch (TelegramApiException e) {
            log.error("Error with webhook", e);
        }
    }

    public WeatherBot(UpdateDispatcher updateDispatcher, TelegramProperties telegramProperties) {
        super(telegramProperties.getToken());
        this.updateDispatcher = updateDispatcher;
        this.telegramProperties = telegramProperties;
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getUsername();
    }

    @Override
    public String getBotPath() {
        return "/update";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }
}
