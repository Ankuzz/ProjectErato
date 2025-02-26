package com.pmydm.projecterato

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        val languageCode = loadLanguagePreference()
        setAppLocale(languageCode)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        applyBackground()

        setupVolumeButton()

        setup()

        session()


    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun loadLanguagePreference(): String {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        return sharedPreferences.getString("language_code", Locale.getDefault().language) ?: Locale.getDefault().language
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
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmar salida")
        builder.setMessage("¿Estás seguro de que quieres salir?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            finishAffinity()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account=task.getResult(ApiException::class.java)

                if(account!=null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if (it.isSuccessful) {
                            showHome(account.email ?: "", ProviderType.GOOGLE)
                        } else {
                            showAlert()
                        }
                    }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }

    override fun onStart(){
        super.onStart()
        val authLayout: ConstraintLayout = findViewById(R.id.rootLayout)
        authLayout.visibility = View.VISIBLE
    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            val authLayout: ConstraintLayout = findViewById(R.id.rootLayout)
            authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
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

        // Iniciar servicio si el volumen estaba activado
        if (isVolumeOn) {
            val musicServiceIntent = Intent(this, MusicService::class.java)
            startService(musicServiceIntent)
        }

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





    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error en la autenticación del usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){
        val mainIntent: Intent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(mainIntent)
        overridePendingTransition(0, 0)
    }

    private fun setup(){
        title = "Autenticación"

        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextContrasena: EditText = findViewById(R.id.editTextContrasena)

        val buttonIniciarSesion: Button = findViewById(R.id.buttonIniciarSesion)

        buttonIniciarSesion.setOnClickListener {
            if(editTextEmail.text.toString().isNotEmpty() && editTextContrasena.text.toString().isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextEmail.text.toString(), editTextContrasena.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            } else {
                showAlert()
            }
        }

        val imageGoogle: View? = findViewById(R.id.imageGoogle)

        imageGoogle?.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        val buttonRegistro: Button = findViewById(R.id.buttonRegister)

        buttonRegistro.setOnClickListener {
            if(editTextEmail.text.toString().isNotEmpty() && editTextContrasena.text.toString().isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextContrasena.text.toString()).addOnCompleteListener{
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        showAlert()
                    }
                }
            } else {
                showAlert()
            }
        }
    }
}