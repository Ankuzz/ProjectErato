package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private var login = false
        fun setLogin() {
            login = !login
        }

        fun isLoggedIn(): Boolean {
            return login
        }
    }
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageButtonVolumen: ImageButton = findViewById(R.id.imageButtonVolumen)

        imageButtonVolumen.setOnClickListener {
            if (volumen) {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenapagado)
                volumen=false
            } else {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenencendido)
                volumen=true
            }  // Alternar el estado
        }
        val imageButtonMenu: ImageButton = findViewById(R.id.imageButtonMenu)

        imageButtonMenu.setOnClickListener {
            if (isLoggedIn()) {
                val intent = Intent(this, ProfileMenuActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, ProfileMenuNLActivity::class.java)
                startActivity(intent)
            }
        }

        val buttonBanderas: Button = findViewById(R.id.buttonBanderas)
        buttonBanderas.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Banderas")
            startActivity(intent)
        }

        val buttonPaises: Button = findViewById(R.id.buttonPaises)
        buttonPaises.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Paises")
            startActivity(intent)
        }

        val buttonCapitales: Button = findViewById(R.id.buttonCapitales)
        buttonCapitales.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Capitales")
            startActivity(intent)
        }
    }
}