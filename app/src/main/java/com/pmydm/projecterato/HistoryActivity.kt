package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: QuizDatabaseHelper
    private lateinit var historyLayout: LinearLayout
    private lateinit var imageButtonVolver: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        applyBackground()
        historyLayout = findViewById(R.id.linearLayoutContenedorBotones)
        dbHelper = QuizDatabaseHelper(this)
        setupVolumeButton()
        imageButtonVolver = findViewById(R.id.imageButtonVolver)
        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        // Cargar las partidas recientes de manera dinámica
        loadRecentGames()
    }

    private fun applyBackground() {
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val fondoGuardado = prefs.getString("fondo", "fondoapp")

        val rootLayout: ConstraintLayout = findViewById(R.id.rootLayout)

        when (fondoGuardado) {
            "fondoapp" -> rootLayout.setBackgroundResource(R.drawable.fondoapp)
            "fondochina" -> rootLayout.setBackgroundResource(R.drawable.fondochina)
            else -> rootLayout.setBackgroundResource(R.drawable.fondoapp)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, ProfileMenuActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun setupVolumeButton() {
        val volumeButton = findViewById<ImageButton>(R.id.imageButtonVolumen)

        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        var isVolumeOn = sharedPreferences.getBoolean("volume_state", true)

        fun updateButtonState(isVolumeOn: Boolean) {
            if (isVolumeOn) {
                volumeButton.setImageResource(R.drawable.iconovolumenencendido)
            } else {
                volumeButton.setImageResource(R.drawable.iconovolumenapagado)
            }
        }

        updateButtonState(isVolumeOn)

        volumeButton.setOnClickListener {
            isVolumeOn = !isVolumeOn
            sharedPreferences.edit().putBoolean("volume_state", isVolumeOn).apply()
            updateButtonState(isVolumeOn)

            val musicServiceIntent = Intent(this, MusicService::class.java)
            if (isVolumeOn) {
                startService(musicServiceIntent)
            } else {
                stopService(musicServiceIntent)
            }
        }
    }

    private fun loadRecentGames() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Games ORDER BY date DESC LIMIT 5", null)

        // Obtener el ancho de la pantalla
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val buttonWidth = (screenWidth * 0.7).toInt() // El 70% del ancho de la pantalla

        val historyLayoutParams = historyLayout.layoutParams
        if (historyLayoutParams is LinearLayout.LayoutParams) {
            historyLayoutParams.setMargins(0, 10, 0, 0) // Ajustar margen superior
            historyLayout.layoutParams = historyLayoutParams
        }

        while (cursor.moveToNext()) {
            val gameId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val region = cursor.getString(cursor.getColumnIndexOrThrow("region"))
            val mode = cursor.getString(cursor.getColumnIndexOrThrow("mode"))
            val style = cursor.getString(cursor.getColumnIndexOrThrow("style"))
            val aciertos = getAciertosForGame(gameId)

            // Crear el LinearLayout principal con fondo
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL
            layout.setBackgroundResource(R.drawable.button_background) // Establecer fondo desde drawable
            layout.setPadding(10, 10, 10, 10) // Ajuste de padding
            layout.gravity = Gravity.CENTER_VERTICAL // Centrar contenido verticalmente

            // Establecer ancho al 70% de la pantalla
            val layoutParams = LinearLayout.LayoutParams(buttonWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
            layout.layoutParams = layoutParams

            // Ajustar la altura del botón (bajo)
            layoutParams.height = (100 * resources.displayMetrics.density).toInt() // Ajustar la altura de manera que sea más baja

            // Centrar el botón horizontalmente en el contenedor principal
            val layoutContainer = LinearLayout(this)
            layoutContainer.orientation = LinearLayout.VERTICAL
            layoutContainer.gravity = Gravity.CENTER_HORIZONTAL // Centrado horizontal

            // Establecer márgenes para el layoutContainer
            val layoutContainerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutContainerParams.setMargins(0, 10, 0, 20) // Reducir los márgenes superiores para moverlo más arriba
            layoutContainer.layoutParams = layoutContainerParams
            layoutContainer.addView(layout)

            // Imagen del estilo del juego con padding a la izquierda y tamaño aumentado
            val styleImageView = ImageView(this)
            styleImageView.setImageResource(getStyleImage(style))
            styleImageView.layoutParams = LinearLayout.LayoutParams(150, 150) // Aumentar tamaño de la imagen
            styleImageView.setPadding(60, 0, 10, 20) // Aumentar padding izquierdo para mover la imagen más a la derecha

            // Contenedor de textos
            val textContainer = LinearLayout(this)
            textContainer.orientation = LinearLayout.VERTICAL
            textContainer.gravity = Gravity.CENTER_VERTICAL // Centrar contenido verticalmente

            // Ajuste de padding en textContainer para moverlo más arriba
            textContainer.setPadding(0, -40, 0, 0) // Reducir padding superior aún más para acercar los textos hacia arriba

            // Texto del botón con padding a la derecha y tamaño aumentado
            val buttonText = TextView(this)
            buttonText.text = "$region - $mode"
            buttonText.setTextColor(Color.parseColor("#6F5D47"))
            buttonText.setTypeface(ResourcesCompat.getFont(this, R.font.bungee_regular))
            buttonText.textSize = 20f // Aumentar tamaño del texto
            buttonText.setPadding(10, 0, 20, 0) // Agregar padding entre el texto y la imagen (derecha)
            val buttonTextParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Cambiar a wrap_content
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonText.layoutParams = buttonTextParams

            // Texto de los aciertos con tamaño más grande
            val aciertosText = TextView(this)
            aciertosText.text = "Aciertos: $aciertos"
            aciertosText.setTextColor(Color.parseColor("#6F5D47"))
            aciertosText.textSize = 18f // Aumentar tamaño del texto de aciertos
            aciertosText.setPadding(10, 0, 20, 0) // Añadir el mismo padding a la derecha para ambos textos
            val aciertosTextParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Cambiar a wrap_content
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            aciertosText.layoutParams = aciertosTextParams

            // Reducir la distancia entre los textos
            aciertosTextParams.topMargin = -50 // Menos margen superior para acercarlo al texto anterior

            // Añadir textos al contenedor
            textContainer.addView(buttonText)
            textContainer.addView(aciertosText)

            // Añadir la imagen y el contenedor de texto al layout
            layout.addView(styleImageView)
            layout.addView(textContainer)

            // Añadir el layout principal al contenedor del historial
            historyLayout.addView(layoutContainer)
        }
        cursor.close()
    }



















    private fun getAciertosForGame(gameId: Int): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(correct) FROM Questions WHERE game_id = ?", arrayOf(gameId.toString()))
        cursor.moveToFirst()
        val aciertos = cursor.getInt(0)
        cursor.close()
        return aciertos
    }

    private fun getStyleImage(style: String): Int {
        return when (style) {
            "infinito" -> R.drawable.infinito
            "alfallo" -> R.drawable.alfallo
            "10" -> R.drawable.diez
            else -> R.drawable.infinito
        }
    }
}