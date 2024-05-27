FROM openjdk:21-jdk-slim
ARG APP_HOME=/build/libs

WORKDIR $APP_HOME

EXPOSE 8080

ENTRYPOINT java $JAVA_OPTS -jar nut-party-bot.jar $JAVA_ARGS