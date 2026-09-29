# syntax=docker/dockerfile:1

########## BUILD ##########
FROM maven:3.9-eclipse-temurin-25 AS build
ARG GITHUBLOGIN
ARG GITHUBTOKEN
ARG SONAR_URL
ARG SONAR_TOKEN
ARG IMAGE_NAME
ENV GITHUBLOGIN="$GITHUBLOGIN"
ENV GITHUBTOKEN="$GITHUBTOKEN"
ENV SONAR_URL="$SONAR_URL"
ENV SONAR_TOKEN="$SONAR_TOKEN"
ENV IMAGE_NAME="$IMAGE_NAME"
WORKDIR /myproject
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B clean verify

########## JAR → DEBIAN ##########
FROM eclipse-temurin:25-jre-jammy AS finalJarDebian
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=build /myproject/target/*.jar /${IMAGE_NAME}.jar
ENTRYPOINT ["java", "-jar", "/${IMAGE_NAME}.jar"]

########## JAR → ALPINE ##########
FROM eclipse-temurin:25-jre-alpine AS finalJarAlpine
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=build /myproject/target/*.jar /${IMAGE_NAME}.jar
ENTRYPOINT ["java", "-jar", "/${IMAGE_NAME}.jar"]
