plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.detekt)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.zenithtasks"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zenithtasks"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.zenithtasks.HiltTestRunner" // Use Hilt test runner for instrumentation tests
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Consider enabling for release builds later
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    packaging { // Needed for Hilt/Coroutines sometimes
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "35.0.0"
    ndkVersion = "28.0.13004108"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)

    // Core & Base UI (Keep activity-ktx, remove appcompat/material/constraintlayout if fully Compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx) // Use activity-ktx
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle runtime

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Data (Local - Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DI (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core) // Include core

    // Location & Activity Services
    implementation(libs.play.services.location)

    // Widget (Glance)
    implementation(libs.androidx.glance.appwidget)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk.android) // Unit tests mocking
    testImplementation(libs.mockk.agent)   // Unit tests mocking
    testImplementation(libs.turbine)       // Flow testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose test BOM
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Compose testing
    // Hilt testing dependencies (add if needed for instrumented tests)
    // androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

// Optional: Configure Detekt task defaults if needed
detekt {
    config.setFrom(files("$rootDir/detekt.yml")) // Point to your config file
    buildUponDefaultConfig = true // Use default config as a base
    // source.setFrom(files("src/main/java", "src/main/kotlin")) // Usually inferred correctly
}
