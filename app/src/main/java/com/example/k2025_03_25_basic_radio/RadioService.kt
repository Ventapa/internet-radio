package com.example.k2025_03_25_basic_radio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class RadioService : Service() {
    private val binder = RadioBinder()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private var currentStation: RadioStation? = null
    private var isPlaying = false
    private var volume = 0.5f
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "RadioServiceChannel"
    }

    inner class RadioBinder : Binder() {
        fun getService(): RadioService = this@RadioService
    }

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("RadioThread").also { it.start() }
        handler = Handler(handlerThread.looper)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Radio Service"
            val descriptionText = "Radio playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun playRadio(station: RadioStation, onCompletion: () -> Unit = {}) {
        handler.post {
            try {
                if (currentStation?.url == station.url && isPlaying) {
                    pauseRadio()
                    return@post
                }
                
                // Request audio focus
                requestAudioFocus()
                
                // Release previous media player
                mediaPlayer?.release()
                currentStation = station
                
                // Start foreground service with notification
                startForegroundWithNotification(station)

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(station.url)
                    prepareAsync()
                    setOnPreparedListener {
                        setVolume(volume, volume)
                        start()
                        this@RadioService.isPlaying = true
                        updateNotification(station)
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("MediaPlayer", "Error: what=$what, extra=$extra")
                        this@RadioService.isPlaying = false
                        false
                    }
                    setOnCompletionListener {
                        this@RadioService.isPlaying = false
                        onCompletion()
                    }
                    setOnBufferingUpdateListener { _, percent ->
                        Log.d("RadioService", "Buffering: $percent%")
                    }
                }
            } catch (e: Exception) {
                Log.e("RadioService", "Error playing radio", e)
                isPlaying = false
            }
        }
    }
    
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> pauseRadio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseRadio()
                        AudioManager.AUDIOFOCUS_GAIN -> resumeRadio()
                    }
                }.build()
            
            audioFocusRequest?.let { request ->
                val result = audioManager.requestAudioFocus(request)
                if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.w("RadioService", "Audio focus not granted")
                }
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus({ focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> pauseRadio()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseRadio()
                    AudioManager.AUDIOFOCUS_GAIN -> resumeRadio()
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }
    
    private fun startForegroundWithNotification(station: RadioStation) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing: ${station.name}")
            .setContentText("Internet Radio")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun updateNotification(station: RadioStation) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing: ${station.name}")
            .setContentText("Internet Radio")
            .setSmallIcon(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
            .setContentIntent(pendingIntent)
            .build()
            
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun pauseRadio() {
        handler.post {
            mediaPlayer?.pause()
            isPlaying = false
            currentStation?.let { updateNotification(it) }
        }
    }

    fun resumeRadio() {
        handler.post {
            requestAudioFocus()
            mediaPlayer?.start()
            isPlaying = true
            currentStation?.let { updateNotification(it) }
        }
    }

    fun setVolume(newVolume: Float) {
        handler.post {
            volume = newVolume
            mediaPlayer?.setVolume(volume, volume)
        }
    }

    fun getCurrentState(): RadioState {
        return RadioState(currentStation, isPlaying, volume)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handlerThread.quitSafely()
        
        // Release audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        
        stopForeground(true)
    }
}