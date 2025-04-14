package com.example.k2025_03_25_basic_radio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import com.example.k2025_03_25_basic_radio.ui.theme.K2025_03_25_basic_radioTheme

class MainActivity : ComponentActivity() {
    private val viewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            K2025_03_25_basic_radioTheme {
                val radioState by viewModel.radioState.collectAsState()
                
                RadioUI(
                    currentStation = radioState.currentStation,
                    isPlaying = radioState.isPlaying,
                    volume = radioState.volume,
                    onStationSelected = { station ->
                        viewModel.playStation(station)
                    },
                    onVolumeChanged = { newVolume ->
                        viewModel.setVolume(newVolume)
                    },
                    onPlayPauseToggle = {
                        viewModel.togglePlayPause()
                    }
                )
            }
        }
    }
}
