package ru.sultanyarov.nutpartybot.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LongPollingBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${bot.token}")
    private String token;

    private final TelegramClient telegramClient;

    @Override
    public String getBotToken() {
        return "7047035187:AAE43JFdA9ihZ2-dH2xdhl-W00nROtRzbWo";
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            System.out.println(chat_id);
            telegramClient.execute(
                    SendPoll.builder()
                            .chatId(chat_id)
                            .allowMultipleAnswers(true)
                            .question("Когда собираемся?")
                            .options(List.of("ПТ", "СБ", "ВС"))
                            .isAnonymous(false)
                            .build()
            );
        }
    }
}
