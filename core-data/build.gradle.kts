plugins {
  id("aiope2.android.library")
  id("aiope2.android.hilt")
  id("aiope2.spotless")
}

android {
  namespace = "com.aiope2.core.data"
}

dependencies {
  api(project(":core-model"))
  api(project(":core-network"))
  api(project(":core-preferences"))
}
