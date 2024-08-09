import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.sentry)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "app.mindguru.android"
    compileSdk = 35

    defaultConfig {
        versionCode = 1
        versionName = "1.0"


        applicationId = "app.mindguru.android"
        minSdk = 24
        targetSdk = 35

        ndk.debugSymbolLevel = "FULL"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions{
        jvmTarget = "17"
    }
    buildFeatures{
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging{
        resources{
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\Android\\AmazeAI\\keystores\\keystore.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }
}

dependencies {
    implementation(libs.jemoji)

    implementation (libs.coil.compose)
    implementation (libs.coil.gif)

    implementation (libs.shimmer)
    implementation (libs.richtext.common)
    implementation (libs.richtext.ui.material)
    implementation (libs.richtext.ui.material3)


    //Scroll Bar
    implementation(libs.lazyColumnScrollbar)


    //implementation( libs.accompanist.navigation.animation)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.constraintlayout)
    ksp("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.room.ktx)

    implementation(libs.material3)
    implementation(libs.material.icons)

    implementation(libs.androidx.credentials){
        exclude(group = "androidx.biometric", module = "biometric")
    }
    implementation(libs.androidx.credentials.play.services.auth){
        exclude(group = "androidx.biometric", module = "biometric")
    }
    implementation(libs.googleid){
        exclude(group = "androidx.biometric", module = "biometric")
    }

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.vertexai)

    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    implementation (libs.firebase.config)
    implementation (libs.firebase.auth){
        exclude(group = "androidx.biometric", module = "biometric")
    }
    implementation (libs.firebase.firestore)
    implementation (libs.firebase.perf)
    implementation (libs.firebase.inappmessaging)
    implementation (libs.firebase.analytics)
    implementation (libs.firebase.crashlytics)

    implementation(libs.billing.ktx)
    implementation(libs.androidx.appcompat)

    //hilt
    implementation(libs.hilt.android){
        exclude(group = "com.squareup", module = "javapoet")
    }
    kapt(libs.hilt.compiler){
        exclude(group = "com.squareup", module = "javapoet")
    }
    implementation(libs.navigation.hilt){
        exclude(group = "com.squareup", module = "javapoet")
    }
    implementation(libs.javapoet)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}