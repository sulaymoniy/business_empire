plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.businessempire.game"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // TODO: Choose your final applicationId before publishing. It can never change after the first
        //  upload to Google Play. The Kotlin package (namespace) can stay as it is.
        applicationId = "com.businessempire.game"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-demo"

        // MONETIZATION: flip to true once Google Play Billing is wired up (see MONETIZATION_TODO.md).
        buildConfigField("boolean", "STORE_ENABLED", "false")
        // Rich List: true = real people from assets/rich_list.json, false = built-in fictional tycoons.
        // Flip to false if a store reviewer objects to real names.
        buildConfigField("boolean", "USE_REAL_RICH_LIST", "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    // Images: only bundled photos (res/drawable-nodpi). No coil-network module, so no network access.
    implementation(libs.coil.compose)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
