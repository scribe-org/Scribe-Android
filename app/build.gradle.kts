import java.io.FileInputStream
import java.util.Locale
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
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("jacoco")
    kotlin("plugin.serialization") version "1.9.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

jacoco {
    toolVersion = "0.8.12"
}

val junit5Version by extra("5.11.2")
val mockkVersion by extra("1.13.13")

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "be.scri"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    kover {
        verify {
            rule {
                isEnabled = true
                name = "Coverage must be more than 60%"
                bound {
                    minValue = 60
                }
            }
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
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
        compose = true
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
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
        toolVersion = "1.23.8"
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.files("detekt.yml"))
    }


    kotlinter {
        failBuildWhenCannotAutoFormat = false
        ignoreFailures = false
    }

    namespace = "be.scri"

    applicationVariants.all {
        val variantName = this.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val unitTests = "test${variantName}UnitTest"
        val androidTests = "connected${variantName}AndroidTest"

        val exclusions =
            listOf(
                // Data binding.
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )

        tasks.register<JacocoReport>("jacoco${variantName}CodeCoverage") {
            dependsOn(listOf(unitTests, androidTests))
            group = "Reporting"
            description = "Generate Jacoco coverage reports for the $variantName build"
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
            sourceDirectories.setFrom(layout.projectDirectory.dir("src/main"))
            classDirectories.setFrom(
                files(
                    fileTree(layout.buildDirectory.dir("intermediates/javac/")) {
                        exclude(exclusions)
                    },
                    fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/")) {
                        exclude(exclusions)
                    },
                ),
            )
            executionData.setFrom(
                files(
                    fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) },
                ),
            )
        }
    }
}

// MARK: Dependencies

dependencies {
    detektPlugins("io.nlopez.compose.rules:detekt:0.4.17")
    lintChecks("com.slack.lint.compose:compose-lint-checks:1.4.2")

    // MARK: AndroidX

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    debugImplementation("androidx.fragment:fragment-testing:1.8.8")
    implementation("androidx.test.ext:junit-ktx:1.2.1")

    // MARK: Room Database

    ksp("androidx.room:room-compiler:2.7.1")
    implementation("androidx.room:room-runtime:2.7.1")

    // MARK: Kotlin

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.0")

    // MARK: Layout and UI

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.exifinterface:exifinterface:1.4.1")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // MARK: Jetpack Compose

    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material:1.8.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // MARK: Activity Compose

    implementation("androidx.activity:activity-compose")

    // MARK: Navigation Compose

    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-compose:$2.8.4")

    // MARK: Testing

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // MARK: Instrumentation Tests

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.2")

    // MARK: UI Tests

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // MARK: Android Testing

    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    // MARK: JUnit 5

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")

    // MARK: AndroidJUnit4

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("io.mockk:mockk-android:1.13.5")

    // MARK: Other

    api("joda-time:joda-time:2.10.13")
    api("com.github.tibbi:RecyclerView-FastScroller:e7d3e150c4")
    api("com.github.tibbi:reprint:2cb206415d")
    api("androidx.core:core-ktx:1.16.0")
    api("com.google.code.gson:gson:2.10.1")
    api("com.github.bumptech.glide:glide:4.14.2")
    ksp("com.github.bumptech.glide:ksp:4.14.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

tasks.register<Copy>("moveFromi18n") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    destinationDir = file("src/main/res")

    val locales =
        file("src/main/assets/i18n/Scribe-i18n/values")
            .listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()
    locales.forEach { locale ->
        val fromDir = file("src/main/assets/i18n/Scribe-i18n/values/$locale/")
        val targetDir =
            if (locale == "en-US") {
                "values"
            } else {
                "values-$locale"
            }

        if (fromDir.exists()) {
            println("Copying from $fromDir to $targetDir")
            from(fromDir) {
                into(targetDir)
            }
        } else {
            println("Source directory does not exist: $fromDir")
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(tasks.named("moveFromi18n"))
}

tasks.withType(Test::class) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    reports {
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacoco.xml"))
        xml.required.set(true)
        csv.required.set(true)
    }
}
