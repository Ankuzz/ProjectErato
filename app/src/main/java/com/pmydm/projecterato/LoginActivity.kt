package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, SettingsNLActivity::class.java)
            startActivity(intent)
        }

        val imageButtonVolumen: ImageButton = findViewById(R.id.imageButtonVolumen)

        imageButtonVolumen.setOnClickListener {
            if (volumen) {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenapagado)
                volumen=false
            } else {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenencendido)
                volumen=true
            }
            // Alternar el estado
        }

        val buttonIniciarSesion: Button = findViewById(R.id.buttonIniciarSesion)

        buttonIniciarSesion.setOnClickListener {
            MainActivity.setLogin()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
}