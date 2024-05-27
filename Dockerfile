FROM gradle:8.7.0-jdk21 AS build
RUN gradle test bootJar --no-daemon


FROM openjdk:21-jdk-slim
ARG APP_HOME=/app
WORKDIR $APP_HOME

EXPOSE 8080

COPY --from=build build/libs/*.jar $APP_HOME/nut-party-bot.jar

ENTRYPOINT java $JAVA_OPTS -jar nut-party-bot.jar $JAVA_ARGS