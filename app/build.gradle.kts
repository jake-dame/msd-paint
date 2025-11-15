plugins {

    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.ksp)

    alias(libs.plugins.google.services)

}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.msdpaint"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.msdpaint"
        minSdk = 33
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        viewBinding = true
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // androidx - compose, material3, etc.
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Test
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)

    debugImplementation(libs.androidx.fragment.testing.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Jetpack Room
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    // Ksp
    ksp(libs.androidx.room.compiler)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)

    // Material
    implementation(libs.material)

    // Ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization.jvm)
    implementation(libs.ktor.client.serialization)

    implementation(libs.ktor.serialization.kotlinx.json)

    // Google - Firebase, Gson, Play, etc.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.play.services) // kt layer for google play stuff
    implementation(libs.play.services.auth)

    // "Google Sign In Button Library" - https://github.com/joyner-perez/GoogleSignInButtonLibrary
    implementation(libs.googlesigninbuttonlibrary)

    // "Color picker" - https://github.com/QuadFlask/colorpicker
    implementation(libs.colorpicker.v0015)
}
