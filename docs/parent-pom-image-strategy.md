# Parent POM image strategy — consolidated suggestions

This document records the recommended changes for `maven-parentpom` around application image modes. The parent POM is treated as **read-only** for the current archetype work; these are suggestions only.

## Goal

Make application packaging explicit and Docker-friendly with one discriminator:

- `app.image=libs` — thin application jar plus `target/libs/` runtime dependencies
- `app.image=jlink` — JPMS-validated custom runtime
- `app.image=native` — GraalVM native executable
- `-Pshadedjar` — optional fat-jar mode, explicitly outside the `app.image` discriminator

The default archetype experience should be `app.image=libs`.

## Contract

Suggested properties:

- `app.image` — empty in parent, defaulted to `libs` by archetypes
- `app.main.class` — required by `libs`, `jlink`, and `native`
- `app.module.name` — required for reliable `jlink`; archetype default is `${package}`
- `app.native.static` — optional, default `false`

Docker targets should map 1:1:

- `libs` → `finalLibs`
- `jlink` → `finalJlink`
- `native` → `finalNative`

## Evidence in current SOTA-2026 parent

Observed in `/var/folders/4j/m_xh7zw15hgb3yht31rr3_r40000gn/T/opencode/parentpom-sota2026/pom.xml`:

- `<app.main.class />` is declared empty at line 18.
- `mavenjigsaw.version=1.1.3` is declared at line 155.
- The `requireProperty app.main.class` enforcer rule is commented out around lines 683–690.
- `exec-maven-plugin` already uses `${app.main.class}` around line 1091.
- `maven-shade-plugin` pluginManagement exists around lines 1103–1159.
- `shadedjar` profile starts at line 1640.
- `native` profile starts at line 1651.
- `jlink` profile starts at line 1686.
- `jlink` currently uses `<ignoreMissingDeps>true</ignoreMissingDeps>` and launcher `myapp=${project.module}/${app.main.class}`.
- `native` currently hardcodes `--static` and does not declare `<mainClass>${app.main.class}</mainClass>`.

## Suggested parent changes

### P0 — required for the `app.image` model

1. **Add `app.image` discriminator**
   - Declare `<app.image/>` in parent properties.
   - Use property-activated profiles for `libs`, `jlink`, and `native`.
   - Archetypes default `app.image` to `libs`.

2. **Add `libs` profile**
   - Activate when `app.image=libs`.
   - Run `maven-dependency-plugin:copy-dependencies` with `includeScope=runtime` into `${project.build.directory}/libs`.
   - Keep the normal thin jar as the main artifact.

3. **Demote `shadedjar` to optional mode**
   - Keep `-Pshadedjar` for compatibility.
   - Do not include `shadedjar` in the `app.image` discriminator.
   - Document it as an explicit legacy or convenience mode.

4. **Require `app.main.class` in app modes**
   - Move the currently commented `requireProperty app.main.class` enforcer rule into the `libs`, `jlink`, and `native` profiles.
   - This avoids enforcing the property for libraries or non-app modules.

5. **Make `jlink` strict and explicit**
   - Default `ignoreMissingDeps` to `false`.
   - Add a `verify`-phase validation step.
   - Use an explicit `app.module.name` property for launcher/module metadata instead of relying on an ad hoc `project.module` value.

6. **Add explicit `mainClass` to `native`**
   - Use `<mainClass>${app.main.class}</mainClass>` in the GraalVM plugin.
   - Avoid silent reliance on discovery.

### P1 — recommended improvements

1. **Make native static mode configurable**
   - Add `<app.native.static>false</app.native.static>` as the default.
   - Enable `--static` only when `app.native.static=true`.
   - This avoids portability issues on macOS and non-Linux environments.

2. **Document the parent app contract**
   - Document `app.image`, `app.main.class`, `app.module.name`, and `app.native.static`.
   - Include run commands for each mode and the matching Docker target.

### P2 — follow-up alignment

1. **Ship `jlink` strictness together with archetype `module-info` fixes**
   - `withparent` currently has a broken `module-info.java` template.
   - Strict `jlink` validation and archetype module-name fixes should be released together to avoid breaking generated apps.

2. **Revisit plugin naming consistency**
   - Parent currently uses the property `mavengrallvm.version` for the GraalVM native Maven plugin.
   - Keep the existing property name for compatibility if possible, but consider a corrected alias in a future major version.

## Suggested profile shape

Conceptually, the parent should evolve toward:

- `libs`
  - property activation: `app.image=libs`
  - requires `app.main.class`
  - copies runtime dependencies into `target/libs`

- `jlink`
  - property activation: `app.image=jlink`
  - requires `app.main.class` and `app.module.name`
  - builds a custom runtime with strict dependency validation

- `native`
  - property activation: `app.image=native`
  - requires `app.main.class`
  - builds a native executable, with `--static` optional

- `shadedjar`
  - manual activation only: `-Pshadedjar`
  - remains available but is not part of the `app.image` model

## Archetype impact

### `maven-archetype-simple`

Because `simple` is standalone, it can adopt the full model immediately:

- default `app.image=libs`
- inline `libs` profile
- optional `shadedjar` profile
- fixed `jlink` profile
- new `native` profile
- Dockerfile targets `finalLibs`, `finalJlink`, `finalNative`

### `maven-archetype-withparent`

Because `withparent` inherits from the parent POM, its rollout is split:

- immediate / parent-decoupled:
  - default `app.image=libs`
  - explicit `app.main.class`
  - fixed `module-info.java`
  - local `libs` build path if needed
- deferred until parent changes land:
  - full property-driven exclusivity for `jlink` and `native`
  - strict `jlink` validation
  - configurable native static mode

## Docker target mapping

Recommended generated Dockerfiles:

- `finalLibs`
  - build thin jar + `libs/`
  - run with `java -cp app.jar:libs/* ${APP_MAIN_CLASS}`
- `finalJlink`
  - build custom runtime
  - run the launcher
- `finalNative`
  - build native executable
  - run the binary

This lets CD pick a target directly from `app.image` without implicit fallbacks.

## Released parent `0.1.19` constraints observed during implementation

Observed in `/Users/bruno/.m2/repository/fr/ebruno/maven/poms/maven-parentpom/0.1.19/maven-parentpom-0.1.19.pom`:

- Java `21`
- `mavendependency.version=3.7.1`
- `mavenjigsaw.version=1.1.3`
- `mavengrallvm.version=0.9.26`
- `maven-dependency-plugin` version is managed as `${mavendependency.version}`
- the released `jlink` profile uses `${project.module}` instead of `app.module.name`
- the released `jlink` profile has malformed `jlinkOptions` observed as `--no-header-files,- -no-man-pages,- -strip-debug,--compress=2`
- the released `native` profile does not provide an explicit generated-app `mainClass` and hardcodes `--static`

Because of these constraints, the immediate `withparent` archetype work ships only the safe `libs` / `finalLibs` subset. `jlink` and `native` remain gated on parent POM alignment.

## Implemented archetype-side changes

### `maven-archetype-simple`

- default generated POM uses `app.image=libs`
- generated POM provides `libs`, `shadedjar`, `jlink`, and `native` profiles
- Dockerfile provides `finalLibs`, `finalJlink`, and `finalNative`
- README documents the local run commands and Docker build commands
- `module-info.java` cleaned to remove the unused Lombok requirement/comment typo

### `maven-archetype-withparent`

- generated POM defaults to `app.image=libs`
- generated POM adds `app.module.name=${package}`
- generated POM includes a local `libs` profile that copies runtime dependencies to `${project.build.directory}/libs`
- `module-info.java` fixed to valid JPMS syntax
- Dockerfile replaced with a safe `finalLibs` target using Java `21`
- Docker Compose aligned to `docker/Dockerfile`, project-root context, and explicit build args
- README documents `libs` as the initial supported mode and marks `jlink` / `native` as pending parent alignment

## Validation performed

- `mvn -B -pl maven-archetype-simple -am -DskipTests package`
- `mvn -B -pl maven-archetype-withparent -am -DskipTests package`
- XML validation of the edited generated POM templates
- inspection of packaged archetype resources to confirm:
  - `withparent` remains unfiltered
  - `simple` Maven expressions remain escaped
  - `module-info.java` files are valid templates
  - Docker and Compose files match the project-root build context
