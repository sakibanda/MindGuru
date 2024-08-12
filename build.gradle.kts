// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //    apply plugin: 'java'
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    alias(libs.plugins.androidLibrary) apply false
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    alias(libs.plugins.daggerHilt) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.google.services) // Add this line
        // other classpath dependencies
    }
}