import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.github.ben-manes.versions")

    // for release
}

val applicationName = "Timer"
val versionMajor = 0
val versionMinor = 1
val versionPatch = 0

android {
    compileSdk = 33

    namespace = "net.mm2d.timer"
    defaultConfig {
        applicationId = "net.mm2d.timer"
        minSdk = 26
        targetSdk = 33
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        base.archivesName.set("${applicationName}-${versionName}")
        multiDexEnabled = true
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName =
                    "${applicationName}-${versionName}.apk"
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isTestCoverageEnabled = true
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.activity:activity-ktx:1.5.1")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.webkit:webkit:1.5.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")

    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-android-compiler:2.43.2")

    implementation("net.mm2d.color-chooser:color-chooser:0.5.2")

    testImplementation("junit:junit:4.13.2")

    // for release
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.toUpperCase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf { !isStable(candidate.version) }
}
