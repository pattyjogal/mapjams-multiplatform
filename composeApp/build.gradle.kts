import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

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
    alias(libs.plugins.kotlinCocoapods)

    alias(libs.plugins.googleServices) apply true // Apply directly for Android app module config
    alias(libs.plugins.firebaseCrashlytics) apply true // Apply directly for Android app module config
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

    cocoapods {
        // Required properties
        // Specify the required Pod version here
        // Otherwise, the Gradle project version is used
        version = "1.0"
        summary = "The iOS Map Jams app"
        homepage = "http://example.com"

        ios.deploymentTarget = "14.1"

        // Optional properties
        // Configure the Pod name here instead of changing the Gradle project name
        name = "MapJams"

        framework {
            // Required properties
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "MapJams"

            // Optional properties
            // Specify the framework linking type. It's dynamic by default.
            isStatic = false
            // Dependency export
            // Uncomment and specify another project module if you have one:
            // export(project(":<your other KMP module>"))
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            transitiveExport = false // This is default.
        }

        pod("FirebaseCrashlytics") {
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        // Maps custom Xcode configuration to NativeBuildType
        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
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

            implementation(project.dependencies.platform(libs.firebase.bom))
            // Analytics dependency (recommended for Crashlytics user data)
            implementation(libs.firebase.analytics)
            // Crashlytics dependency
            implementation(libs.firebase.crashlytics)
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
        versionCode = 2
        versionName = "0.2.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            firebaseCrashlytics {
                nativeSymbolUploadEnabled = false
                mappingFileUploadEnabled = !isMinifyEnabled
            }
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