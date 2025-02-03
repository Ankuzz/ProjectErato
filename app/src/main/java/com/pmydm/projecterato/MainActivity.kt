package com.pmydm.projecterato

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applyBackground()

        setupVolumeButton()

        botones()

        session()

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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar salida")
        builder.setMessage("¿Estás seguro de que quieres salir?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            finishAffinity()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private lateinit var mediaPlayer: MediaPlayer

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




    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email == null && provider == null) {
            prefs()
        }
    }

    private fun prefs(){
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }


    private fun botones(){
        val buttonPaises: Button = findViewById(R.id.buttonPaises)
        buttonPaises.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Paises")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        val buttonCapitales: Button = findViewById(R.id.buttonCapitales)
        buttonCapitales.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Capitales")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonMixto: Button = findViewById(R.id.buttonMixto)
        buttonMixto.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Mixto")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val imageButtonMenu: ImageButton = findViewById(R.id.imageButtonMenu)

        val bundle : Bundle? = intent.extras
        val email : String? = bundle?.getString("email")
        val provider : String? = bundle?.getString("provider")

        imageButtonMenu.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}