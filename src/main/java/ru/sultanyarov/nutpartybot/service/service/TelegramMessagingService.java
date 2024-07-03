package ru.sultanyarov.nutpartybot.service.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.sultanyarov.nutpartybot.domain.entity.PollDocument;
import ru.sultanyarov.nutpartybot.domain.entity.PollInfoDocument;

import java.util.List;

/**
 * Service for working with telegram client
 */
public interface TelegramMessagingService {

    /**
     * Create telegram message object and send it
     *
     * @param chatId chat to sending message
     * @param text   message text
     */
    void createAndSendMessage(Long chatId, String text);

    /**
     * Create telegram message object and send it with reply
     *
     * @param chatId         chat to sending message
     * @param text           message text
     * @param replyMessageId message to reply
     */
    void createAndSendMessageWithReply(Long chatId, String text, Integer replyMessageId);

    /**
     * Send telegram message
     *
     * @param sendMessage telegram message
     */
    void sendMessage(SendMessage sendMessage);

    /**
     * Create telegram poll message and send it
     *
     * @param chatId           chat to sending poll
     * @param pollInfoDocument poll info
     * @param answers          answers
     * @return sent message
     */
    Message sendPollMessage(Long chatId, PollInfoDocument pollInfoDocument, List<String> answers);

    /**
     * Stop poll
     *
     * @param pollDocument poll to stop
     * @param pollId       id of poll
     */
    void stopPoll(PollDocument pollDocument, Long pollId);
}
