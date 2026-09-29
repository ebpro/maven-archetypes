# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-25-alpine AS libsbuild
WORKDIR /myproject
COPY pom.xml .
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=libs dependency:go-offline
COPY src src
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=libs clean verify

FROM libsbuild AS jlinkbuild
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=jlink clean verify

FROM ghcr.io/graalvm/graalvm-community:java25-ol9 AS nativebuild
WORKDIR /myproject
RUN dnf install -y maven && dnf clean all
COPY pom.xml .
COPY src src
RUN --mount=type=cache,id=mvn-cache-native,target=/root/.m2/repository \
  mvn -B -Dapp.image=native clean verify && mv target/$(ls target | grep -v '\.jar$\|\.pom$\|maven-archiver\|maven-status' | head -1) target/app

FROM eclipse-temurin:25-jre-alpine AS finallibs
ARG APP_MAIN_CLASS
ENV APP_MAIN_CLASS=${APP_MAIN_CLASS}
WORKDIR /app
COPY --from=libsbuild /myproject/target/*.jar /app/app.jar
COPY --from=libsbuild /myproject/target/libs /app/libs
ENTRYPOINT ["sh", "-c", "java -cp /app/app.jar:/app/libs/* \"$APP_MAIN_CLASS\""]

FROM alpine AS finaljlink
COPY --from=jlinkbuild /myproject/target/image /app
ENTRYPOINT ["/app/bin/myapp"]

FROM oraclelinux:9 AS finalnative
COPY --from=nativebuild /myproject/target/app /app/app
RUN chmod +x /app/app
ENTRYPOINT ["/app/app"]
