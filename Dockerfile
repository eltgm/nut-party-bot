FROM openjdk:21-jdk-slim
ARG APP_HOME=/app

WORKDIR $APP_HOME

EXPOSE 8080 8081

COPY build/libs/*.jar $APP_HOME/nut-party-bot.jar

ENTRYPOINT java $JAVA_OPTS -jar nut-party-bot.jar $JAVA_ARGS