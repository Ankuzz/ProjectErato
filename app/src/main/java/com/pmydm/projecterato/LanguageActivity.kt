package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val languageCode = loadLanguagePreference()
        setAppLocale(languageCode)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        applyBackground()

        setupVolumeButton()

        setupLanguageButtons()

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }


    private fun setupLanguageButtons() {
        val buttonSpanish = findViewById<Button>(R.id.buttonEspanol)
        val buttonEnglish = findViewById<Button>(R.id.buttonIngles)
        val buttonDeutsch = findViewById<Button>(R.id.buttonDeutsch)
        val buttonPolaco = findViewById<Button>(R.id.buttonPolaco)

        buttonSpanish.setOnClickListener {
            saveLanguagePreference("es")
            setAppLocale("es")
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        buttonEnglish.setOnClickListener {
            saveLanguagePreference("en")
            setAppLocale("en")
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        buttonDeutsch.setOnClickListener {
            saveLanguagePreference("de")
            setAppLocale("de")
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        buttonPolaco.setOnClickListener {
            saveLanguagePreference("pl")
            setAppLocale("pl")
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }


    private fun saveLanguagePreference(languageCode: String) {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        sharedPreferences.edit().putString("language_code", languageCode).apply()
    }

    private fun loadLanguagePreference(): String {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        return sharedPreferences.getString("language_code", Locale.getDefault().language) ?: Locale.getDefault().language
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
        val intent = Intent(this, ProfileMenuActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
