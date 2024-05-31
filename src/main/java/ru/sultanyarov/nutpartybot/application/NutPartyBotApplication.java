package ru.sultanyarov.nutpartybot.application;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableMongock
@EnableReactiveMongoRepositories(basePackages = "ru.sultanyarov.nutpartybot.domain")
@SpringBootApplication
@ComponentScan(basePackages = "ru.sultanyarov.nutpartybot")
public class NutPartyBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutPartyBotApplication.class, args);
    }
}
