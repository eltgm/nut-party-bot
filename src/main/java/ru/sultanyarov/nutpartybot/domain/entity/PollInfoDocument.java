package ru.sultanyarov.nutpartybot.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("poll_info")
public class PollInfoDocument {
    @Id
    @Indexed(unique = true)
    private String name;

    @Field("question")
    private String question;

    @Field("answers")
    private List<String> answers;
}
