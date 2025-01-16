package com.pmydm.projecterato

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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

        setupVolumeButton()

        botones()

        session()

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