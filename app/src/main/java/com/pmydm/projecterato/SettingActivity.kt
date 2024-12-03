package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val buttonCerrarSesion : Button = findViewById(R.id.buttonCerrarSesion)

        buttonCerrarSesion.setOnClickListener {
            val intent = Intent(this, ProfileMenuNLActivity::class.java)
            MainActivity.setLogin()
            startActivity(intent)
        }
    }
}