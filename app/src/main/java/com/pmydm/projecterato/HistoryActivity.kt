package com.pmydm.projecterato

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

    private fun generatePDF(gameId: Int, mode: String, region: String, style: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val pageHeight = pageInfo.pageHeight

        // Crear la primera página
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Obtener datos del usuario
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.displayName ?: "Usuario Desconocido"
        val bio = getUserBio(user?.uid)

        // Dibujar el nombre de usuario
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(username, 50f, 70f, paint)

        // Dibujar la región
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Región: $region", 50f, 120f, paint)

        // Dibujar la biografía
        paint.textSize = 16f
        canvas.drawText("Biografía: $bio", 50f, 160f, paint)

        // Espacio entre datos del usuario y detalles de la partida
        var yPosition = 220f

        // Dibujar detalles de la partida
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Detalles de la Partida", 50f, yPosition, paint)

        yPosition += 40f
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Modo: $mode", 50f, yPosition, paint)
        yPosition += 40f
        canvas.drawText("Región: $region", 50f, yPosition, paint)
        yPosition += 40f

        // Ajustar el texto del estilo
        val displayStyle = when (style) {
            "infinito" -> "Infinito"
            "alfallo" -> "Al fallo"
            else -> style
        }
        canvas.drawText("Estilo: $displayStyle", 50f, yPosition, paint)
        yPosition += 40f

        // Dibujar tabla con preguntas
        val questions = getQuestionsForGame(gameId)
        yPosition += 20f
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Pregunta", 50f, yPosition, paint)
        canvas.drawText("Respuesta", 250f, yPosition, paint)
        canvas.drawText("Correcta", 400f, yPosition, paint)
        yPosition += 30f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (question in questions) {
            if (yPosition + 30f > pageHeight) {
                // Finalizar la página actual y crear una nueva
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 30f
            }
            canvas.drawText(question.question, 50f, yPosition, paint)
            canvas.drawText(question.userAnswer ?: "Sin respuesta", 250f, yPosition, paint)
            canvas.drawText(if (question.correct) "Sí" else "No", 400f, yPosition, paint)
            yPosition += 30f
        }

        // Finalizar la última página
        pdfDocument.finishPage(page)

        // Guardar PDF en la carpeta Descargas usando MediaStore
        val fileName = "Historial_Partida_$gameId.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        pdfUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
                openPdf(uri) // Llamar a una función para abrir el PDF
            } catch (e: IOException) {
                Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No se pudo crear el archivo PDF", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }


    private fun getUserBio(userId: String?): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT bio FROM Users WHERE user_id = ?", arrayOf(userId))
        cursor.moveToFirst()
        val bio = cursor.getString(0)
        cursor.close()
        return bio
    }

    private fun getQuestionsForGame(gameId: Int): List<Question> {
        val db = dbHelper.readableDatabase
        val questions = mutableListOf<Question>()
        val cursor = db.rawQuery("SELECT question, user_answer, correct FROM Questions WHERE game_id = ?", arrayOf(gameId.toString()))
        while (cursor.moveToNext()) {
            val question = cursor.getString(0)
            val userAnswer = cursor.getString(1)

            // Extraer solo el nombre del archivo sin la extensión
            val fileNameWithExtension = userAnswer.substringAfterLast('/')
            val fileNameWithoutExtension = fileNameWithExtension.substringBeforeLast('.', missingDelimiterValue = fileNameWithExtension)

            val correct = cursor.getInt(2) == 1
            questions.add(Question(question, fileNameWithoutExtension, correct))
        }
        cursor.close()
        return questions
    }

    private fun openPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Permiso de lectura
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No hay una aplicación para abrir PDFs", Toast.LENGTH_SHORT).show()
        }
    }


    private fun applyBackground() {
        val prefs = getSharedPreferences("prefs_file", MODE_PRIVATE)
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
        val username = getUsername()

        val cursor = db.rawQuery(
            "SELECT * FROM Games WHERE username = ? ORDER BY date DESC LIMIT 5",
            arrayOf(username)
        )

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val buttonWidth = (screenWidth * 0.7).toInt()

        while (cursor.moveToNext()) {
            val gameId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val region = cursor.getString(cursor.getColumnIndexOrThrow("region"))
            val mode = cursor.getString(cursor.getColumnIndexOrThrow("mode"))
            val style = cursor.getString(cursor.getColumnIndexOrThrow("style"))
            val aciertos = getAciertosForGame(gameId)

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.HORIZONTAL
            layout.setBackgroundResource(R.drawable.button_background)
            layout.setPadding(10, 10, 10, 10)
            layout.gravity = Gravity.CENTER

// Establecer el tamaño de los botones (ancho fijo y alto ajustable)
            val layoutParams = LinearLayout.LayoutParams(buttonWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
            layout.layoutParams = layoutParams
            layoutParams.height = (100 * resources.displayMetrics.density).toInt()

// Añadir separación vertical entre los botones
            layoutParams.topMargin = (30 * resources.displayMetrics.density).toInt()

            val styleImageView = ImageView(this)
            styleImageView.setImageResource(getStyleImage(style))
            styleImageView.layoutParams = LinearLayout.LayoutParams(150, 150)
            styleImageView.setPadding(60, 0, 10, 20)

            val textContainer = LinearLayout(this)
            textContainer.orientation = LinearLayout.VERTICAL
            textContainer.gravity = Gravity.CENTER_VERTICAL
            textContainer.setPadding(0, -40, 0, 0)

            val buttonText = TextView(this)
            buttonText.text = "$mode - $region"
            buttonText.setTextColor(Color.parseColor("#6F5D47"))
            buttonText.setTypeface(ResourcesCompat.getFont(this, R.font.bungee_regular))
            buttonText.textSize = 20f
            buttonText.setPadding(10, 0, 20, 0)

            val aciertosText = TextView(this)
            aciertosText.text = "Aciertos: $aciertos"
            aciertosText.setTextColor(Color.parseColor("#6F5D47"))
            aciertosText.textSize = 18f
            aciertosText.setPadding(10, 0, 20, 0)

            textContainer.addView(buttonText)
            textContainer.addView(aciertosText)

            layout.addView(styleImageView)
            layout.addView(textContainer)

            layout.setOnClickListener {
                generatePDF(gameId, mode, region, style)
            }

            historyLayout.addView(layout)


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

    private fun getUsername(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        return if (provider == ProviderType.GOOGLE.name) {
            // Obtener el nombre de usuario de Google usando FirebaseAuth
            val user = FirebaseAuth.getInstance().currentUser
            user?.displayName ?: "Nombre de Usuario de Google"
        } else {
            email?.split("@")?.get(0) ?: "Usuario Desconocido"
        }
    }
}
