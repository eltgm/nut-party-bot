package ru.sultanyarov.nutpartybot.application.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import ru.sultanyarov.nutpartybot.service.service.PollService;

@Component
@RequiredArgsConstructor
public class LongPollingBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${bot.tg.token}")
    private String token;

    private final PollService pollService;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasPollAnswer()) {
            PollAnswer pollAnswer = update.getPollAnswer();
            pollService.updatePollResults(Long.valueOf(pollAnswer.getPollId()), pollAnswer.getOptionIds());
        }
    }
}
