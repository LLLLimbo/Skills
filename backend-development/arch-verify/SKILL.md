---
name: arch-verify
description: Verify Java architecture rules with ArchUnit. Use when you need to define or validate package or layer boundaries, dependency constraints, slices, or cyclic dependencies in Java projects, including during refactors or when adding governance checks to CI builds.
---

# ArchUnit Verification

## Workflow

1. Identify the architectural rules to enforce (layers, packages, slices, cycles, coding rules).
2. Choose the test framework (JUnit 4, JUnit 5, or plain unit tests).
3. Add the matching ArchUnit dependency.
4. Write ArchUnit rules in test sources and run them in CI.
5. Use the Maven plugin when you need cross-repo governance.

## Dependencies

### JUnit 4

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit4</artifactId>
  <version>1.4.1</version>
  <scope>test</scope>
</dependency>
```

### JUnit 5

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit5</artifactId>
  <version>1.4.1</version>
  <scope>test</scope>
</dependency>
```

### Other test frameworks

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit</artifactId>
  <version>1.4.1</version>
  <scope>test</scope>
</dependency>
```

## On-demand examples

- Run `scripts/fetch-examples.sh` to clone examples only when needed.
- Use `--dest /tmp/archunit-examples` to override the destination.
- The default destination `/tmp/archunit-examples` is auto-cleaned when it already exists.
- Use `--clean` to overwrite a custom destination.
- Use `example-junit4`, `example-junit5`, or `example-plain` as the starting point.

## Resources

- Use https://github.com/TNG/ArchUnit-Examples.git for runnable examples.
- Read `references/arch-unit-maven-plugin.md` to configure Maven-based governance.
- Check https://www.archunit.org/userguide for rule APIs and patterns.
- Check https://github.com/TNG/ArchUnit for source and release notes.
