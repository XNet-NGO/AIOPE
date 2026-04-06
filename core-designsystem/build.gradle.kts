plugins {
  id("aiope2.android.library")
  id("aiope2.android.library.compose")
  id("aiope2.spotless")
}

android {
  namespace = "com.aiope2.core.designsystem"
}

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.ui)
  api(libs.androidx.compose.ui.tooling)
  api(libs.androidx.compose.ui.tooling.preview)
  api(libs.androidx.compose.material.iconsExtended)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.foundation.layout)
  api(libs.androidx.compose.constraintlayout)
}
