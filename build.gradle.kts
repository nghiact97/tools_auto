plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.javarpa"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass = "com.javarpa.App"
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.desktop/java.awt=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED"
    )
}

javafx {
    version = "11"
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.graphics")
}

repositories {
    mavenCentral()
}

dependencies {
    // Global Hotkey
    implementation("com.github.kwhat:jnativehook:2.2.2")
    // OCR
    implementation("net.sourceforge.tess4j:tess4j:4.5.5")
    // JSON
    implementation("com.google.code.gson:gson:2.11.0")
    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.13")
    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.13")
}

tasks.shadowJar {
    archiveBaseName = "javarpa"
    archiveClassifier = ""
    archiveVersion = "1.0"
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "com.javarpa.App"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
