plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.tictactoe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tictactoe"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("main") {
            kotlin.srcDir("src/main/kotlin")
            java.srcDir("src/main/java")
        }
    }
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)

    implementation(libs.androidx.activity.compose)


//    SQLite dependencies
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.sqlite.framework)

    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

}

