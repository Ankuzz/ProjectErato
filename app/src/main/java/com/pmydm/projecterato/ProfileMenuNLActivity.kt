package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ProfileMenuNLActivity : AppCompatActivity(){
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile_menu_nl)

        val imageButtonVolumen: ImageButton = findViewById(R.id.imageButtonVolumen)

        imageButtonVolumen.setOnClickListener {
            if (volumen) {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenapagado)
                volumen=false
            } else {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenencendido)
                volumen=true
            }// Alternar el estado
        }

        val buttonLogin: Button = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val buttonRegister : Button = findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}