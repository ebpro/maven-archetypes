FROM maven:3.9-eclipse-temurin-21-alpine AS jlinkAlpineBuild
WORKDIR /myproject
COPY pom.xml .
COPY src src
RUN --mount=type=cache,id=mvncache,target=/root/.m2/repository,rw \
	mvn -B -Pjlink clean verify

FROM alpine AS finalJLinkAlpine
ARG IMAGE_NAME=
ENV IMAGE_NAME=${IMAGE_NAME}
COPY --from=jlinkAlpineBuild /myproject/target/image /${IMAGE_NAME}
ENTRYPOINT /${IMAGE_NAME}/bin/myapp
