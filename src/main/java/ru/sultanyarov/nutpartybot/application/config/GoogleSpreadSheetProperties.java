package ru.sultanyarov.nutpartybot.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "bot.google")
public class GoogleSpreadSheetProperties {

    /**
     * id of spreadsheet
     */
    private String spreadsheetId;

    /**
     * names of tables for work with
     */
    private List<String> tables;
}
