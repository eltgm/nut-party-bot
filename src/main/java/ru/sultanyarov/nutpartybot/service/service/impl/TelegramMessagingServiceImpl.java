package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;
import ru.sultanyarov.nutpartybot.domain.exception.ServiceException;
import ru.sultanyarov.nutpartybot.service.service.TelegramMessagingService;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessagingServiceImpl implements TelegramMessagingService {
    private final TelegramClient telegramClient;

    @Override
    public void createAndSendMessage(Long chatId, String text) {
        log.info("Create tg message and send to chat - {}", chatId);
        var message = new SendMessage(chatId.toString(), hasText(text) ? text : "Пусто");
        sendMessage(message);
    }

    @Override
    public void createAndSendMessageWithReply(Long chatId, String text, Integer replyMessageId) {
        log.info("Create tg message with reply to {} and send to chat - {}", replyMessageId, chatId);
        var message = new SendMessage(chatId.toString(), hasText(text) ? text : "Пусто");
        message.setReplyToMessageId(replyMessageId);
        sendMessage(message);
    }

    @Override
    public void sendMessage(SendMessage sendMessage) {
        log.info("Send tg message to chat - {}", sendMessage.getChatId());
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Telegram API exception while send message to chat {}", sendMessage.getChatId(), e);
            throw new ServiceException("Error sending message", e);
        }
    }

    @Override
    public Message sendPollMessage(Long chatId, PollInfoDocument pollInfoDocument, List<String> answers) {
        var pollType = pollInfoDocument.getName();
        log.info("Send tg poll with type {} to chat - {}", pollType, chatId);
        Message message;
        try {
            message = telegramClient.execute(
                    SendPoll.builder()
                            .chatId(chatId)
                            .allowMultipleAnswers(true)
                            .question(pollInfoDocument.getQuestion())
                            .options(answers)
                            .isAnonymous(false)
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Error creating poll with type {} for chatId {}", pollType, chatId, e);
            throw new ServiceException("Error creating poll with type " + pollType, e);
        }

        return message;
    }

    @Override
    public void stopPoll(PollDocument pollDocument, Long pollId) {
        log.info("Stop tg poll with id - {}", pollId);
        try {
            telegramClient.execute(
                    StopPoll.builder()
                            .chatId(pollDocument.getChatId())
                            .messageId(pollDocument.getMessageId())
                            .build()
            );
        } catch (TelegramApiException e) {
            log.error("Error closing poll with id {}", pollId, e);
        }
    }
}
