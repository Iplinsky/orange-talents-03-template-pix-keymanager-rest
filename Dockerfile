FROM openjdk:11
LABEL MAINTAINER="Thiago"
ARG JAR_FILE=build/libs/*-all.jar
COPY $JAR_FILE app.jar
ENV APP_NAME keymanager-rest
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]