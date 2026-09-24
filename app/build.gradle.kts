import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties()
if (versionPropsFile.exists()) {
    FileInputStream(versionPropsFile).use { stream ->
        versionProps.load(stream)
    }
}
val appVersionCode = versionProps.getProperty("versionCode", "1").toInt()
val appVersionName = versionProps.getProperty("versionName", "1.0.0")

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.dragontiger.wqpz"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "VERSION_NAME", "\"$appVersionName\"")
        buildConfigField("int", "VERSION_CODE", "$appVersionCode")
        buildConfigField("String", "DEFAULT_GITHUB_OWNER", "\"masterfoodsstore\"")
        buildConfigField("String", "DEFAULT_GITHUB_REPO", "\"dragon-vs-tiger\"")
        buildConfigField("String", "APK_FILENAME_PREFIX", "\"MyGame-v\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val propFile = rootProject.file("release-keystore.properties")
        val releaseProps = Properties()
        if (propFile.exists()) {
            FileInputStream(propFile).use { releaseProps.load(it) }
        }

        val customStorePath = System.getenv("RELEASE_STORE_FILE")
            ?: releaseProps.getProperty("storeFile")
            ?: if (file("${rootDir}/release.keystore").exists()) "${rootDir}/release.keystore" else null

        val releaseKeyFile = customStorePath?.let { file(it) } ?: file("${rootDir}/debug.keystore")

        create("releaseConfig") {
            if (releaseKeyFile.exists()) {
                storeFile = releaseKeyFile
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                    ?: releaseProps.getProperty("storePassword")
                    ?: "android"
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                    ?: releaseProps.getProperty("keyAlias")
                    ?: "androiddebugkey"
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
                    ?: releaseProps.getProperty("keyPassword")
                    ?: "android"
            }
        }
        create("debugConfig") {
            val debugKey = file("${rootDir}/debug.keystore")
            if (debugKey.exists()) {
                storeFile = debugKey
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("releaseConfig")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
