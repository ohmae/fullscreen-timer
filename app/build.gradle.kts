import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.dependencyGuard)

    // for release
}

val applicationName = "Timer"
val versionMajor = 0
val versionMinor = 12
val versionPatch = 2

android {
    namespace = "net.mm2d.timer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "net.mm2d.timer"
        minSdk = 26
        targetSdk = 37
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
        multiDexEnabled = true
    }
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
    jvmToolchain(21)
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach {
            (it as VariantOutputImpl).outputFileName.set("$applicationName-${it.versionName.get()}.apk")
        }
    }
}

dependencies {
    implementation(platform(libs.androidxComposeBom))
    implementation(libs.androidxCore)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxActivity)
    implementation(libs.androidxComposeActivity)
    implementation(libs.androidxComposeAnimation)
    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.androidxComposeMaterialIconsExtended)
    implementation(libs.androidxComposeUi)
    implementation(libs.androidxComposeUiToolingPreview)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxFragment)
    implementation(libs.androidxLifecycleLiveData)
    implementation(libs.androidxLifecycleProcess)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleRuntimeCompose)
    implementation(libs.androidxLifecycleViewModel)
    implementation(libs.androidxHiltLifecycleViewModelCompose)
    implementation(libs.androidxConstraintLayout)
    implementation(libs.androidxDatastorePreferences)
    implementation(libs.androidxWebkit)
    implementation(libs.material)
    implementation(libs.playAppUpdate)
    debugImplementation(libs.androidxComposeUiTooling)

    implementation(libs.hiltAndroid)
    ksp(libs.hiltAndroidCompiler)

    implementation(libs.colorChooser)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxJunit)
    testImplementation(libs.kotlinxCoroutinesTest)

    // for release
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}
