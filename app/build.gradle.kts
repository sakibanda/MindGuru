import com.google.protobuf.gradle.id
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.kotlin.fir.analysis.checkers.checkCasting

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
    id("com.google.protobuf")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}


protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.22.3" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.1"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
            task.plugins {
                id("grpc") {
                    option("lite")
                }
            }
        }
    }
}
sentry {
    projectName.set("mindguru")
}

android {

    namespace = "app.mindguru.android"
    compileSdk = 35

    lint{
        abortOnError = false
        checkReleaseBuilds = false
        checkDependencies = false
        checkAllWarnings = false
        checkTestSources = false
        checkGeneratedSources = false
    }

    defaultConfig {
        versionCode = 5
        versionName = "0.05"


        applicationId = "app.mindguru.android"
        minSdk = 26
        targetSdk = 35

        ndk.debugSymbolLevel = "FULL"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false

            isShrinkResources = false
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

    packaging {
        resources {
            excludes += "mozilla/public-suffix-list.txt"
            excludes += "META-INF/**"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            //excludes += "/system/framework/org.apache.http.legacy.jar"
            //excludes += "protolite-well-known-types-18.0.0-runtime.jar"
        }
    }
}

dependencies {
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation("co.yml:ycharts:2.1.0")
    //implementation("com.himanshoe:charty:2.0.0-alpha01")

    // https://mvnrepository.com/artifact/io.ktor/ktor-client-android
    //runtimeOnly("io.ktor:ktor-client-android:2.3.12")
    //implementation("javax.xml.bind:jaxb-api:2.3.1")
    //implementation("java.beans:java.beans:1.0")
    implementation ("io.grpc:grpc-okhttp:1.65.0") {
        exclude(group = "com.google.protobuf")
    }
    implementation ("io.grpc:grpc-protobuf-lite:1.65.0") {
    }
    implementation ("io.grpc:grpc-stub:1.65.0") {
        exclude(group = "com.google.protobuf")
    }
    implementation(libs.androidx.work.runtime.ktx)
    //implementation("com.google.protobuf:protobuf-java:3.19.4")

    compileOnly ("org.apache.tomcat:annotations-api:6.0.53") // necessary for Java 9+
    /*implementation("io.grpc:grpc-okhttp:1.57.2")
    implementation("io.grpc:grpc-protobuf:1.66.0")
    implementation("io.grpc:grpc-stub:1.65.1")*/
    //implementation("com.google.protobuf:protobuf-java:4.27.3")

    //Voice Activity Detection
    implementation(libs.android.vad.silero)

    /*implementation("com.google.protobuf:protobuf-javalite:3.19.4")
    implementation("io.grpc:grpc-protobuf-lite:1.66.0"){
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }*/
    //implementation("io.grpc:grpc-protobuf:1.66.0")

    //implementation("com.google.protobuf:protobuf-java-util:4.27.3")
    // gRPC for Google Cloud API.
    // Google Cloud API.
    /*implementation("com.google.cloud:google-cloud-speech:4.42.0"){
        //exclude(group = "com.google.api.grpc", module = "grpc-protobuf")
        //exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }*/

    // Markdown support for notes
    implementation("com.halilibo.compose-richtext:richtext-ui-material3:0.17.0")
    implementation("com.halilibo.compose-richtext:richtext-commonmark:0.17.0")
    /*implementation (libs.richtext.common)
    implementation (libs.richtext.ui.material)
    implementation (libs.richtext.ui.material3)*/
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jre8:1.5.31")
    //implementation("com.google.cloud:google-cloud-language:0.21.1-beta")
    //implementation("commons-cli:commons-cli:1.8.0")
    //add org.apache.commons
   /* implementation("com.google.cloud:google-cloud-speech:4.42.0"){
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }*/


    implementation(libs.jemoji)

    implementation (libs.coil.compose)
    implementation (libs.coil.gif)

    implementation (libs.shimmer)


    //Scroll Bar
    implementation(libs.lazyColumnScrollbar)


    //implementation( libs.accompanist.navigation.animation)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.constraintlayout)
    implementation(project(":sound"))
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

    implementation(platform(libs.firebase.bom)){
        /* exclude(group = "com.google.protobuf")
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")*/
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation(libs.firebase.analytics){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    /*implementation(libs.firebase.vertexai){
    }*/

    implementation(libs.firebase.messaging) {
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation(libs.firebase.functions){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation(libs.firebase.appcheck.playintegrity){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation(libs.firebase.appcheck.debug){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }

    implementation (libs.firebase.config){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.auth){
        exclude(group = "androidx.biometric", module = "biometric")
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.firestore) {
        //protolite-well-known-types jar issue build error
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.perf){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.inappmessaging) {
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.analytics){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }
    implementation (libs.firebase.crashlytics){
        exclude(group = "com.google.firebase" , module = "protolite-well-known-types")
    }

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