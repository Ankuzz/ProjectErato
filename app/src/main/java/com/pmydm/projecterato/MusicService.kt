package com.pmydm.projecterato

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null // Este servicio no es para interactuar directamente con la interfaz, por lo que retornamos null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayer.create(this, R.raw.musica)
        mediaPlayer.isLooping = true // Hacer que la música se repita

        val isVolumeOn = intent?.getBooleanExtra("volume_state", true) ?: true
        if (isVolumeOn) {
            mediaPlayer.start() // Iniciar música
        } else {
            mediaPlayer.pause() // Pausar música
        }

        return START_STICKY // Mantener el servicio en segundo plano
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release() // Liberar recursos del MediaPlayer
        }
    }
}
