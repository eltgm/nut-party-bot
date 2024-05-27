package ru.sultanyarov.nutpartybot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Configuration
public class TelegramApi {
    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient("7047035187:AAE43JFdA9ihZ2-dH2xdhl-W00nROtRzbWo");
    }

    public void test(TelegramClient telegramClient) throws TelegramApiException {

        telegramClient.execute(
                org.telegram.telegrambots.meta.api.methods.polls.SendPoll.builder()
                        .allowMultipleAnswers(true)
                        .question("Когда собираемся?")
                        .options(List.of("ПТ", "СБ", "ВС"))
                        .build()
        );
    }
}
