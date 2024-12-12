package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MenuRegionSelectionActivity : AppCompatActivity() {
    private var volumen=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_region_selection)
        val opcion = intent.getStringExtra("Tipo")


        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)

        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
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

        val buttonEuropa: Button = findViewById(R.id.buttonEuropa)
        buttonEuropa.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Europa")
            startActivity(intent)
        }

        val buttonAsia: Button = findViewById(R.id.buttonAsia)
        buttonAsia.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Asia")
            startActivity(intent)
        }

        val buttonAfrica: Button = findViewById(R.id.buttonAfrica)
        buttonAfrica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Africa")
            startActivity(intent)
        }

        val buttonAmerica: Button = findViewById(R.id.buttonAmerica)
        buttonAmerica.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "America")
            startActivity(intent)
        }

        val buttonOceania: Button = findViewById(R.id.buttonOceania)
        buttonOceania.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("Tipo", opcion)
            intent.putExtra("Region", "Oceania")
            startActivity(intent)
        }
    }
}