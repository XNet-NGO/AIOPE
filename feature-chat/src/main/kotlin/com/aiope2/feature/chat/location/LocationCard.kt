package com.aiope2.feature.chat.location

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre
import org.ramani.compose.Symbol
import org.ramani.compose.UiSettings

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocationCard(latitude: Double, longitude: Double, altitude: Double? = null, speed: Double? = null, bearing: Double? = null, accuracy: Double? = null) {
  val view = LocalView.current
  Card(
    modifier = Modifier.fillMaxWidth().padding(4.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
        .clip(RoundedCornerShape(8.dp))
        .pointerInteropFilter { event ->
          when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
          }
          false // don't consume — let MapLibre handle it
        },
    ) {
      val initialPos = remember {
        CameraPosition(target = org.maplibre.android.geometry.LatLng(latitude, longitude), zoom = 15.5)
      }
      val style = remember {
        org.maplibre.android.maps.Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")
      }
      val ui = remember {
        UiSettings(
          scrollGesturesEnabled = true,
          zoomGesturesEnabled = true,
          rotateGesturesEnabled = false,
          tiltGesturesEnabled = false,
          doubleTapGesturesEnabled = true,
          quickZoomGesturesEnabled = true,
          isLogoEnabled = false,
          isAttributionEnabled = false,
        )
      }
      MapLibre(
        modifier = Modifier.fillMaxSize(),
        styleBuilder = style,
        cameraPosition = initialPos,
        uiSettings = ui,
      ) {
        Symbol(center = org.maplibre.android.geometry.LatLng(latitude, longitude), color = "Red", size = 1.4f)
      }
    }
  }
}
