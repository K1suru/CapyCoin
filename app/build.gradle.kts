import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Properties

// Las claves de firma se leen de local.properties (no se sube al control de versiones).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kapt)
}

base {
    archivesName.set("CapyCoin")
}

android {
    namespace = "com.example.gestiongastos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gestiongastos"
        minSdk = 24
        targetSdk = 35
        // versionCode solo puede subir (Android no deja instalar una versión menor encima)
        versionCode = 5
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // La llave propia (keystore + claves en local.properties) no está en el repositorio.
    // Si no existe, debug usa la firma de debug estándar de Android y release queda sin firmar.
    val releaseKeystore = file("keystore/capycoin.jks")
    val hasReleaseKey = releaseKeystore.exists() && localProps.getProperty("capycoin.storePassword") != null

    signingConfigs {
        if (hasReleaseKey) {
            create("capycoinRelease") {
                storeFile = releaseKeystore
                storePassword = localProps.getProperty("capycoin.storePassword")
                keyAlias = localProps.getProperty("capycoin.keyAlias", "capycoin")
                keyPassword = localProps.getProperty("capycoin.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Misma firma que release para poder actualizar la app instalada sin perder datos
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("capycoinRelease")
        }
        release {
            isMinifyEnabled = false
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("capycoinRelease")
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as BaseVariantOutputImpl
            if (buildType.name == "release") {
                output.outputFileName = "CapyCoin.apk"
            }
        }
    }
    compileOptions {
        // Permite usar java.time (YearMonth, Instant...) en Android 7 (API 24-25)
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.biometric)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}