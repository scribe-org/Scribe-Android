buildscript {
    val kotlinVersion = "2.0.0"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.9.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.6") // Kover still needs to be here if configured at root
    }
}

plugins {
    // Only Kover is applied at the root project level.
    // Other plugins (Detekt, Kotlinter, KSP, etc.) will be applied directly in their respective modules.
    id("org.jetbrains.kotlinx.kover") version "0.7.6" apply true
}

kover {
    // Kover 0.7.x configuration remains the same
}

koverReport {
    verify {
        rule("Coverage must be more than 60%") {
            minBound(60)
        }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    // No more `apply(plugin = ...)` here. Modules will declare their own plugins explicitly.
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}