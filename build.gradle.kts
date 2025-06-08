// Top-level (plugins y config general)
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0" // Usa la última versión de Kotlin
    id("application")
}

// Repositorios (dónde descargar dependencias)
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // Necesario para dependencias de GitHub
}

// Dependencias
dependencies {
    implementation("com.github.revanced:revanced-cli:3.0.0") // ReVanced CLI oficial
    implementation("com.github.CUPUL-MIU-04:revanced-patches-kitsune:main-SNAPSHOT") // Tus parches personalizados
    // Otras dependencias comunes:
    implementation("org.smali:dexlib2:2.5.2") // Para manipulación de APKs
    implementation("com.google.guava:guava:31.1-jre") // Utilidades
}

// Configuración de la aplicación (si es ejecutable)
application {
    mainClass.set("com.revanced.cli.Main") // Clase principal de ReVanced CLI
}