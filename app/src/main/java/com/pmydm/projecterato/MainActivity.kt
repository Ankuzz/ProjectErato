package com.pmydm.projecterato

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isVolumeOn = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageButtonVolumen: ImageButton = findViewById(R.id.imageButtonVolumen)

        imageButtonVolumen.setOnClickListener {
            if (isVolumeOn) {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenapagado)
            } else {
                imageButtonVolumen.setImageResource(R.drawable.iconovolumenencendido)
            }
            isVolumeOn = !isVolumeOn  // Alternar el estado
        }
        val imageButtonMenu: ImageButton = findViewById(R.id.imageButtonMenu)

        imageButtonMenu.setOnClickListener {
            val intent = Intent(this, SettingsNLActivity::class.java)
            startActivity(intent)
        }
    }
}