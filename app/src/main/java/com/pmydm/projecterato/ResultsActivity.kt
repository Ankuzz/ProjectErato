package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        applyBackground()
        setupVolumeButton()

        // Obtener referencias a los TextView
        val numeroAciertos: TextView = findViewById(R.id.numeroAciertos)
        val numeroFallos: TextView = findViewById(R.id.numeroFallos)

        // Obtener los datos pasados por Intent
        val aciertos = intent.getIntExtra("aciertos", 0)
        val fallos = intent.getIntExtra("fallos", 0)

        // Establecer los valores en los TextView
        numeroAciertos.text = aciertos.toString()
        numeroFallos.text = fallos.toString()

        // Botón para volver al menú principal
        val buttonMenu: Button = findViewById(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun setupVolumeButton() {
        val volumeButton = findViewById<ImageButton>(R.id.imageButtonVolumen)

        // Obtener SharedPreferences
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        var isVolumeOn = sharedPreferences.getBoolean("volume_state", true) // Por defecto, true

        // Actualizar el estado del botón
        fun updateButtonState(isVolumeOn: Boolean) {
            if (isVolumeOn) {
                volumeButton.setImageResource(R.drawable.iconovolumenencendido)
            } else {
                volumeButton.setImageResource(R.drawable.iconovolumenapagado)
            }
        }

        // Inicializar el estado del botón
        updateButtonState(isVolumeOn)

        // Configurar el listener del botón
        volumeButton.setOnClickListener {
            isVolumeOn = !isVolumeOn // Cambiar el estado de volumen
            sharedPreferences.edit().putBoolean("volume_state", isVolumeOn).apply()
            updateButtonState(isVolumeOn)

            // Crear un Intent para iniciar o detener el servicio
            val musicServiceIntent = Intent(this, MusicService::class.java)
            if (isVolumeOn) {
                startService(musicServiceIntent) // Iniciar servicio si el volumen está activado
            } else {
                stopService(musicServiceIntent) // Detener servicio si el volumen está desactivado
            }
        }
    }




    private fun applyBackground() {
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val fondoGuardado = prefs.getString("fondo", "fondoapp")

        // Obtener el layout raíz de la actividad
        val rootLayout: ConstraintLayout = findViewById(R.id.rootLayout)

        when (fondoGuardado) {
            "fondoapp" -> rootLayout.setBackgroundResource(R.drawable.fondoapp)
            "fondochina" -> rootLayout.setBackgroundResource(R.drawable.fondochina)
            else -> rootLayout.setBackgroundResource(R.drawable.fondoapp)
        }
    }


    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
