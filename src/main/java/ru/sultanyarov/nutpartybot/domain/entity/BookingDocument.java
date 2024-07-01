package ru.sultanyarov.nutpartybot.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("booking")
public class BookingDocument {
    @Id
    @HashIndexed
    private String id;

    @Field("userName")
    private String userName;

    @Field("createdAt")
    private LocalDateTime createdAt;
}
