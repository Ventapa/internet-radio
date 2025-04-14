package com.example.k2025_03_25_basic_radio

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun RadioUI(
    currentStation: RadioStation?,
    isPlaying: Boolean,
    volume: Float,
    onStationSelected: (RadioStation) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onPlayPauseToggle: () -> Unit,
    stations: List<RadioStation> = RadioViewModel(LocalContext.current.applicationContext as Application).radioStations
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        currentStation?.let { station ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = station.imageUrl,
                        contentDescription = station.name,
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = { onVolumeChanged(maxOf(0f, volume - 0.1f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease Volume",
                    modifier = Modifier.size(24.dp)
                )
            }
            Slider(
                value = volume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onVolumeChanged(minOf(1f, volume + 0.1f)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase Volume",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            "Available Stations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stations) { station ->
                StationItem(
                    station = station,
                    isSelected = currentStation?.url == station.url,
                    isPlaying = isPlaying && currentStation?.url == station.url,
                    onClick = { onStationSelected(station) }
                )
            }
        }
    }
}

@Composable
fun StationItem(
    station: RadioStation,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = station.imageUrl,
                contentDescription = station.name,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (isSelected) {
                    Text(
                        text = if (isPlaying) "▶ Playing" else "⏸ Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}