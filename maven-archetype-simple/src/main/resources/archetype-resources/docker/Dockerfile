# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21-alpine AS libsBuild
WORKDIR /myproject
COPY pom.xml .
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=libs dependency:go-offline
COPY src src
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=libs clean verify

FROM libsBuild AS jlinkBuild
RUN --mount=type=cache,id=mvn-cache,target=/root/.m2/repository \
  mvn -B -Dapp.image=jlink clean verify

FROM ghcr.io/graalvm/graalvm-community:java21-ol9 AS nativeBuild
WORKDIR /myproject
RUN dnf install -y maven && dnf clean all
COPY pom.xml .
COPY src src
RUN --mount=type=cache,id=mvn-cache-native,target=/root/.m2/repository \
  mvn -B -Dapp.image=native clean verify

FROM eclipse-temurin:21-jre-alpine AS finalLibs
ARG APP_MAIN_CLASS
ENV APP_MAIN_CLASS=${APP_MAIN_CLASS}
WORKDIR /app
COPY --from=libsBuild /myproject/target/*.jar /app/app.jar
COPY --from=libsBuild /myproject/target/libs /app/libs
ENTRYPOINT ["sh", "-c", "java -cp /app/app.jar:/app/libs/* \"$APP_MAIN_CLASS\""]

FROM alpine AS finalJlink
ARG IMAGE_NAME=myapp
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=jlinkBuild /myproject/target/image /${IMAGE_NAME}
ENTRYPOINT ["/${IMAGE_NAME}/bin/myapp"]

FROM alpine AS finalNative
ARG IMAGE_NAME=myapp
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=nativeBuild /myproject/target/${IMAGE_NAME} /app/app
ENTRYPOINT ["/app/app"]
