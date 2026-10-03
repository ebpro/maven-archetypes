# Contributing

Thanks for contributing!

## Quick start

1. Fork & clone, then create a branch: `git checkout -b feat/your-change`
2. Build & test: `./mvnw -B clean verify`
3. Open a PR against `main` using the PR template.

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Default branch (always deployable), receives PRs |

## Archetypes

| Archetype | Artifact ID |
|-----------|-------------|
| Simple (no parent) | `maven-archetype-simple` |
| With parent POM | `maven-archetype-withparent` |

## Docker targets

The `app.image` Maven property and the Docker `--target` stage names are
**different** — the property selects the Maven build profile, the `--target`
selects the Docker stage. Both are shown below.

| Target | `docker build --target` | Maven profile (`-Dapp.image`) | Description |
|--------|------------------------|-------------------------------|-------------|
| Thin JAR + libs | `finallibs` | `libs` (default) | JAR + `target/libs/` |
| Custom runtime | `finaljlink` | `jlink` | JPMS jlink image |
| Native image | `finalnative` | `native` | GraalVM native (not in CI) |

Build:
```bash
docker build --target finallibs --build-arg APP_MAIN_CLASS=com.example.App -t myapp .
docker build --target finaljlink -t myapp .
```

## Conventions

- Follow the existing archetype structure.
- Keep changes minimal and focused; one concern per PR.
- Use Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, ...).
- Docker templates use `app.image` activation, not separate profile IDs.

## CI gates

A PR is mergeable when:
- CI (build + tests) is green
- Docker build matrix is green
- Security workflow is green
- SonarQube quality gate passes
