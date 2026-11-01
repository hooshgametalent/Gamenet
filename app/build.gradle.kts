plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.gamenetmehdi.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gamenetmehdi.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "3.0"
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(
        files(
            gradle.gradleHomeDir!!.resolve("lib/junit-4.13.2.jar"),
            gradle.gradleHomeDir!!.resolve("lib/hamcrest-core-1.3.jar"),
        ),
    )
}
