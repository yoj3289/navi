plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.naviproj"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.naviproj"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.recyclerview:recyclerview-selection:1.1.0-rc03") //231210추가
    implementation ("androidx.recyclerview:recyclerview:1.1.0") //231210추가
    implementation ("androidx.cardview:cardview:1.0.0") //231210추가
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //그래프 표시 위해 추가하기

    implementation ("org.jsoup:jsoup:1.13.1")

    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")

    implementation ("com.github.bumptech.glide:glide:4.16.0") // 최신 버전 확인 필요
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.firebase:firebase-storage:20.3.0") // 최신 버전 확인 필요

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.viewpager2:viewpager2:1.0.0-alpha04")

    implementation ("androidx.appcompat:appcompat:1.3.0")
    implementation ("androidx.fragment:fragment:1.3.6")

}