plugins {
    id("com.android.application") version "7.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.6.20" apply false
    id("dagger.hilt.android.plugin") version "2.41" apply false
    id("com.github.ben-manes.versions") version "0.42.0" apply false

    // for release
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
