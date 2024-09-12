plugins {
    id("com.android.library") version "8.0.2"
    id("org.jetbrains.kotlin.android") version "1.8.0"
    `maven-publish`
}

android {
    namespace = "com.app.bestbrain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.5.0")
    api("com.squareup.okhttp3:logging-interceptor:4.11.0")
    api("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    api("com.google.android.flexbox:flexbox:3.0.0")
    api("com.github.ybq:Android-SpinKit:1.4.0")
    implementation(kotlin("stdlib"))
}

tasks.withType<Jar> {
    from(sourceSets["main"].output)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = "com.github.salildts"
            artifactId = "bestbrainLib"
            version = "1.0.0"

            pom {
                name.set("BestBrain Library")
                description.set("A library for managing BestBrain features.")
                url.set("https://github.com/salildts/bestbrainLib")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("salildts")
                        name.set("Salil")
                        email.set("salildts@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/salildts/bestbrainLib.git")
                    developerConnection.set("scm:git:ssh://git@github.com:salildts/bestbrainLib.git")
                    url.set("https://github.com/salildts/bestbrainLib")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
}