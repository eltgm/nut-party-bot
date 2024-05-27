package ru.sultanyarov.nutpartybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class NutPartyBotApplication {

    public static void main(String[] args) throws TelegramApiException, InterruptedException {
        ConfigurableApplicationContext run = SpringApplication.run(NutPartyBotApplication.class, args);
        TelegramClient telegramClient = run.getBean(TelegramClient.class);

       /* Message execute = telegramClient.execute(
                SendPoll.builder()
                        .chatId(207083819L)
                        .allowMultipleAnswers(true)
                        .question("Когда собираемся?")
                        .options(List.of("ПТ", "СБ", "ВС"))
                        .isAnonymous(false)
                        .build()
        );
        System.out.println(execute);
        Thread.sleep(5000);

        telegramClient.execute(StopPoll.builder()
                .chatId(207083819L)
                .messageId(execute.getMessageId())
                .build());*/
    }
}
