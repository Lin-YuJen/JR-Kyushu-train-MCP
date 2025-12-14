plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("com.gradleup.shadow") version "9.2.2"
    id("io.ktor.plugin") version "3.3.1"
}

group = "com.enix"
version = "0.1.0"

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

val mcpVersion = "0.7.2"
val slf4jVersion = "2.0.17"
val ktorVersion = "3.3.1"
val log4jVersion = "2.25.2"

dependencies {
    implementation("org.slf4j:slf4j-nop:${slf4jVersion}")
    implementation("io.modelcontextprotocol:kotlin-sdk:${mcpVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("io.ktor:ktor-server-di:${ktorVersion}")
    implementation("io.ktor:ktor-server-config-yaml:${ktorVersion}")

    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.fleeksoft.ksoup:ksoup-jvm:0.2.5")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.apply {
    // Disable distribution task
    listOf(distZip, distTar, shadowDistZip, shadowDistTar)
        .forEach { it { enabled = false } }
}