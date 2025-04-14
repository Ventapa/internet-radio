package com.example.k2025_03_25_basic_radio

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioViewModel(application: Application) : AndroidViewModel(application) {
    private var radioService: RadioService? = null
    private var isBound = false

    private val _radioState = MutableStateFlow(RadioState(null, false, 0.5f))
    val radioState: StateFlow<RadioState> = _radioState.asStateFlow()
    
    // List of available radio stations
    val radioStations = listOf(
        RadioStation(
            "Radio Paradise",
            "https://stream.radioparadise.com/mp3-192",
            "https://yt3.googleusercontent.com/1-sb7SOHP7VCoEOhMhzJXX38QPzPjAdkA-4b8e3-cahWl5nDgSiG3YRnmm6ZXpWIRHqf5s4u=s900-c-k-c0x00ffffff-no-rj"
        ),
        RadioStation(
            "Jazz24",
            "https://live.wostreaming.net/direct/ppm-jazz24mp3-ibc1",
            "https://npr.brightspotcdn.com/dims4/default/032ce25/2147483647/strip/true/crop/900x900+0+0/resize/1760x1760!/format/webp/quality/90/?url=http%3A%2F%2Fnpr-brightspot.s3.amazonaws.com%2F98%2Ff7%2F48229ba341b0b1c8933834130d10%2Fjazz24.jpg"
        ),
        RadioStation(
            "BBC Radio 1",
            "http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one",
            "https://ichef.bbci.co.uk/images/ic/1200x675/p0cbjbk8.jpg"
        ),
        RadioStation(
            "KEXP",
            "https://kexp-mp3-128.streamguys1.com/kexp128.mp3",
            "https://play-lh.googleusercontent.com/0LcDI5r90wP77ANQcXHl0gjg1GecYSBqcCKFsFxt1nq89EOXbw820UMaFW2z66RPIm4f"
        ),
        RadioStation(
            "NPR News",
            "https://npr-ice.streamguys1.com/live.mp3",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSIVtlLTLsHc7I9jONXGElH-xgurhSFXeL0FA&s"
        ),
        RadioStation(
            "Classical KUSC",
            "https://kusc.streamguys1.com/kusc128.mp3",
            "https://play-lh.googleusercontent.com/T5tfBCj39SESBTj0Ot9KnbmsBcbpYBonJF7OflclVaoRocl6D0CO7osaijQlbLyKDPfP"
        ),
        RadioStation(
            "Radio Swiss Pop",
            "http://stream.srg-ssr.ch/m/pop/mp3_128",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJ3xidGVroH0sMeqv9KJ-k5cANPhA90QfVGQ&s"
        ),
        RadioStation(
            "Chilltrax",
            "https://ais-sa1.streamon.fm/7117_128k.aac",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSs-922hj_HGmvrCtBb36dWSleU5JsnKiIOsQ&s"
        ),
        RadioStation(
            "Deep House Lounge",
            "http://198.58.98.83:8356/stream",
            "https://i1.sndcdn.com/avatars-000196423494-ggyazv-t1080x1080.jpg"
        ),
        RadioStation(
            "Dance Wave Retro",
            "https://stream.dancewave.online/retro",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRb-tlvvRh5qtg9z8ywEpoXwxld_RjkbJkWuA&s"
        )
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioService.RadioBinder
            radioService = binder.getService()
            isBound = true
            updateStateFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            radioService = null
            isBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(getApplication(), RadioService::class.java)
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        getApplication<Application>().startService(intent)
    }

    fun playStation(station: RadioStation) {
        viewModelScope.launch {
            radioService?.playRadio(station)
            updateStateFromService()
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            radioService?.let { service ->
                if (service.getCurrentState().isPlaying) {
                    service.pauseRadio()
                } else {
                    service.resumeRadio()
                }
                updateStateFromService()
            }
        }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch {
            radioService?.setVolume(volume)
            updateStateFromService()
        }
    }

    private fun updateStateFromService() {
        viewModelScope.launch {
            radioService?.let { service ->
                _radioState.value = service.getCurrentState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
}