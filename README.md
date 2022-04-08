## Important
Current version works with OpenRewrite 7.22.0-SNAPSHOT, so you need to build OpenRewrite locally with the latest master.

## Rewrite recipe starter

This repository serves as a template for building your own recipe JARs and publishing them to a repository where they can be applied on [app.moderne.io](https://app.moderne.io) against all of the public OSS code that is included there.

We include a sample recipe and test that just exists as a placeholder and is intended to be replaced by whatever recipe you are interested in writing.

Fork this repository and customize by:

1. Change the root project name in `settings.gradle.kts`.
2. Change the `group` in `build.gradle.kts`.
3. (Optional) Change the project name in `settings.gradle.kts`.
4. Change the package structure from `org.openrewrite` to whatever you want.

## Local Publishing for Testing

Before you publish your recipe module to an artifact repository, you may want to try it out locally.
To do this on the command line, run `./gradlew publishToMavenLocal` (or equivalently `./gradlew pTML`).
This will publish to your local maven repository, typically under `~/.m2/repository`.

Replace the groupId, artifactId, and recipe name in these samples with those you selected in the previous steps.

Gradle must be explicitly configured to resolve dependencies from maven local.
The root project of your gradle build, make your recipe module a dependency of the `rewrite` configuration:

```groovy
plugins {
    id("java")
    id("org.openrewrite.rewrite") version("5.14.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    rewrite("org.openrewrite.recipe:rewrite-recipe-starter:0.1.0-SNAPSHOT")
}

rewrite {
    activeRecipe("org.openrewrite.starter.MigrateToProviderApiRecipe")
}
```

Now you can run `mvn rewrite:run` or `gradlew rewriteRun` to run your recipe.

## Publishing to Artifact Repositories

This project is configured to publish to Moderne's open artifact repository.
Moderne's code search, refactoring, and modernization platform at [app.moderne.io](https://app.moderne.io) can draw recipes from this repository, as well as public repositories like [Maven Central](https://search.maven.org/).

Also see:

* Sonatype's instructions for [publishing to Maven Central](https://maven.apache.org/repository/guide-central-repository-upload.html) 
* Gradle's instructions on the [Gradle Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html).

### From Github Actions

The .github directory contains a Github action that will push a snapshot on every successful build.

Run the release action to publish a release version of a recipe.

### From the command line

To build a snapshot, run `./gradlew snapshot publish` to build a snapshot and publish it to Moderne's open artifact repository for inclusion at [app.moderne.io](https://app.moderne.io).

To build a release, run `./gradlew final publish` to tag a release and publish it to Moderne's open artifact repository for inclusion at [app.moderne.io](https://app.moderne.io).
