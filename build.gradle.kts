plugins {
  id("com.android.library")
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
}
