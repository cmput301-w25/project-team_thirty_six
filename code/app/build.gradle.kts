plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidproject"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.espresso.intents)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation("com.squareup.picasso:picasso:2.8")
    implementation(libs.firebase.auth)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation("com.squareup.picasso:picasso:2.8")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation ("junit:junit:4.13.2")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-storage")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("org.mockito:mockito-android:5.0.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test:core:1.5.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    androidTestImplementation(libs.espresso.intents)

}
