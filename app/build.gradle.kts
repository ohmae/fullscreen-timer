import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.gradleVersions)
    alias(libs.plugins.dependencyGuard)

    // for release
}

val applicationName = "Timer"
val versionMajor = 0
val versionMinor = 8
val versionPatch = 0

android {
    compileSdk = 35

    namespace = "net.mm2d.timer"
    defaultConfig {
        applicationId = "net.mm2d.timer"
        minSdk = 26
        targetSdk = 35
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
        multiDexEnabled = true
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName =
                    "$applicationName-$versionName.apk"
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            enableAndroidTestCoverage = true
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
        jvmToolchain(17)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    lint {
        abortOnError = true
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxActivity)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxFragment)
    implementation(libs.androidxLifecycleLiveData)
    implementation(libs.androidxLifecycleProcess)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewModel)
    implementation(libs.androidxConstraintLayout)
    implementation(libs.androidxDatastorePreferences)
    implementation(libs.androidxWebkit)
    implementation(libs.material)
    implementation(libs.playAppUpdate)

    implementation(libs.hiltAndroid)
    ksp(libs.hiltAndroidCompiler)

    implementation(libs.colorChooser)

    testImplementation(libs.junit)

    debugImplementation(libs.leakcanary)
    debugImplementation(libs.bundles.flipper)

    // for release
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}

fun isStable(
    version: String,
): Boolean {
    val versionUpperCase = version.uppercase(Locale.getDefault())
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf { !isStable(candidate.version) && isStable(currentVersion) }
}
