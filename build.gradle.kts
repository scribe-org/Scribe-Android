// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion = "2.0.0"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
    }



    dependencies {
        classpath("io.nlopez.compose.rules:ktlint:0.4.17")
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:12.1.1")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
        classpath("org.jmailen.gradle:kotlinter-gradle:4.4.1")
    }
}

apply(plugin = "io.gitlab.arturbosch.detekt")
apply(plugin = "org.jmailen.kotlinter")
apply(plugin = "org.jetbrains.kotlinx.kover")


plugins {
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
