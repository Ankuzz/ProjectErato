package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
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
        }
    }

    private fun setupVolumeButton() {
        val volumeButton = findViewById<ImageButton>(R.id.imageButtonVolumen)

        // Obtener SharedPreferences
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val isVolumeOn = sharedPreferences.getBoolean("volume_state", true) // Por defecto, true

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
            val newState = !sharedPreferences.getBoolean("volume_state", true)
            sharedPreferences.edit().putBoolean("volume_state", newState).apply()
            updateButtonState(newState)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
