package ru.sultanyarov.nutpartybot.service.facade;

/**
 * The service for working with tg
 */
public interface TelegramFacade {
    /**
     * Sending start message
     *
     * @param chatId chat to sending keyboard
     */
    void sendStartMessage(Long chatId);

    void addFilm(String filmName);

    void getFilms(Long chatId);

    void bookPlace(Long chatId, String userName);

    void unBookPlace(Long chatId, String userName);

    void showMenu(Long chatId);

    void sendError(Long chatId);
}
