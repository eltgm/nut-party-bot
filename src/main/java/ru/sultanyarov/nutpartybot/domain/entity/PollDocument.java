package ru.sultanyarov.nutpartybot.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import ru.sultanyarov.nutpartybot.domain.model.PollType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("poll")
public class PollDocument {
    @Id
    @Indexed(unique = true)
    private Long id;

    @Field("chatId")
    private Long chatId;

    @Field("messageId")
    private Integer messageId;

    @Field("type")
    private PollType type;

    @Field("answers")
    private List<String> answers;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("closedAt")
    private LocalDateTime closedAt;

    @Field("votes")
    private Map<String, Integer> votes;

    @Field("isAdmin")
    private Boolean isAdmin;
}
