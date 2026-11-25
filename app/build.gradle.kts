plugins {
    id("com.android.application")
}

android {
    namespace = "vn.linh.doomweather"
    compileSdk = 36

    defaultConfig {
        applicationId = "vn.linh.doomweather"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.2.2"
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
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
