package ru.sultanyarov.nutpartybot.service.service;


import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.BookingDocument;

/**
 * The service for work with booking
 */
public interface BookService {

    /**
     * Booking place
     *
     * @param userName user which booking place
     * @return result of booking
     */
    Mono<BookingDocument> bookPlace(String userName);

    /**
     * Unbooking place
     *
     * @param userName user which unbooked place
     */
    Mono<Void> unBookPlace(String userName);

    /**
     * Removing all bookings
     */
    void removeBookings();
}
