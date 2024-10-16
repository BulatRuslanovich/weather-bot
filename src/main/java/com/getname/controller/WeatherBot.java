package com.getname.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class WeatherBot extends TelegramLongPollingBot {

    private final UpdateProcessor updateProcessor;

    @Value("${bot.username}")
    private String botUsername;

    public WeatherBot(UpdateProcessor updateProcessor,  @Value("${bot.token}") String botToken) {
        super(botToken);
        this.updateProcessor = updateProcessor;
    }

    @PostConstruct
    void init() {
        updateProcessor.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateProcessor.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }
}
