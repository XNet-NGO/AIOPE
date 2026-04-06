plugins {
  id("aiope2.android.library")
  id("aiope2.android.library.compose")
  id("aiope2.android.feature")
  id("aiope2.android.hilt")
  id("aiope2.spotless")
}

android {
  namespace = "com.aiope2.feature.chat"
}

dependencies {
  implementation(project(":core-data"))

  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.lifecycle.viewModelCompose)

  // openai-kotlin
  implementation(libs.openai.client)
  implementation(libs.ktor.client.okhttp)

  // markdown
  implementation(libs.markwon.core)
}
