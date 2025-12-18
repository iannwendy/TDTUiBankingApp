plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.tdtumobilebanking"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tdtumobilebanking"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Tự động chạy Stripe backend server trước khi build debug
afterEvaluate {
    tasks.findByName("preDebugBuild")?.dependsOn(rootProject.tasks.named("startStripeBackend"))
}

configurations.all {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.accompanist.permissions)

    // Hilt & Kotlin
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Media & Images
    implementation(libs.coil.compose)

    // Maps & Location
    implementation(libs.maps)
    implementation(libs.location)
    implementation(libs.maps.utils.ktx)
    implementation(libs.play.services.auth)

    // CameraX for eKYC
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Stripe Payment
    implementation("com.stripe:stripe-android:20.48.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Task để lấy SHA-1 fingerprint
tasks.register("printSha1") {
    doLast {
        val keystoreFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
        val keystorePassword = "android"
        val keyAlias = "androiddebugkey"
        
        if (keystoreFile.exists()) {
            project.exec {
                commandLine(
                    "keytool",
                    "-list",
                    "-v",
                    "-keystore", keystoreFile.absolutePath,
                    "-alias", keyAlias,
                    "-storepass", keystorePassword
                )
            }
        } else {
            println("Debug keystore không tìm thấy tại: ${keystoreFile.absolutePath}")
            println("Chạy lệnh sau trong terminal:")
            println("keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android")
        }
    }
}
