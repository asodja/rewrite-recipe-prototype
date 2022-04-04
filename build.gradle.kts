import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`

    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("nebula.release") version "15.3.1"

    id("nebula.maven-manifest") version "17.3.2"
    id("nebula.maven-nebula-publish") version "17.3.2"
    id("nebula.maven-resolved-dependencies") version "17.3.2"

    id("nebula.contacts") version "5.1.0"
    id("nebula.info") version "9.3.0"

    id("nebula.javadoc-jar") version "17.3.2"
    id("nebula.source-jar") version "17.3.2"
}

apply(plugin = "nebula.publish-verification")

configure<nebula.plugin.release.git.base.ReleasePluginExtension> {
    defaultVersionStrategy = nebula.plugin.release.NetflixOssStrategies.SNAPSHOT(project)
}

group = "org.openrewrite.recipe"
description = "Rewrite recipes."

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

val rewriteVersion = "7.20.0"

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    implementation("org.openrewrite:rewrite-java:${rewriteVersion}")
    implementation("org.openrewrite:rewrite-groovy:${rewriteVersion}")
    runtimeOnly("org.openrewrite:rewrite-java-11:${rewriteVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    testImplementation("org.openrewrite:rewrite-test:${rewriteVersion}")
    testImplementation("org.assertj:assertj-core:3.22.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames")
    testLogging {
        lifecycle.exceptionFormat = TestExceptionFormat.FULL
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<ContactsExtension> {
    val j = Contact("team@moderne.io")
    j.moniker("Team Moderne")
    people["team@moderne.io"] = j
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")
        }
    }
}

publishing {
  repositories {
      maven {
          name = "moderne"
          url = uri("https://us-west1-maven.pkg.dev/moderne-dev/moderne-recipe")
      }
  }
}
