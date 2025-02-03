package com.pmydm.projecterato

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth

class ProfileMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_menu)
        applyBackground()
        setupVolumeButton()
        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonAjustes: Button = findViewById(R.id.buttonAjustes)

        buttonAjustes.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val bundle : Bundle? = intent.extras
        val email : String? = bundle?.getString("email")
        val provider : String? = bundle?.getString("provider")

        setup(email ?: "", provider ?: "")
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
    private fun setup(email: String, provider: String){
        title = "Inicio"

        val buttonCerrarSesion : Button = findViewById(R.id.buttonCerrarSesion)
        buttonCerrarSesion.setOnClickListener {
            val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
            val isVolumeOn = sharedPreferences.getBoolean("volume_state", true)
            val musicServiceIntent = Intent(this, MusicService::class.java)
            if (isVolumeOn) {
                stopService(musicServiceIntent) // Iniciar servicio si el volumen está activado
            }
            FirebaseAuth.getInstance().signOut()
            val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonIdioma : Button = findViewById(R.id.buttonIdioma)
        buttonIdioma.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonFondo : Button = findViewById(R.id.buttonFondo)
        buttonFondo.setOnClickListener {
            val intent = Intent(this, ChangeBackgroundActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}