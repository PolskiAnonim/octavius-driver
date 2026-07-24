plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("maven-publish")
}

dependencies {
    implementation(projects.driver)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.hikaricp)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}
