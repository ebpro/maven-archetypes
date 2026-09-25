# Project ${groupId}/${artifactId}

## Description
A brief description of what your project does and its purpose.

## Run modes
The default image mode is `libs`.

### libs
```shell
./mvnw clean package
java -cp "target/${artifactId}-${version}.jar:target/libs/*" ${package}.App
```

### jlink
```shell
./mvnw -Dapp.image=jlink clean package
./target/image/bin/myapp
```

### native
```shell
./mvnw -Dapp.image=native clean package
./target/${artifactId}
```

## Docker
```shell
docker build --target finalLibs --build-arg APP_MAIN_CLASS=${package}.App -t ${artifactId}:libs -f docker/Dockerfile .
docker build --target finalJlink -t ${artifactId}:jlink -f docker/Dockerfile .
docker build --target finalNative --build-arg IMAGE_NAME=${artifactId} -t ${artifactId}:native -f docker/Dockerfile .
```
