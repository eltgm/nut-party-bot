package ru.sultanyarov.nutpartybot.application.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import ru.sultanyarov.nutpartybot.domain.model.PollType;
import ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType;
import ru.sultanyarov.nutpartybot.service.facade.TelegramFacade;

import static org.springframework.util.StringUtils.hasText;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.ACTIVITY_POLL_GROUP_RESULT;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.ADD_FILM;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.DATE_POLL_ADMIN_RESULT;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.DATE_POLL_GROUP_RESULT;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.UNDEFINED;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.ADD_FILM_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.FILM_LIST_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.MENU_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.REMOVE_BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.START_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.TABLETOP_LIST_COMMAND;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.TABLETOP_LIST_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class LongPollingBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${bot.tg.token}")
    private String token;

    private final TelegramFacade telegramFacade;

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
        Long chatId = null;
        String text = null;
        TelegramMessageType type = UNDEFINED;
        String userName = null;

        var pollAnswer = update.getPollAnswer();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            text = message.getText();
            userName = message.getFrom().getUserName();

            type = getTelegramMessageTypeFromMessage(text);
        } else if (update.hasPollAnswer()) {
            type = getTelegramMessageTypeFromPoll(pollAnswer);
        }

        launchProcessing(type, chatId, text, userName, pollAnswer);
    }

    private @NotNull TelegramMessageType getTelegramMessageTypeFromMessage(String text) {
        return switch (text) {
            case START_MESSAGE -> TelegramMessageType.START;
            case FILM_LIST_MESSAGE -> TelegramMessageType.FILM_LIST;
            case TABLETOP_LIST_MESSAGE, TABLETOP_LIST_COMMAND -> TelegramMessageType.TABLETOP_LIST;
            case BOOK_PLACE_MESSAGE -> TelegramMessageType.BOOK_PLACE;
            case REMOVE_BOOK_PLACE_MESSAGE -> TelegramMessageType.REMOVE_BOOK_PLACE;
            case MENU_MESSAGE -> TelegramMessageType.MENU;
            default -> text.toLowerCase().contains(ADD_FILM_MESSAGE.toLowerCase()) ? ADD_FILM : UNDEFINED;
        };
    }

    private TelegramMessageType getTelegramMessageTypeFromPoll(PollAnswer pollAnswer) {
        return telegramFacade.getPoll(Long.valueOf(pollAnswer.getPollId()))
                .map(pollDocument -> {
                    if (pollDocument.getType() == PollType.DATE_POLL_DOCUMENT_NAME) {
                        return pollDocument.getIsAdmin() ? DATE_POLL_ADMIN_RESULT : DATE_POLL_GROUP_RESULT;
                    }

                    return ACTIVITY_POLL_GROUP_RESULT;
                })
                .block();
    }

    private void launchProcessing(TelegramMessageType type, Long chatId, String text, String userName, PollAnswer pollAnswer) {
        log.debug("Received telegram message type '{}' for chat id '{}'", type, chatId);

        switch (type) {
            case START -> telegramFacade.sendStartMessage(chatId);
            case ADD_FILM -> {
                String filmName = text.toLowerCase().replace(ADD_FILM_MESSAGE.toLowerCase(), "").strip();

                if (hasText(filmName)) {
                    telegramFacade.addFilm(filmName, chatId);
                }
            }
            case FILM_LIST -> telegramFacade.getFilms(chatId);
            case BOOK_PLACE -> telegramFacade.bookPlace(chatId, userName);
            case REMOVE_BOOK_PLACE -> telegramFacade.unBookPlace(chatId, userName);
            case MENU -> telegramFacade.showMenu(chatId);
            case DATE_POLL_ADMIN_RESULT -> telegramFacade.processDatePollResults(pollAnswer);
            case DATE_POLL_GROUP_RESULT -> telegramFacade.updateDatePollResults(pollAnswer);
            case ACTIVITY_POLL_GROUP_RESULT -> telegramFacade.updateActivityPollResults(pollAnswer);
            case TABLETOP_LIST -> telegramFacade.getTabletops(chatId);
            case UNDEFINED -> {
                //log.error("Unknown command - {}", update);
            }
        }
    }
}
