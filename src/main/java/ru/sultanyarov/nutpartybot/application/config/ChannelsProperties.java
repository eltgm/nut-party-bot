package ru.sultanyarov.nutpartybot.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "channels")
public class ChannelsProperties {
    /**
     * Tg admin bot id
     */
    private Long admin;

    /**
     * Group ids to work with
     */
    private List<Long> groups;
}
