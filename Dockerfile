FROM gradle:8.7.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle test bootJar --no-daemon


FROM openjdk:21-jdk-slim
ARG APP_HOME=/app
WORKDIR $APP_HOME

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar $APP_HOME/nut-party-bot.jar

ENTRYPOINT java $JAVA_OPTS -jar nut-party-bot.jar $JAVA_ARGS