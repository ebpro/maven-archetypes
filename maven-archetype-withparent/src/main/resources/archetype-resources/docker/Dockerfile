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
    mvn -s ci-settings.xml -B -Pshadedjar clean verify

########## SHADED JAR → DEBIAN ##########
FROM eclipse-temurin:25-jre-jammy AS finalShadedJarDebian
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=build /myproject/target/*-withdependencies.jar /${IMAGE_NAME}.jar
ENTRYPOINT ["java", "-jar", "/${IMAGE_NAME}.jar"]

########## SHADED JAR → ALPINE ##########
FROM eclipse-temurin:25-jre-alpine AS finalShadedJarAlpine
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=build /myproject/target/*-withdependencies.jar /${IMAGE_NAME}.jar
ENTRYPOINT ["java", "-jar", "/${IMAGE_NAME}.jar"]

########## JLINK → DEBIAN ##########
FROM build AS jlinkBuild
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Pjlink verify
FROM debian:bookworm-slim AS finalJLinkDebian
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=jlinkBuild /myproject/target/image /${IMAGE_NAME}
ENTRYPOINT ["/${IMAGE_NAME}/bin/myapp"]

########## JLINK → ALPINE ##########
FROM maven:3.9-eclipse-temurin-25-alpine AS jlinkAlpineBuild
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
    mvn -s ci-settings.xml -B -Pjlink clean verify
FROM alpine:3.20 AS finalJLinkAlpine
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=jlinkAlpineBuild /myproject/target/image /${IMAGE_NAME}
ENTRYPOINT ["/${IMAGE_NAME}/bin/myapp"]

########## GRAALVM NATIVE → DEBIAN ##########
FROM ghcr.io/graalvm/native-image:java25-ol9 AS graalvmBuild
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
    mvn -s ci-settings.xml -B -Pnative clean verify
FROM debian:bookworm-slim AS finalNativeDebian
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=graalvmBuild /myproject/target/${IMAGE_NAME} /${IMAGE_NAME}
ENTRYPOINT ["/${IMAGE_NAME}"]

########## GRAALVM NATIVE → ALPINE ##########
FROM alpine:3.20 AS finalNativeAlpine
ARG IMAGE_NAME
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=graalvmBuild /myproject/target/${IMAGE_NAME} /${IMAGE_NAME}
ENTRYPOINT ["/${IMAGE_NAME}"]
