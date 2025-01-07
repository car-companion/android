plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("org.qtproject.qt.gradleplugin") version("1.+")
}

QtBuild {
    projectPath = file("../my_car_companion")
    qtPath = file( "C:/QtAndroid/6.8.1")
    qtKitDir = file("C:/QtAndroid/6.8.1/android_arm64_v8a")
    //qtPath = file("C:\\QtAndroid\\6.8.1")
    //projectPath = file("C:\\QtApplication\\android\\my_car_companion")
    //qtKitDir = file("C:\\Qt\\6.8.1\\android_x86_64")
    //qtKitDir = file("C:\\Qt\\6.8.1\\android_arm64_v8a")
}

android {
    namespace = "com.dsd.carcompanion"
    compileSdk = 34

    packagingOptions.jniLibs.useLegacyPackaging = true

    defaultConfig {
        applicationId = "com.dsd.carcompanion"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    sourceSets {
        getByName("main") {
            jniLibs.setSrcDirs(listOf("libs"))
            assets.setSrcDirs(listOf("assets"))
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.colorpicker)
    implementation(libs.flexbox)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
}