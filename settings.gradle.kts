pluginManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google().content {
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
            includeGroupAndSubgroups("androidx")
        }
        gradlePluginPortal().content {
            includeGroupAndSubgroups("com.github.ben-manes")
        }
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "dagger.hilt.android.plugin" ->
                    useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google().content {
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
            includeGroupAndSubgroups("androidx")
        }
        mavenCentral()
    }
}

rootProject.name = "fullscreen-timer"
include(":app")
