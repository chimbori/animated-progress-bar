val kotlin_version: String by rootProject.extra
plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-android-extensions")
}

android {
  compileSdkVersion(29)
  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(29)
  }
}

dependencies {
  implementation("androidx.annotation:annotation:1.1.0")
  implementation("androidx.appcompat:appcompat:1.0.2")
  implementation("androidx.core:core-ktx:1.2.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
}
repositories {
  mavenCentral()
}
