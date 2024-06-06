package ru.sultanyarov.nutpartybot.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.sultanyarov.nutpartybot.domain.exception.NotFoundException;
import ru.sultanyarov.nutpartybot.domain.exception.TooManyBooksException;
import ru.sultanyarov.nutpartybot.service.facade.TelegramFacade;
import ru.sultanyarov.nutpartybot.service.service.BookService;
import ru.sultanyarov.nutpartybot.service.service.FilmService;

import java.util.ArrayList;
import java.util.List;

import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.BOOK_PLACE_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.FILM_LIST_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.MENU_MESSAGE;
import static ru.sultanyarov.nutpartybot.service.utils.TelegramMessageConstants.REMOVE_BOOK_PLACE_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFacadeImpl implements TelegramFacade {
    private final TelegramClient telegramClient;
    private final FilmService filmService;
    private final BookService bookService;

    @Override
    public void sendStartMessage(Long chatId) {
        log.info("Sending start message to chat {}", chatId);
        var sendMessage = new SendMessage(chatId.toString(), "Выбирай");
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        var replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(
                        List.of(
                                new KeyboardRow(
                                        KeyboardButton.builder()
                                                .text(FILM_LIST_MESSAGE)
                                                .build(),
                                        KeyboardButton.builder()
                                                .text(BOOK_PLACE_MESSAGE)
                                                .build(),
                                        KeyboardButton.builder()
                                                .text(REMOVE_BOOK_PLACE_MESSAGE)
                                                .build()
                                ),
                                new KeyboardRow(
                                        KeyboardButton.builder()
                                                .text(MENU_MESSAGE)
                                                .build()
                                )
                        )
                )
                .selective(true)
                .build();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Telegram API exception while send keyboard to chat {}", chatId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFilm(String filmName) {
        filmService.addFilmWithNotification(filmName);
    }

    @Override
    public void getFilms(Long chatId) {
        filmService.getActualFilms()
                .collectList()
                .subscribe(strings -> {
                    List<String> formattedFilmsList = new ArrayList<>(strings.size());
                    for (int i = 1; i <= strings.size(); i++) {
                        formattedFilmsList.add(String.format("%d. %s", i, strings.get(i - 1)));
                    }
                    var message = String.join("\n", formattedFilmsList);
                    sendMessage(chatId, message);
                });
    }

    @Override
    public void bookPlace(Long chatId, String userName) {
        bookService.bookPlace(userName)
                .doOnError(throwable -> {
                    if (throwable instanceof TooManyBooksException) {
                        sendMessage(chatId, "Мест нет!");
                    } else {
                        sendMessage(chatId, "Непредвиденная ошибка. Пиши @eltgm");
                    }
                })
                .subscribe(bookingDocument -> sendMessage(chatId, "Место успешно забронировано"));
    }

    @Override
    public void unBookPlace(Long chatId, String userName) {
        bookService.unBookPlace(userName)
                .doOnError(throwable -> {
                    if (throwable instanceof NotFoundException) {
                        sendMessage(chatId, "У тебя нет броней, не парься");
                    } else {
                        sendMessage(chatId, "Непредвиденная ошибка. Пиши @eltgm");
                    }
                })
                .doOnSuccess(bookingDocument -> sendMessage(chatId, "Бронь снята!"))
                .subscribe();
    }

    @Override
    public void showMenu(Long chatId) {
        sendMessage(chatId, """
                /start - запуск бота
                /addFilm название_фильма - добавить фильм в список
                """);
    }

    @Override
    public void sendError(Long chatId) {
        sendMessage(chatId, "Неизвестная команда!");
    }

    private void sendMessage(Long chatId, String text) {
        var sendMessage = new SendMessage(chatId.toString(),
                text);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Telegram API exception while send message to chat {}", chatId, e);
            throw new RuntimeException(e);
        }
    }
}
