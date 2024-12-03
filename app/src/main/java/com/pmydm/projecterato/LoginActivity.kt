package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, ProfileMenuNLActivity::class.java)
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
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
        }

    }
}