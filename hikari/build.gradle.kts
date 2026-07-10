plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("maven-publish")
}

group = "io.github.octavius-framework"
version = "0.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":driver"))
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(25)
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}
