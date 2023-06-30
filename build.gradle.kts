plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("dagger.hilt.android.plugin") version "2.46.1" apply false
    id("com.github.ben-manes.versions") version "0.47.0" apply false

    // for release
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
