package com.pmydm.projecterato

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.contentValuesOf
import com.google.firebase.auth.FirebaseAuth


enum class ProviderType {
    BASIC,
    GOOGLE
}

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: QuizDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        dbHelper = QuizDatabaseHelper(this)

        applyBackground()

        setupVolumeButton()

        botones()

        session()

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
        val languageCode = getSharedPreferences("prefs_file", Context.MODE_PRIVATE)
            .getString("language_code", "en") // Obtén el idioma de las preferencias (por defecto "en")

        val title: String
        val message: String
        val positiveButtonText: String
        val negativeButtonText: String

        // Configuración de los textos según el idioma
        when (languageCode) {
            "es" -> {
                title = "Confirmar salida"
                message = "¿Estás seguro de que quieres salir?"
                positiveButtonText = "Sí"
                negativeButtonText = "No"
            }
            "de" -> {
                title = "Bestätigen Sie den Ausgang"
                message = "Sind Sie sicher, dass Sie die App verlassen möchten?"
                positiveButtonText = "Ja"
                negativeButtonText = "Nein"
            }
            "pl" -> {
                title = "Potwierdź wyjście"
                message = "Jesteś pewny, że chcesz wyjść?"
                positiveButtonText = "Tak"
                negativeButtonText = "Nie"
            }
            else -> { // Default to English
                title = "Confirm Exit"
                message = "Are you sure you want to exit?"
                positiveButtonText = "Yes"
                negativeButtonText = "No"
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            finishAffinity()
        }

        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
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




    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        val user = auth.currentUser
        val userId = user?.uid ?: return

        // Si el email o provider son nulos, o si el provider ha cambiado (sesión diferente), actualizamos los datos
        val newProvider = if (user?.providerData?.any { it.providerId == "google.com" } == true) ProviderType.GOOGLE.name else null
        val newEmail = user?.email

        // Si el provider o email cambian, actualizamos la base de datos y SharedPreferences
        if (email != newEmail || provider != newProvider) {
            val username = if (newProvider == ProviderType.GOOGLE.name) {
                user?.displayName ?: "Nombre de Usuario de Google"
            } else {
                newEmail?.split("@")?.getOrNull(0) ?: "Usuario Desconocido"
            }

            val profileImageUrl = if (newProvider == ProviderType.GOOGLE.name) {
                user?.photoUrl?.toString() ?: "perfil_default"
            } else {
                "perfil_default"
            }

            // Actualizamos SharedPreferences solo si es un usuario completamente nuevo
            if (email == null || provider == null) {
                with(prefs.edit()) {
                    putString("email", newEmail)
                    putString("provider", newProvider)
                    apply()
                }
            }

            // Actualizamos la base de datos con la nueva información si el usuario es completamente nuevo
            val db = dbHelper.writableDatabase
            val cursor = db.rawQuery("SELECT user_id, profile_image_path FROM Users WHERE user_id = ?", arrayOf(userId))

            if (!cursor.moveToFirst()) {
                // Insertamos el nuevo usuario si no existe
                val values = ContentValues().apply {
                    put("user_id", userId)
                    put("username", username)
                    put("bio", "")
                    put("region", "")
                    put("profile_image_path", profileImageUrl)
                }
                db.insert("Users", null, values)
                Log.d("ProfileActivity", "New user inserted: $userId")
            } else {
                // Si el usuario existe, no actualizamos la foto de perfil
                val storedProfileImage = cursor.getString(cursor.getColumnIndex("profile_image_path"))

                if (storedProfileImage == "perfil_default" && newProvider == ProviderType.GOOGLE.name) {
                    // Solo actualizamos la foto si la foto está en "perfil_default" (es decir, si no está definida)
                    val values = ContentValues().apply {
                        put("username", username)
                        put("profile_image_path", profileImageUrl)
                    }
                    db.update("Users", values, "user_id = ?", arrayOf(userId))
                    Log.d("ProfileActivity", "User updated: $userId with profile image")
                } else {
                    // No actualizamos nada si la foto ya está configurada
                    Log.d("ProfileActivity", "User already exists, no profile update: $userId")
                }
            }

            cursor.close()
        }
    }





    private fun botones(){
        val buttonPaises: Button = findViewById(R.id.buttonPaises)
        buttonPaises.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Paises")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        val buttonCapitales: Button = findViewById(R.id.buttonCapitales)
        buttonCapitales.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Capitales")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val buttonMixto: Button = findViewById(R.id.buttonMixto)
        buttonMixto.setOnClickListener {
            val intent = Intent(this, MenuRegionSelectionActivity::class.java)
            intent.putExtra("Tipo", "Mixto")
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val imageButtonMenu: ImageButton = findViewById(R.id.imageButtonMenu)

        val bundle : Bundle? = intent.extras
        val email : String? = bundle?.getString("email")
        val provider : String? = bundle?.getString("provider")

        imageButtonMenu.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}