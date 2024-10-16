package com.getname.controller;

import com.getname.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProcessor {

    private final UpdateService service;

    private WeatherBot weatherBot;

    public void registerBot(WeatherBot weatherBot) {
        this.weatherBot = weatherBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        if (!update.hasMessage()) {
            log.error("Message is null");
            return;
        }

        processMessage(update);
    }

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            var text = message.getText();

            var response = service.process(text);

            var responseMessage = SendMessage.builder()
                    .text(response)
                    .chatId(message.getChatId())
                    .parseMode(ParseMode.HTML)
                    .build();

            setView(responseMessage);
        } else {
            setUnsupportedMessageType(update);
        }
    }

    private void setUnsupportedMessageType(Update update) {
        var notSupportMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId()).text("Not support message type").build();
        setView(notSupportMessage);
    }

    private void setView(SendMessage sendMessage) {
        weatherBot.sendAnswerMessage(sendMessage);
    }
}
