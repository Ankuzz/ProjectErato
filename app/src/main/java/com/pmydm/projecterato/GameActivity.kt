package com.pmydm.projecterato

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    private lateinit var gameHelper: GameHelper
    private lateinit var imageButtonVolver: ImageButton
    private lateinit var button1: ImageButton
    private lateinit var button2: ImageButton
    private lateinit var button3: ImageButton
    private lateinit var button4: ImageButton
    private lateinit var buttonText: TextView

    // Contadores de aciertos y fallos
    private var aciertos = 0
    private var fallos = 0

    // Variable para almacenar la bandera correcta
    private lateinit var correctFlag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        applyBackground()

        setupVolumeButton()

        // Inicializar los elementos del layout
        imageButtonVolver = findViewById(R.id.imageButtonVolver)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        buttonText = findViewById(R.id.buttonText)

        // Obtener los parámetros del Intent
        val opcion = intent.getStringExtra("Tipo")!!
        val region = intent.getStringExtra("Region")!!

        // Inicializar el objeto GameHelper con los parámetros
        gameHelper = GameHelper(gameType = opcion, region = region, context = this)

        // Configurar el botón de volver
        imageButtonVolver.setOnClickListener {
            // Limpiar el estado del juego antes de volver
            gameHelper.clearGameState()

            // Pasar los parámetros de vuelta a la actividad anterior (Selección de región)
            val intent = Intent(this, MenuRegionSelectionActivity::class.java).apply {
                putExtra("Tipo", opcion) // Pasar el tipo de juego
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish() // Asegurarse de que la actividad actual se cierre
        }

        // Obtener la siguiente pregunta y configurar las banderas
        gameHelper.getNextQuestion()

        buttonText.text = gameHelper.getCountryNameOrCapital()

        // Establecer las banderas en los botones
        val flags = gameHelper.getFlagsList().toMutableList()  // Convertir a lista mutable

// Guardar la bandera correcta antes de hacer el shuffle
        correctFlag = flags[0]

// Hacer el shuffle para que las banderas estén en orden aleatorio
        // Hacer el shuffle para que las banderas estén en orden aleatorio
        flags.shuffle()

// Cargar las imágenes de las banderas desde la carpeta assets/
        button1.setImageBitmap(loadImageFromAssets(flags[0]))
        button2.setImageBitmap(loadImageFromAssets(flags[1]))
        button3.setImageBitmap(loadImageFromAssets(flags[2]))
        button4.setImageBitmap(loadImageFromAssets(flags[3]))

// Asignar contentDescription con el nombre de la bandera o país correspondiente
        button1.contentDescription = flags[0]  // Asignar el contentDescription a la bandera del botón 1
        button2.contentDescription = flags[1]  // Asignar el contentDescription a la bandera del botón 2
        button3.contentDescription = flags[2]  // Asignar el contentDescription a la bandera del botón 3
        button4.contentDescription = flags[3]  // Asignar el contentDescription a la bandera del botón 4


// Lógica de clics en los botones (aquí se verificará si la respuesta es correcta)
        button1.setOnClickListener { checkAnswer(button1) }
        button2.setOnClickListener { checkAnswer(button2) }
        button3.setOnClickListener { checkAnswer(button3) }
        button4.setOnClickListener { checkAnswer(button4) }

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
        // Limpiar el estado del juego antes de volver
        gameHelper.clearGameState()

        // Pasar los parámetros de vuelta a la actividad anterior (Selección de región)
        val opcion = intent.getStringExtra("Tipo")!!
        val intent = Intent(this, MenuRegionSelectionActivity::class.java).apply {
            putExtra("Tipo", opcion) // Pasar el tipo de juego
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish() // Asegurarse de que la actividad actual se cierre
    }


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



    private fun checkAnswer(selectedButton: ImageButton) {
        // Deshabilitar todos los botones después de hacer clic
        disableButtons()

        // Obtener los layouts de los botones
        val buttonLayouts = arrayOf(button1.parent as LinearLayout, button2.parent as LinearLayout, button3.parent as LinearLayout, button4.parent as LinearLayout)

        // Variable para determinar si se acertó
        var isCorrect = false

        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val isVolumeOn = sharedPreferences.getBoolean("volume_state", true)

        // Recorrer los botones y cambiar el fondo
        for (buttonLayout in buttonLayouts) {
            val button = buttonLayout.getChildAt(0) as ImageButton // Obtener el botón dentro del LinearLayout

            if (button.contentDescription == correctFlag) {
                buttonLayout.setBackgroundColor(getColor(R.color.lime_green)) // Cambiar el fondo del layout a verde
                if (button == selectedButton) {
                    aciertos++
                    isCorrect = true // Indicar que se acertó
                }
            } else {
                buttonLayout.setBackgroundColor(getColor(R.color.red)) // Cambiar el fondo del layout a rojo
                if (button == selectedButton) {
                    fallos++ // Incrementar fallos si el botón seleccionado es incorrecto
                }
            }
        }

        // Reproducir sonido si el volumen está activado
        if (isVolumeOn) {
            playSoundEffect(if (isCorrect) R.raw.acierto else R.raw.fallo)
        }

        // Usar una corutina para esperar sin bloquear el hilo principal
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Pausa de 3 segundos

            if (gameHelper.isGameOver()) {
                endGame()
                return@launch
            }

            if(!isCorrect) {
                val buttonState=getButtonState()
                if (buttonState=="alfallo") {
                    endGame()
                    return@launch
                }
            }

            // Restablecer el fondo de todos los layouts a transparente
            for (buttonLayout in buttonLayouts) {
                buttonLayout.setBackgroundColor(getColor(android.R.color.transparent)) // Establecer fondo transparente
            }

            // Restablecer los botones
            button1.setImageResource(0)
            button2.setImageResource(0)
            button3.setImageResource(0)
            button4.setImageResource(0)

            gameHelper.getNextQuestion()
            setupNewQuestion()
            enableButtons()
        }
    }

    private fun getButtonState(): String {
        val prefs: SharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        return prefs.getString("button_state", "infinito") ?: "infinito"
    }

    private fun playSoundEffect(soundResId: Int) {
        val soundEffectPlayer = MediaPlayer.create(this, soundResId)

        // Ajustar el volumen solo para el sonido de fallo
        if (soundResId == R.raw.fallo) {
            soundEffectPlayer.setVolume(2.0f, 2.0f) // Volumen máximo
        } else {
            soundEffectPlayer.setVolume(0.8f, 0.8f) // Volumen normal para aciertos
        }

        soundEffectPlayer.apply {
            setOnCompletionListener {
                release() // Liberar el MediaPlayer al terminar el sonido
            }
            start()
        }
    }

    private fun setupNewQuestion() {
        val flags = gameHelper.getFlagsList().toMutableList()
        correctFlag = flags[0]
        flags.shuffle()

        button1.setImageBitmap(loadImageFromAssets(flags[0]))
        button2.setImageBitmap(loadImageFromAssets(flags[1]))
        button3.setImageBitmap(loadImageFromAssets(flags[2]))
        button4.setImageBitmap(loadImageFromAssets(flags[3]))

        button1.contentDescription = flags[0]
        button2.contentDescription = flags[1]
        button3.contentDescription = flags[2]
        button4.contentDescription = flags[3]

        buttonText.text = gameHelper.getCountryNameOrCapital()
    }




    // Método para cargar imágenes desde la carpeta assets/
    private fun loadImageFromAssets(imagePath: String): Bitmap {
        val assetManager = assets
        val inputStream = assetManager.open(imagePath)
        return BitmapFactory.decodeStream(inputStream)
    }

    // Deshabilitar los botones
    private fun disableButtons() {
        button1.isEnabled = false
        button2.isEnabled = false
        button3.isEnabled = false
        button4.isEnabled = false
    }

    // Habilitar los botones
    private fun enableButtons() {
        button1.isEnabled = true
        button2.isEnabled = true
        button3.isEnabled = true
        button4.isEnabled = true
    }

    // Método para finalizar el juego y pasar los resultados a la ResultsActivity
    private fun endGame() {
        val intent = Intent(this, ResultsActivity::class.java).apply {
            putExtra("aciertos", aciertos)
            putExtra("fallos", fallos)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish() // Termina la actividad actual si es necesario
    }
}
