package com.example.k2025_03_25_basic_radio

data class RadioState(
    val currentStation: RadioStation?,
    val isPlaying: Boolean,
    val volume: Float
)