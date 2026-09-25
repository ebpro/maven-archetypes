# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS libsBuild
ARG GITHUBACTOR
ARG GITHUBTOKEN
ARG DOCKER_USERNAME
ARG DOCKER_PASSWORD
ARG SONAR_URL
ARG SONAR_TOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
ENV DOCKER_USERNAME="$DOCKER_USERNAME"
ENV DOCKER_PASSWORD="$DOCKER_PASSWORD"
ENV SONAR_URL="$SONAR_URL"
ENV SONAR_TOKEN="$SONAR_TOKEN"
WORKDIR /myproject
COPY pom.xml .
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -s ci-settings.xml -B -Dapp.image=libs dependency:go-offline
COPY src src
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -s ci-settings.xml -B -Dapp.image=libs clean verify

FROM eclipse-temurin:21-jre AS finalLibs
ARG IMAGE_NAME=myapp
ARG APP_MAIN_CLASS
ENV IMAGE_NAME=${IMAGE_NAME}
ENV APP_MAIN_CLASS=${APP_MAIN_CLASS}
WORKDIR /app
COPY --from=libsBuild /myproject/target/*.jar /app/app.jar
COPY --from=libsBuild /myproject/target/libs /app/libs
ENTRYPOINT ["sh", "-c", "java -cp /app/app.jar:/app/libs/* \"$APP_MAIN_CLASS\""]
