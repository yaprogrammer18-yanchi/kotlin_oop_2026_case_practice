plugins {
    kotlin("jvm") version "1.9.24"
}

group = "org.boxGame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // база данных
    implementation("org.xerial:sqlite-jdbc:3.53.1.0")
    // Kotlin
    implementation(kotlin("stdlib"))

    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.openjfx:javafx-controls:21.0.3")
    implementation("org.openjfx:javafx-fxml:21.0.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}