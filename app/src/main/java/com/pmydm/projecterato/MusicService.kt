package com.pmydm.projecterato

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null // Este servicio no está diseñado para ser enlazado
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Solo inicializamos el mediaPlayer si no está ya inicializado
        if (!::mediaPlayer.isInitialized) {
            mediaPlayer = MediaPlayer.create(this, R.raw.musica)
            mediaPlayer.isLooping = true // La música se repetirá
        }

        val isVolumeOn = intent?.getBooleanExtra("volume_state", true) ?: true
        if (isVolumeOn) {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start() // Iniciar música solo si no está reproduciéndose
            }
        } else {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause() // Pausar música solo si está en reproducción
            }
        }

        return START_STICKY // Mantener el servicio en segundo plano incluso si la actividad cambia
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release() // Liberar recursos del MediaPlayer
        }
    }
}
