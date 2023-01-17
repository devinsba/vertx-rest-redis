import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
  mavenCentral()
}

val vertxVersion = "3.9.2"
val junitVersion = "5.3.2"

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))

  implementation("io.vertx:vertx-core")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-redis-client")

  implementation("org.slf4j:slf4j-api:2.0.6")
  implementation("ch.qos.logback:logback-classic:1.3.5")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
  mainClassName = "io.vertx.core.Launcher"
}

val mainVerticleName = "io.vertx.starter.MainVerticle"
val watchForChange = "src/**/*.java"
val doOnChange = "${projectDir}/gradlew classes"

tasks {
  test {
    useJUnitPlatform()
  }

  getByName<JavaExec>("run") {
    args = listOf("run", mainVerticleName, "--redeploy=${watchForChange}", "--launcher-class=${application.mainClassName}", "--on-redeploy=${doOnChange}")
  }

  withType<ShadowJar> {
    classifier = "fat"
    manifest {
      attributes["Main-Verticle"] = mainVerticleName
    }
    mergeServiceFiles()
  }
}
