    import org.gradle.kotlin.dsl.implementation

    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.jetbrains.kotlin.android)
        alias(libs.plugins.ksp)
        id("com.google.gms.google-services")
    }


    android {
        namespace = "com.example.sadaruang"
        compileSdk = 36

        defaultConfig {
            applicationId = "com.example.sadaruang"
            minSdk = 26
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"


            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }


        packaging {
            resources {
                excludes += "/META-INF/*"
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = false
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17 // Ubah dari 11 ke 17
            targetCompatibility = JavaVersion.VERSION_17 // Ubah dari 11 ke 17
        }
        kotlinOptions {
            jvmTarget = "17" // Tambahkan ini jika belum ada
        }
    }



    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.cardview)
        implementation(libs.androidx.material3)

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        val room_version = "2.6.1"
        implementation("androidx.room:room-runtime:$room_version")
        implementation("androidx.room:room-ktx:$room_version")
        ksp("androidx.room:room-compiler:$room_version")
        implementation("com.github.chrisbanes:PhotoView:2.3.0")
        implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
        implementation("androidx.work:work-runtime-ktx:2.9.0")
        implementation("org.dhatim:fastexcel:0.18.3")
        implementation("org.dhatim:fastexcel-reader:0.18.3")
        implementation("com.fasterxml:aalto-xml:1.3.2")
        implementation("org.codehaus.woodstox:stax2-api:4.2.1")
        implementation("com.fasterxml.woodstox:woodstox-core:6.5.1")
        implementation("javax.xml.stream:stax-api:1.0-2")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        ksp("com.github.bumptech.glide:compiler:4.16.0")
        implementation("com.google.firebase:firebase-auth:24.0.1")
        implementation("com.google.android.gms:play-services-auth:21.5.1")
        implementation("com.google.firebase:firebase-firestore:26.2.0")
        implementation("com.google.android.gms:play-services-auth:21.0.1")
    }