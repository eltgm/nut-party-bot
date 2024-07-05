package ru.sultanyarov.nutpartybot.application.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleApiConfiguration {
    @Value("${bot.google.service-secret}")
    private String serviceSecret;

    @Bean
    public Sheets getSheets() throws IOException {
        var transport = new NetHttpTransport.Builder().build();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new Sheets.Builder(transport, jsonFactory, new HttpCredentialsAdapter(getCredentials()))
                .setApplicationName("test")
                .build();
    }

    public Credentials getCredentials() throws IOException {
        return ServiceAccountCredentials
                .fromStream(IOUtils.toInputStream(serviceSecret, StandardCharsets.UTF_8))
                .createScoped(SheetsScopes.SPREADSHEETS);
    }
}
