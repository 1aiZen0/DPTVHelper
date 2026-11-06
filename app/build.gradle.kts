plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.dptvhelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dptvhelper"
        minSdk = 25
        targetSdk = 25
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
