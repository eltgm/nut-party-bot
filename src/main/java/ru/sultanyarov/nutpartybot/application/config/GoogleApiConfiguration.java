package ru.sultanyarov.nutpartybot.application.config;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiConfiguration {
    @Value("${bot.google.token}")
    private String token;

    @Bean
    public Sheets getSheets() {
        var transport = new NetHttpTransport.Builder().build();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpRequestInitializer httpRequestInitializer = request
                -> request.setInterceptor(intercepted -> intercepted.getUrl().set("key", token));

        return new Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
                .setApplicationName("test")
                .build();
    }
}
