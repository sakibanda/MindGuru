pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.itextsupport.com/android")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://repo.itextsupport.com/android")
        maven( "https://jitpack.io" )
        maven("https://sdk.smartlook.com/android/release")
    }
}

rootProject.name = "MindGuru"
include(":app")
 