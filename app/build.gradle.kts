import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties =
    Properties().apply {
        if (keystorePropertiesFile.exists()) {
            load(FileInputStream(keystorePropertiesFile))
        }
    }

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jmailen.kotlinter")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply true
    id("de.mannodermaus.android-junit5") version "1.11.2.0"
}

val kotlinVersion by extra("2.0.0")
val junit5Version by extra("5.11.2")
val mockkVersion by extra("1.13.13")

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "be.scri"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
                storeFile = keystoreProperties["storeFile"]?.let { file(it) }
                storePassword = keystoreProperties["storePassword"].toString()
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions.add("variants")
    productFlavors {
        create("core")
        create("fdroid")
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    detekt {
        toolVersion = "1.23.6"
        buildUponDefaultConfig = true
        allRules = false
        config = rootProject.files("detekt.yml")

    }

    kotlinter {
        failBuildWhenCannotAutoFormat = false
        ignoreFailures = false
    }

    namespace = "be.scri"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.play:core:1.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    api("joda-time:joda-time:2.10.13")
    api("com.github.tibbi:RecyclerView-FastScroller:e7d3e150c4")
    api("com.github.tibbi:reprint:2cb206415d")
    api("androidx.core:core-ktx:1.13.1")
    api("com.google.code.gson:gson:2.10.1")
    api("com.github.bumptech.glide:glide:4.14.2")
    ksp("com.github.bumptech.glide:ksp:4.14.2")
}

tasks.register<Copy>("moveFromi18n") {
    val locales = listOf("de", "es", "sv", "en-US")

    locales.forEach { locale ->
        val fromDir = "src/main/assets/i18n/Scribe-i18n/values/$locale/"
        val toDir = if (locale == "en-US") "src/main/res/values/" else "src/main/res/values-$locale/"
        val sourceDir = file(fromDir)

        if (sourceDir.exists()) {
            println("Preparing to move from $fromDir to $toDir")
            from(fileTree(fromDir))
            into(toDir)
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        } else {
            println("Source directory does not exist: $fromDir")
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(tasks.named("moveFromi18n"))
}
