# Best practices Maven archetypes

This project maintains some Maven archetypes.

* `fr.ebruno.maven.archetypes:simple` a simple standalone Java project.
* `fr.ebruno.maven.archetypes:simple-withparent`a CI ready one with a complete parent POM.

## fr.ebruno.maven.archetypes:simple

An archetype to generate a simple standalone maven project.

### Usage

To generate from the archetype :

```bash
mvn archetype:generate \
    -DarchetypeGroupId=fr.ebruno.maven.archetypes \
    -DarchetypeArtifactId=maven-archetype-simple
```

parameters can be passed directly :

```bash
mvn archetype:generate \
    -DarchetypeGroupId=fr.ebruno.maven.archetypes \
    -DarchetypeArtifactId=maven-archetype-simple \
    -DgroupId=demo.pkg \
    -DartifactId=MyApp \
    -Dversion=0.1.0-SNAPSHOT
```

The result can be compiled with maven

```console
cd MyApp
mvn verify
```

and executed with maven :

```console
mvn exec:java -Dexec.mainClass="demo.pkg.App"
```

or with java (beware no dependencies in the slim jar)

```console
cd MyAppHelloWorld
java -cp target/MyAppHelloWorld-1.0-SNAPSHOT.jar demo.pkg.App
```

### Web site

a web site can be generated in `target/site/index.html` with :

```console
mvn site
```

## fr.ebruno.maven.archetypes:simple-withparent

```bash
mvn  archetype:generate \
    -DarchetypeGroupId=fr.ebruno.maven.archetypes \
    -DarchetypeArtifactId=maven-archetype-withparent \
    -DgroupId=demo.pkg \
    -DartifactId=MyApp \
    -Dversion=0.1.0-SNAPSHOT
```

A parent pom is defined.
By default the jar is "runnable" (see `app.main.class` in `pom.xml`)

```bash
cd Myapp
mvn verify
java -jar target/MyApp-0.1.0-SNAPSHOT.jar
```

## A full git+maven+docker example

> **WARNING**: STILL WORK IN PROGESS

This project generates a complete Java+Maven project ready for Continuous Integration (CI).
It is ready for GitFlow, SonarQube (tests, code coverage, ...).
It can produce signed artifacts, fat jars, slim runtime with jLink, native executables with GraalVM 
and container images. The build itself can also be done in a container.

### Configuration (Once)

The configuration is done with environment variables.
For GitHub : GITHUBORG (GitHub account or organisation), GITHUBACTOR, GITHUBTOKEN, GITHUBORG (org or account)
and optionally for SonarQube SONAR_URL and SONAR_TOKEN (To install SonarQube see https://github.com/ebpro/sonarqube)

For GitHub the CLI is needed (https://cli.github.com/).

Those variables have to be stored on the CI server (see [GitHub Encrypted secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)).
The script below transforms the local variables in GitHub secrets.

```bash
bash -c 'for secret in GITHUBACTOR GITHUBTOKEN SONAR_URL SONAR_TOKEN; do \
eval gh secret set $secret --app actions  \
                           --body ${!secret} \
                           --org ${GITHUBORG} \
                           --visibility all; \
done'
```

## Each new project

> **NOTE**: Short version, a wrapper script is provided (READ IT BEFORE USE).

```bash
source <(curl -s https://raw.githubusercontent.com/ebpro/demomavenarchetype/develop/src/main/resources/archetype-resources/ci-wrappers.sh)
new-java-project <projectName> <groupId>
```

### Creation

Generate each new project with this maven archetype (adapt at least the bold parameters).

```bash
mvn --batch-mode archetype:generate \
    -DarchetypeGroupId=fr.ebruno.maven.archetypes \
    -DarchetypeArtifactId=maven-archetype-withparent \
    -DgithubAccount=<b>$GITHUBORG</b> \
    -DgroupId=<b>demo.pkg</b> \
    -DartifactId=<b>MyApp</b> \
    -Dversion=0.1.0-SNAPSHOT
```

To enable deployment in the repo during the CI a deployment key is needed for each repository.

```bash
tmpKeydir=$(mktemp --directory /tmp/ci-wrappers.XXXXXX)
ssh-keygen -q -t ed25519 -C "An autogenerated deploy key" -N "" -f ${tmpKeydir}/key
gh repo deploy-key add --allow-write "${tmpKeydir}/key.pub"
gh secret set SECRET_DEPLOY_KEY < "${tmpKeydir}/key"
rm -rf tmpKeydir
```

### GitFlow

1. Initialize git environment for GitFlow (develop and master branches).
2. Make a first commit.
3. Create the GitHub repository with the [GitHub Command line Interface](https://cli.github.com/).
4. Create an orphan gh-pages branch for the website.
5. Open the repository in a web browser.

```bash
git flow init -d && touch README.md && git add . && git commit -m "sets initial release." &&\
  gh repo create ${GITHUBORG}/${PWD##*/} --disable-wiki --private  --source=. --push &&\
    git checkout --orphan gh-pages && \
      git rm -rf . && touch index.html &&  \
      git add index.html && \
      git commit -m "sets initial empty site." && \
      git push --all \
    && git checkout develop &&\
  gh repo view --web
```

The project uses the [gitflow-maven-plugin](https://aleksandr-m.github.io/gitflow-maven-plugin) to manage GitFlow for java+Maven projet (branches and artifact version).

It is possible to easily start and finish a feature (see version in pom.xml).

```bash
mvn -B gitflow:feature-start  -DpushRemote=true -DfeatureName=UI
mvn -B gitflow:feature-start  -DpushRemote=true -DfeatureName=DB

mvn -B gitflow:feature-finish  -DpushRemote=true -DfeatureName=DB
mvn -B gitflow:feature-finish  -DpushRemote=true -DfeatureName=UI 
```

```bash
mvn -B gitflow:release-start -DpushRemote=true -DallowSnapshots=true -DuseSnapshotInRelease=true
mvn -B gitflow:release-finish -DpushRemote=true -DallowSnapshots=true
```

## Artefact packaging

### Compilation and execution

You can compile and package with unit tests.

```bash
mvn package
```

You can compile and package with unit tests and integration tests.

```bash
mvn verify
```

You can execute with maven (see app.mainClass property in pom.xml).

```bash
mvn exec:java
```

### Shaded Jar (close to FatJar or UberJar)

The profile `shadedjar` generates a fat jar with all the classes from the transitive dependencies.

```bash
mvn -Pshadedjar clean verify
ls -lh target/*.jar
```

```console
-rw-r--r--@ 1 bruno  wheel   3.1K May 20 16:09 target/MyApp-0.1.0-UI-SNAPSHOT-sources.jar
-rw-r--r--@ 1 bruno  wheel   2.6K May 20 16:09 target/MyApp-0.1.0-UI-SNAPSHOT-test-sources.jar
-rw-r--r--@ 1 bruno  wheel    38K May 20 16:09 target/MyApp-0.1.0-UI-SNAPSHOT-withdependencies.jar
-rw-r--r--@ 1 bruno  wheel   4.2K May 20 16:09 target/MyApp-0.1.0-UI-SNAPSHOT.jar
```

The application can then be executed without maven :

```bash
java -jar target/*-withdependencies.jar
```

### Jlink

Thanks to Jlink and a modular java project (jigsaw), it is possible to generate a minimal JRE with needed modules and dependencies.

```bash
mvn -Pjlink clean verify
du -hs target/image
```

```console
 44M    target/image
 ```

The application can then be launched without a JRE installed.

```bash
./target/image/bin/myapp
```

```console
Dec 02, 2022 11:18:56 PM fr.univtln.bruno.demos.App main
INFOS: Hello World! []
```

### GraalVM

It is also possible to generate a native binary with [GraalVM](https://www.graalvm.org/). An installation of GraalVM is needed and the package build-essential libz-dev and zlib1g-dev (zlib-devel, zlib-static et glibc-static). Only tested in linux.

```bash
sdk install java 22.3.r19-grl
sdk use java 22.3.r19-grl
```

```bash
mvn -Pnative clean verify
ls -lh target/testci
```

```console
-rwxr-xr-x 1 bruno users 13M  3 déc.  00:12 target/testci
```

```console
./target/testci
déc. 03, 2022 12:15:25 AM fr.univtln.bruno.demos.App main
INFO: Hello World! []
```

### Building with Maven in docker

It is possible to build and run the project with just docker installed. A wrapper to run maven and java
in a container but to work with the current directory is proposed. ~/.m2, ~/.ssh, ~/.gitconfig and the src directories are mounted. 
The environment variables needed for the project are also transmitted. The UID and GID are the one of the current user.

```bash
docker-mvn clean -P shadedjar package
docker run --rm \
  --mount type=bind,source="$(PWD)"/target/,target=/app,readonly \
  eclipse-temurin:17-jre-alpine \
    sh -c "java -jar /app/*-withdependencies.jar"
```

### Docker Multistage build

The file `docker\Dockerfile` is a multistage Dockerfile to build and deliver 
the application with several strategies (shaded jar, jlink, GraalVM) 
on several distributions (debian and alpine). 
To ease the use a wrapper for docker commands is provided in dockerw.sh

```bash
. ./ci-wrappers.sh
docker-wrapper-build
docker-wrapper-run
```

It is also possible to build all final target (Warning graalvm takes a long time to compile).

```bash
docker-wrapper-build-all
```

the result show the images and their size.

```bash
ebpro/testci   develop-finalShadedjarDebian   af3e072b35f7   2 hours ago   266MB
ebpro/testci   develop-finalShadedjarAlpine   38973a2aa588   2 hours ago   170MB
ebpro/testci   develop-finaljLinkDebian       db847ca5b281   2 hours ago   133MB
ebpro/testci   develop-finalJLinkAlpine       0cba42a81a33   2 hours ago   58.2MB
ebpro/testci   develop-finalGraalvmDebian     f5607a1e055f   2 hours ago   93.7MB
ebpro/testci   develop-finalGraalvmAlpine     d9c0573e4750   2 hours ago   18.8MB
```

It is also possible to run all the images :

```bash
docker-wrapper-run-all
```

```log
INFO: Hello World! []
  0,06s user 0,04s system 6% cpu 1,529 total
Running ebpro/testci:develop-finalShadedjarAlpine
Dec 05, 2022 4:37:06 PM fr.univtln.bruno.demos.App main
INFO: Hello World! []
  0,05s user 0,05s system 5% cpu 1,724 total
Running ebpro/testci:develop-finaljLinkDebian
Dec 05, 2022 4:37:07 PM fr.univtln.bruno.demos.App main
INFO: Hello World! []
  0,05s user 0,05s system 5% cpu 1,690 total
Running ebpro/testci:develop-finalJLinkAlpine
Dec 05, 2022 4:37:09 PM fr.univtln.bruno.demos.App main
INFO: Hello World! []
  0,04s user 0,05s system 5% cpu 1,723 total
Running ebpro/testci:develop-finalGraalvmDebian
Dec 05, 2022 4:37:10 PM fr.univtln.bruno.demos.App main
INFO: Hello World! []
  0,05s user 0,03s system 7% cpu 1,161 total
Running ebpro/testci:develop-finalGraalvmAlpine
Dec 05, 2022 4:37:12 PM fr.univtln.bruno.demos.App main
INFO: Hello World! []
```

### Quality

If a sonarqube server is available (i.e with https://github.com/ebpro/sonarqube).
Set the variable SONAR_URL and SONAR_TOKEN

```bash
mvn -P jacoco,sonar \
  -Dsonar.branch.name=$(git rev-parse --abbrev-ref HEAD | tr / _) \
  verify sonar:sonar 
```

### TODO: FIX LOST CODE COVERAGE

```bash
mvn clean verify
mvn -DskipTests=true \
    -Dsonar.branch.name=$(git rev-parse --abbrev-ref HEAD | tr / _) \
    -P jacoco,sonar \
    sonar:sonar
```

### Web site

```bash
mvn site:site
```
