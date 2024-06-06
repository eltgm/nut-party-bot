package ru.sultanyarov.nutpartybot.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.sultanyarov.nutpartybot.domain.entity.BookingDocument;
import ru.sultanyarov.nutpartybot.domain.exception.NotFoundException;
import ru.sultanyarov.nutpartybot.domain.exception.TooManyBooksException;
import ru.sultanyarov.nutpartybot.domain.repository.BookingCollectionRepository;
import ru.sultanyarov.nutpartybot.service.service.BookService;

import java.time.LocalDateTime;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    @Value("${book.places-count}")
    private int bookPlacesCount;

    private final BookingCollectionRepository bookingCollectionRepository;

    @Override
    public Mono<BookingDocument> bookPlace(String userName) {
        return bookingCollectionRepository.count()
                .flatMap(actualBooking -> {
                    if (actualBooking >= bookPlacesCount) {
                        return Mono.error(() -> new TooManyBooksException("Not found free booking for user {}", userName));
                    }

                    return Mono.just(actualBooking);
                })
                .flatMap((Function<Long, Mono<BookingDocument>>) o -> {
                    log.info("Book place for user {}", userName);
                    var bookingDocument = new BookingDocument();
                    bookingDocument.setUserName(userName);
                    bookingDocument.setCreatedAt(LocalDateTime.now());
                    return bookingCollectionRepository.save(bookingDocument);
                });
    }

    @Override
    public Mono<Void> unBookPlace(String userName) {
        log.info("Un book place for user {}", userName);
        return bookingCollectionRepository.findTopByUserName(userName)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Not found booking for user {}", userName)))
                .flatMap(bookingCollectionRepository::delete);
    }

    @Override
    public void removeBookings() {
        log.info("Remove bookings");
        bookingCollectionRepository.deleteAll()
                .subscribe();
    }
}
