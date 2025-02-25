package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class ChangeBackgroundActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_background)

        setupVolumeButton()

        val imageViewFondoApp: ImageView = findViewById(R.id.imageViewFondoApp)
        val imageViewFondoChina: ImageView = findViewById(R.id.imageViewFondoChina)

        // Recuperar las SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)

        // Obtener el fondo guardado en las SharedPreferences
        val fondoGuardado = prefs.getString("fondo", "fondoapp") // "fondoapp" es el valor por defecto

        // Obtener el layout raíz
        val rootLayout: ConstraintLayout = findViewById(R.id.rootLayout)

        // Establecer el fondo basado en el valor guardado
        when (fondoGuardado) {
            "fondoapp" -> {
                rootLayout.setBackgroundResource(R.drawable.fondoapp)
                imageViewFondoApp.setImageResource(R.drawable.fondoapp)
            }
            "fondochina" -> {
                rootLayout.setBackgroundResource(R.drawable.fondochina)
                imageViewFondoChina.setImageResource(R.drawable.fondochina)
            }
            else -> {
                rootLayout.setBackgroundResource(R.drawable.fondoapp)
                imageViewFondoApp.setImageResource(R.drawable.fondoapp)
            }
        }

        whiteBackground()

        // Configurar el clic para cambiar el fondo al de "fondoapp"
        imageViewFondoApp.setOnClickListener {
            setBackground(R.drawable.fondoapp)
            // Guardar en SharedPreferences
            saveBackgroundPreference("fondoapp")
            whiteBackground()
        }

        // Configurar el clic para cambiar el fondo al de "fondochina"
        imageViewFondoChina.setOnClickListener {
            setBackground(R.drawable.fondochina)
            // Guardar en SharedPreferences
            saveBackgroundPreference("fondochina")
            whiteBackground()
        }

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, ProfileMenuActivity::class.java)
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

    private fun whiteBackground(){
        val imageViewFondoApp: ImageView = findViewById(R.id.imageViewFondoApp)
        val imageViewFondoChina: ImageView = findViewById(R.id.imageViewFondoChina)
        val imageViewFondoBlanco: ImageView = findViewById(R.id.imageViewFondoBlanco)
        val imageViewFondoBlancoChina: ImageView = findViewById(R.id.imageViewFondoBlancoChina)

        // Recuperar las SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val fondoGuardado = prefs.getString("fondo", "fondoapp") // "fondoapp" es el valor por defecto

        // Ajustar el fondo actual con la capa blanca
        when (fondoGuardado) {
            "fondoapp" -> {
                imageViewFondoApp.setImageResource(R.drawable.fondoapp)
                imageViewFondoBlanco.visibility = View.VISIBLE
                imageViewFondoBlancoChina.visibility = View.GONE
            }
            "fondochina" -> {
                imageViewFondoChina.setImageResource(R.drawable.fondochina)
                imageViewFondoBlancoChina.visibility = View.VISIBLE
                imageViewFondoBlanco.visibility = View.GONE
            }
            else -> {
                imageViewFondoApp.setImageResource(R.drawable.fondoapp)
                imageViewFondoBlanco.visibility = View.VISIBLE
                imageViewFondoBlancoChina.visibility = View.GONE
            }
        }

    }

    private fun setBackground(resourceId: Int) {
        // Cambiar el fondo del layout raíz
        val rootLayout: ConstraintLayout = findViewById(R.id.rootLayout)
        rootLayout.setBackgroundResource(resourceId)
    }

    private fun saveBackgroundPreference(fondo: String) {
        // Guardar el fondo seleccionado en las SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("fondo", fondo)
        editor.apply()
    }
}
