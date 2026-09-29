# syntax=docker/dockerfile:1

########## BUILD ##########
FROM maven:3.9.11-eclipse-temurin-25 AS build
ARG GITHUBACTOR
ARG GITHUBTOKEN
ARG SONAR_URL
ARG SONAR_TOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
ENV SONAR_URL="$SONAR_URL"
ENV SONAR_TOKEN="$SONAR_TOKEN"
WORKDIR /myproject
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Pshadedjar clean verify

########## SHADED JAR → DEBIAN ##########
FROM eclipse-temurin:25-jre-jammy AS finalShadedJarDebian
COPY --from=build /myproject/target/*-withdependencies.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

########## SHADED JAR → ALPINE ##########
FROM eclipse-temurin:25-jre-alpine AS finalShadedJarAlpine
COPY --from=build /myproject/target/*-withdependencies.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

########## JLINK → DEBIAN ##########
FROM build AS jlinkBuild
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Pjlink verify
FROM debian:bookworm-slim AS finalJLinkDebian
COPY --from=jlinkBuild /myproject/target/image /app
ENTRYPOINT ["/app/bin/myapp"]

########## JLINK → ALPINE ##########
FROM maven:3.9.11-eclipse-temurin-25-alpine AS jlinkAlpineBuild
ARG GITHUBACTOR
ARG GITHUBTOKEN
ARG SONAR_URL
ARG SONAR_TOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
ENV SONAR_URL="$SONAR_URL"
ENV SONAR_TOKEN="$SONAR_TOKEN"
WORKDIR /myproject
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Pjlink clean verify
FROM alpine:3.20 AS finalJLinkAlpine
COPY --from=jlinkAlpineBuild /myproject/target/image /app
ENTRYPOINT ["/app/bin/myapp"]

########## GRAALVM NATIVE → DEBIAN ##########
FROM ghcr.io/graalvm/native-image:java25-ol9 AS graalvmBuild
ARG GITHUBACTOR
ARG GITHUBTOKEN
ARG SONAR_URL
ARG SONAR_TOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
ENV SONAR_URL="$SONAR_URL"
ENV SONAR_TOKEN="$SONAR_TOKEN"
WORKDIR /myproject
RUN dnf install -y maven && dnf clean all
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Pnative clean verify && mv target/$(ls target | grep -v '\.jar$\|\.pom$\|maven-archiver\|maven-status\|classes\|generated-sources\|generated-test-sources\|test-classes\|surefire-reports\|failsafe-reports' | head -1) target/app
FROM debian:bookworm-slim AS finalNativeDebian
COPY --from=graalvmBuild /myproject/target/app /app/app
RUN chmod +x /app/app
ENTRYPOINT ["/app/app"]

########## GRAALVM NATIVE → ALPINE (glibc compat) ##########
FROM oraclelinux:9 AS finalNativeAlpine
COPY --from=graalvmBuild /myproject/target/app /app/app
RUN chmod +x /app/app
ENTRYPOINT ["/app/app"]
