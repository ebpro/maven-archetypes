# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-25 AS build
ARG GITHUBACTOR
ARG GITHUBTOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
WORKDIR /myproject
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Dapp.image=libs dependency:go-offline
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Dapp.image=libs clean verify

FROM build AS jlinkbuild
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
    mvn -s ci-settings.xml -B -Dapp.image=jlink clean verify

FROM ghcr.io/graalvm/graalvm-community:java25-ol9 AS nativebuild
ARG GITHUBACTOR
ARG GITHUBTOKEN
ENV GITHUBACTOR="$GITHUBACTOR"
ENV GITHUBTOKEN="$GITHUBTOKEN"
WORKDIR /myproject
RUN dnf install -y maven && dnf clean all
COPY pom.xml .
COPY src src
COPY docker/ci-settings.xml .
RUN --mount=type=cache,id=mvncache-native,target=/root/.m2/repository \
    mvn -s ci-settings.xml -B -Dapp.image=native clean verify && mv target/$(ls target | grep -v '\.jar$\|\.pom$\|maven-archiver\|maven-status\|classes\|generated-sources\|generated-test-sources\|test-classes\|surefire-reports\|failsafe-reports' | head -1) target/app

FROM eclipse-temurin:25-jre-alpine AS finallibs
ARG APP_MAIN_CLASS
ENV APP_MAIN_CLASS=${APP_MAIN_CLASS}
WORKDIR /app
COPY --from=build /myproject/target/*.jar /app/app.jar
COPY --from=build /myproject/target/libs /app/libs
ENTRYPOINT ["sh", "-c", "java -cp /app/app.jar:/app/libs/* \"$APP_MAIN_CLASS\""]

FROM alpine AS finaljlink
COPY --from=jlinkbuild /myproject/target/image /app
ENTRYPOINT ["/app/bin/myapp"]

FROM oraclelinux:9 AS finalnative
COPY --from=nativebuild /myproject/target/app /app/app
RUN chmod +x /app/app
ENTRYPOINT ["/app/app"]
