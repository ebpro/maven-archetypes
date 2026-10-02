# Contributing

Thanks for contributing!

## Quick start

1. Fork & clone, then create a branch: `git checkout -b feat/your-change`
2. Build & test: `./mvnw -B clean verify`
3. Open a PR against `develop` using the PR template.

## Branches

| Branch | Purpose |
|--------|---------|
| `develop` | Default branch, receives PRs |
| `master` | Release / site branch (no direct PRs) |

## Archetypes

| Archetype | Artifact ID |
|-----------|-------------|
| Simple (no parent) | `maven-archetype-simple` |
| With parent POM | `maven-archetype-withparent` |

## Docker targets

Each archetype supports:
- `finallibs` (default): thin JAR + `target/libs/`
- `finaljlink`: JPMS custom runtime image
- `finalnative`: GraalVM native image (not in CI)

Activate via: `-Dapp.image=finaljlink` or `-Dapp.image=finalnative`

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
