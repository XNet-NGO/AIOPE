package com.aiope2.feature.chat.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre
import org.ramani.compose.Symbol

@Composable
fun LocationCard(
  latitude: Double,
  longitude: Double,
  altitude: Double? = null,
  speed: Double? = null,
  bearing: Double? = null,
  accuracy: Double? = null
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(4.dp),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column {
      // Map — quarter mile ≈ zoom 15.5
      Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))) {
        MapLibre(
          modifier = Modifier.fillMaxSize(),
          styleBuilder = org.maplibre.android.maps.Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"),
          cameraPosition = CameraPosition(
            target = org.maplibre.android.geometry.LatLng(latitude, longitude),
            zoom = 15.5
          )
        ) {
          Symbol(
            center = org.maplibre.android.geometry.LatLng(latitude, longitude),
            color = "Red",
            size = 1.2f
          )
        }
      }

      // Location details
      Column(Modifier.padding(10.dp)) {
        Text("${latitude.fmt(6)}, ${longitude.fmt(6)}",
          fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          altitude?.let { Text("Alt: ${it.fmt(1)}m", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
          bearing?.let { Text("Bearing: ${it.fmt(0)}°", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          speed?.let { Text("Speed: ${(it * 3.6).fmt(1)} km/h", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
          accuracy?.let { Text("Acc: ${it.fmt(1)}m", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
        }
      }
    }
  }
}

private fun Double.fmt(d: Int) = "%.${d}f".format(this)
