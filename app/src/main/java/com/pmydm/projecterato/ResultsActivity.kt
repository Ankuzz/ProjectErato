package com.pmydm.projecterato

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        applyBackground()
        setupVolumeButton()

        // Obtener referencias a los TextView
        val numeroAciertos: TextView = findViewById(R.id.numeroAciertos)
        val numeroFallos: TextView = findViewById(R.id.numeroFallos)

        // Obtener los datos pasados por Intent
        val aciertos = intent.getIntExtra("aciertos", 0)
        val fallos = intent.getIntExtra("fallos", 0)
        val region = intent.getStringExtra("region") ?: ""
        val mode = intent.getStringExtra("tipo") ?: ""

        // Establecer los valores en los TextView
        numeroAciertos.text = aciertos.toString()
        numeroFallos.text = fallos.toString()

        // Obtener las preguntas enviadas desde la GameActivity
        val questionsData = intent.getStringArrayListExtra("questions")

        // Si las preguntas no son nulas, procesarlas
        if (questionsData != null) {
            val questionsList = questionsData.map {
                val parts = it.split(",")
                val questionText = parts[0]
                val userAnswer = parts[1]
                val correct = parts[2].toBoolean()
                Question(questionText, userAnswer, correct)
            }

            // Obtener el style desde SharedPreferences
            val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
            val style = sharedPreferences.getString("button_state", "default_style") ?: "default_style"

            // Obtener la fecha actual
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Obtener el UID desde Firebase (asumiendo que estás usando FirebaseAuth)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default_uid"

            // Guardar los resultados en la base de datos
            saveResultsToDatabase(questionsList, aciertos, fallos, style, currentDate, region, mode, uid)
        }

        // Botón para volver al menú principal
        val buttonMenu: Button = findViewById(R.id.buttonMenu)
        buttonMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun saveResultsToDatabase(
        questions: List<Question>,
        aciertos: Int,
        fallos: Int,
        style: String,
        date: String,
        region: String,
        mode: String,
        uid: String
    ) {
        val dbHelper = QuizDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        // Insertar los datos generales del juego en la tabla Games
        val gameValues = ContentValues().apply {
            put("user_id", uid)
            put("date", date)
            put("region", region)
            put("style", style)
            put("mode", mode)
        }

        val gameId = db.insert("Games", null, gameValues) // Insertamos los datos generales y obtenemos el ID

        // Insertar cada pregunta relacionada con el resultado
        questions.forEach { question ->
            val questionValues = ContentValues().apply {
                put("game_id", gameId) // Relacionamos la pregunta con el resultado
                put("question", question.question)
                put("user_answer", question.userAnswer)
                put("correct", if (question.correct) 1 else 0) // 1 para verdadero, 0 para falso
            }
            db.insert("Questions", null, questionValues) // Insertamos la pregunta en la base de datos
        }

        // Limitar el número de partidas a las últimas 5
        limitGamesToFive(db)

        db.close() // Cerramos la base de datos
    }

    private fun limitGamesToFive(db: SQLiteDatabase) {
        // Verificar cuántas partidas hay en la tabla Games
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Games", null)
        cursor.moveToFirst()
        val gameCount = cursor.getInt(0)
        cursor.close()

        // Si hay más de 5 partidas, eliminamos la más antigua
        if (gameCount > 5) {
            // Eliminar la partida con el id más bajo (la más antigua)
            db.execSQL("DELETE FROM Games WHERE id = (SELECT MIN(id) FROM Games)")
        }
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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
