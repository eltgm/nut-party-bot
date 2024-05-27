FROM gradle:8.7.0-jdk21 AS build
RUN gradle test bootJar --no-daemon


FROM openjdk:21-jdk-slim
ARG APP_HOME=/app

EXPOSE 8080

COPY build/libs/*.jar $APP_HOME/nut-party-bot.jar
WORKDIR $APP_HOME

ENTRYPOINT java $JAVA_OPTS -jar nut-party-bot.jar $JAVA_ARGS