package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MenuRegionSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_region_selection)
        applyBackground()
        setupVolumeButton()
        val opcion = intent.getStringExtra("Tipo")


        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonEuropa: Button = findViewById(R.id.buttonEuropa)
        buttonEuropa.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Europa")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonAsia: Button = findViewById(R.id.buttonAsia)
        buttonAsia.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Asia")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonAfrica: Button = findViewById(R.id.buttonAfrica)
        buttonAfrica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Africa")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonAmerica: Button = findViewById(R.id.buttonAmerica)
        buttonAmerica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "America")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonOceania: Button = findViewById(R.id.buttonOceania)
        buttonOceania.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Oceania")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        val imageButtonSeleccionCantidad = findViewById<ImageButton>(R.id.imageButtonSeleccionCantidad)
        val currentState = getButtonState()

        if (currentState == "infinito") {
            imageButtonSeleccionCantidad.setImageResource(R.drawable.infinito)
        } else if (currentState=="10") {
            imageButtonSeleccionCantidad.setImageResource(R.drawable.diez)
        } else {
            imageButtonSeleccionCantidad.setImageResource(R.drawable.alfallo)
        }

        imageButtonSeleccionCantidad.setOnClickListener {
            val currentState2 = getButtonState()

            if (currentState2 == "infinito") {
                imageButtonSeleccionCantidad.setImageResource(R.drawable.diez)
                saveButtonState("10")  // Guardar el nuevo estado
            } else if (currentState2=="10") {
                imageButtonSeleccionCantidad.setImageResource(R.drawable.alfallo)
                saveButtonState("alfallo")  // Guardar el estado "infinito"
            } else {
                imageButtonSeleccionCantidad.setImageResource(R.drawable.infinito)
                saveButtonState("infinito")  // Guardar el estado "infinito"
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

    private fun getButtonState(): String {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        return sharedPreferences.getString("button_state", "infinito") ?: "infinito"
    }

    private fun saveButtonState(state: String) {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("button_state", state)
        editor.apply()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
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
}