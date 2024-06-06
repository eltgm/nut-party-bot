package ru.sultanyarov.nutpartybot.application.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType;
import ru.sultanyarov.nutpartybot.service.facade.TelegramFacade;

import static org.springframework.util.StringUtils.hasText;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.ADD_FILM;
import static ru.sultanyarov.nutpartybot.domain.model.TelegramMessageType.UNDEFINED;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.ADD_FILM_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.FILM_LIST_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.MENU_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.REMOVE_BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.START_MESSAGE;

@Component
@RequiredArgsConstructor
public class LongPollingBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${bot.tg.token}")
    private String token;

    private final TelegramFacade telegramFacade;
    private final TelegramClient telegramClient;

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
        TelegramMessageType type = null;
        String userName = null;

        if (update.hasMessage()) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            text = message.getText();
            userName = message.getFrom().getUserName();

            type = switch (text) {
                case START_MESSAGE -> TelegramMessageType.START;
                case FILM_LIST_MESSAGE -> TelegramMessageType.FILM_LIST;
                case BOOK_PLACE_MESSAGE -> TelegramMessageType.BOOK_PLACE;
                case REMOVE_BOOK_PLACE_MESSAGE -> TelegramMessageType.REMOVE_BOOK_PLACE;
                case MENU_MESSAGE -> TelegramMessageType.MENU;
                default -> text.toLowerCase().contains(ADD_FILM_MESSAGE.toLowerCase()) ? ADD_FILM : UNDEFINED;
            };
        }

        switch (type) {
            case START -> telegramFacade.sendStartMessage(chatId);
            case ADD_FILM -> {
                String filmName = text.toLowerCase().replace(ADD_FILM_MESSAGE.toLowerCase(), "").strip();

                if (hasText(filmName)) {
                    telegramFacade.addFilm(filmName);
                }
            }
            case FILM_LIST -> telegramFacade.getFilms(chatId);
            case BOOK_PLACE -> telegramFacade.bookPlace(chatId, userName);
            case REMOVE_BOOK_PLACE -> telegramFacade.unBookPlace(chatId, userName);
            case MENU -> telegramFacade.showMenu(chatId);
            case UNDEFINED -> telegramFacade.sendError(chatId);
        }
    }
}
