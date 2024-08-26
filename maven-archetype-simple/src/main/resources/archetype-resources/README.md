# Project ${groupId}/${artifactId}

## Description
A brief description of what your project does and its purpose.

## Build the Project
Build the project and run all tests with `./mvnw verify` or `mvnw.cmd verify` for Windows.

## Run the application

### Old way with a fat jar

```shell
./mvnw -P jlink package
java -jar target/${artifactId}-${version}-withdependencies.jar
```

### New way with jlink

```shell
./mvnw -P jlink clean package
./target/image/bin/myapp
```
