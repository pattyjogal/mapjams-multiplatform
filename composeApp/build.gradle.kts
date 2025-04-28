import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.secrets)
    kotlin("plugin.serialization") version "2.0.21"
    id("app.cash.sqldelight") version "2.0.2"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
//            isStatic = true
        }

        iosTarget.compilations.getByName("main") {
            val nskeyvalueobserving by cinterops.creating {
                defFile(
                    project.file(
                        "src/nativeInterop/cinterop/nskeyvalueobserving.def"
                    )
                )
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.navigation.fragment)
            implementation(libs.androidx.navigation.ui)

            // Feature module support for Fragments
            implementation(libs.androidx.navigation.dynamic.features.fragment)

            implementation(libs.android.maps.compose)

            implementation(libs.android.driver)

            implementation(libs.koin.android)

            implementation(libs.androidx.media3.exoplayer)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.navigation.compose)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)

            // JSON serialization library, works with the Kotlin serialization plugin
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            implementation(libs.lifecycle.viewmodel.compose)

            api(libs.permissions.compose)
            implementation(libs.permissions.location)

            implementation(libs.kermit)

            implementation(libs.basic.haptic)
        }

        iosMain.dependencies {
            implementation(libs.native.driver)
        }
    }
}

android {
    namespace = "al.pattyjog.mapjams"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "al.pattyjog.mapjams"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.play.services.location)
    implementation(libs.foundation.layout.android)
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

sqldelight {
    databases {
        create("MapJamsDatabase") {
            packageName.set("al.pattyjog.mapjams")
        }
    }
}