package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MenuRegionSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_region_selection)
        setupVolumeButton()
        val opcion = intent.getStringExtra("Tipo")


        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val buttonEuropa: Button = findViewById(R.id.buttonEuropa)
        buttonEuropa.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Europa")
            startActivity(intent)
        }

        val buttonAsia: Button = findViewById(R.id.buttonAsia)
        buttonAsia.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Asia")
            startActivity(intent)
        }

        val buttonAfrica: Button = findViewById(R.id.buttonAfrica)
        buttonAfrica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Africa")
            startActivity(intent)
        }

        val buttonAmerica: Button = findViewById(R.id.buttonAmerica)
        buttonAmerica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "America")
            startActivity(intent)
        }

        val buttonOceania: Button = findViewById(R.id.buttonOceania)
        buttonOceania.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Oceania")
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
}