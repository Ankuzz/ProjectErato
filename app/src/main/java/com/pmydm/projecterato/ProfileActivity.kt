package com.pmydm.projecterato

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: QuizDatabaseHelper
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImage: ImageView
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    private lateinit var profileName: TextView
    private lateinit var profileBio: TextView
    private lateinit var profileRegion: TextView
    private lateinit var addBioButton: Button
    private lateinit var addRegionButton: Button

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        applyBackground()
        setupVolumeButton()

        profileImage = findViewById(R.id.profileImage)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        profileName = findViewById(R.id.profileName)
        profileBio = findViewById(R.id.profileBio)
        profileRegion = findViewById(R.id.profileRegion)
        addBioButton = findViewById(R.id.addBioButton)
        addRegionButton = findViewById(R.id.addRegionButton)

        profileImage.setOnClickListener {
            if (allPermissionsGranted()) {
                showPictureDialog()
            } else {
                askForPermissions(REQUIRED_PERMISSIONS)
            }
        }

        val imageButtonVolver: ImageButton = findViewById(R.id.imageButtonVolver)
        imageButtonVolver.setOnClickListener {
            val intent = Intent(this, ProfileMenuActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        dbHelper = QuizDatabaseHelper(this)

        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            loadProfileData(userId)
        }

        addBioButton.setOnClickListener {
            showEditDialog("bio") { newBio ->
                val values = ContentValues().apply {
                    put("bio", newBio)
                }
                val db = dbHelper.writableDatabase
                db.update("Users", values, "user_id = ?", arrayOf(user?.uid))
                profileBio.text = newBio
            }
        }

        addRegionButton.setOnClickListener {
            showEditDialog("region") { newRegion ->
                val values = ContentValues().apply {
                    put("region", newRegion)
                }
                val db = dbHelper.writableDatabase
                db.update("Users", values, "user_id = ?", arrayOf(user?.uid))
                profileRegion.text = newRegion
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun executeDialogForNegativePermission(isRationale: Boolean, callback: () -> Unit) {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("language_code", "es")

        Log.d("Permissions", if (isRationale) "Mostrando diálogo de racionalización." else "Redirigiendo a configuración de la aplicación.")
        MaterialAlertDialogBuilder(this)
            .setTitle(
                when (languageCode) {
                    "es" -> "ACEPTE LOS PERMISOS"
                    "en" -> "ACCEPT THE PERMISSIONS"
                    "de" -> "AKZEPTIEREN SIE DIE BERECHTIGUNGEN"
                    "pl" -> "AKCEPTUJ UPRAWNIENIA"
                    else -> "ACEPTE LOS PERMISOS"
                }
            )
            .setMessage(
                when (languageCode) {
                    "es" -> if (isRationale) "Se necesita permiso para acceder a la cámara." else "Por favor, habilite el permiso desde la configuración de la aplicación."
                    "en" -> if (isRationale) "Permission is needed to access the camera." else "Please enable the permission from the application settings."
                    "de" -> if (isRationale) "Berechtigung wird benötigt, um auf die Kamera zuzugreifen." else "Bitte aktivieren Sie die Berechtigung in den Anwendungseinstellungen."
                    "pl" -> if (isRationale) "Wymagana jest zgoda na dostęp do kamery." else "Proszę włączyć uprawnienie w ustawieniach aplikacji."
                    else -> if (isRationale) "Se necesita permiso para acceder a la cámara." else "Por favor, habilite el permiso desde la configuración de la aplicación."
                }
            )
            .setPositiveButton(
                when (languageCode) {
                    "es" -> "Aceptar"
                    "en" -> "Accept"
                    "de" -> "Akzeptieren"
                    "pl" -> "Akceptuj"
                    else -> "Aceptar"
                }
            ) { dialog, _ ->
                callback.invoke()
                if (!isRationale) {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null),
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                dialog.dismiss()
            }
            .show()
    }


    private fun askForPermissions(REQUIRED_PERMISSIONS: Array<String>) {
        val permissionToAsk = mutableListOf<String>()

        REQUIRED_PERMISSIONS.forEach { permission ->
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Permissions", "Permiso $permission ya concedido.")
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Log.d("Permissions", "Permiso $permission denegado, pero se puede volver a pedir.")
                    permissionToAsk.add(permission)
                }
                else -> {
                    Log.d("Permissions", "Permiso $permission no ha sido concedido ni denegado previamente.")
                    permissionToAsk.add(permission)
                }
            }
        }

        if (permissionToAsk.isNotEmpty()) {
            Log.d("Permissions", "Solicitando permisos: ${permissionToAsk.joinToString()}")
            requestPermissionLauncher.launch(permissionToAsk.toTypedArray())
        } else {
            Log.d("Permissions", "No hay permisos para pedir, mostrando diálogo para ir a configuración.")
            executeDialogForNegativePermission(false) {
                // Aquí puedes agregar lógica adicional si es necesario
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { isGranted: Map<String, Boolean>? ->
            if (isGranted == null) {
                Log.e("Permissions", "Resultado de permisos es null.")
                return@registerForActivityResult
            }

            val permissionToAsk = mutableListOf<String>()

            Log.d("Permissions", "Resultado de permisos: $isGranted")

            if (isGranted.values.contains(false)) {
                REQUIRED_PERMISSIONS.forEach { permission ->
                    val isPermissionGranted = isGranted[permission]
                    if (isPermissionGranted == false && shouldShowRequestPermissionRationale(permission)) {
                        permissionToAsk.add(permission)
                    }
                }
                if (permissionToAsk.isNotEmpty()) {
                    executeDialogForNegativePermission(true) {
                        askForPermissions(permissionToAsk.toTypedArray())
                    }
                } else {
                    executeDialogForNegativePermission(false) {
                        // Aquí puedes agregar lógica adicional si es necesario
                    }
                }
            } else {
                showPictureDialog()
            }
        }

    private fun loadProfileData(userId: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Users", null, "user_id = ?", arrayOf(userId), null, null, null)
        if (cursor.moveToFirst()) {
            val bioIndex = cursor.getColumnIndex("bio")
            val regionIndex = cursor.getColumnIndex("region")
            val usernameIndex = cursor.getColumnIndex("username")
            val imagePathIndex = cursor.getColumnIndex("profile_image_path")

            if (bioIndex >= 0) {
                val bio = cursor.getString(bioIndex)
                profileBio.text = bio
            }
            if (regionIndex >= 0) {
                val region = cursor.getString(regionIndex)
                profileRegion.text = region
            }
            if (usernameIndex >= 0) {
                val username = cursor.getString(usernameIndex)
                profileName.text = username
            }
            if (imagePathIndex >= 0) {
                val imagePath = cursor.getString(imagePathIndex)
                if (imagePath != "perfil_default" && imagePath.isNotEmpty()) {
                    Glide.with(this).load(imagePath).into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.perfil_default)
                }
            }
        }
        cursor.close()
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

    private fun showEditDialog(field: String, onTextEntered: (String) -> Unit) {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("language_code", "es")

        val builder = AlertDialog.Builder(this)

        when (languageCode) {
            "es" -> builder.setTitle("Editar $field")
            "en" -> builder.setTitle("Edit $field")
            "de" -> builder.setTitle("Bearbeiten $field")
            "pl" -> builder.setTitle("Edytuj $field")
            else -> builder.setTitle("Editar $field")
        }

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        when (languageCode) {
            "es" -> builder.setPositiveButton("Guardar") { _, _ ->
                val newText = input.text.toString()
                onTextEntered(newText)
            }
            "en" -> builder.setPositiveButton("Save") { _, _ ->
                val newText = input.text.toString()
                onTextEntered(newText)
            }
            "de" -> builder.setPositiveButton("Speichern") { _, _ ->
                val newText = input.text.toString()
                onTextEntered(newText)
            }
            "pl" -> builder.setPositiveButton("Zapisz") { _, _ ->
                val newText = input.text.toString()
                onTextEntered(newText)
            }
            else -> builder.setPositiveButton("Guardar") { _, _ ->
                val newText = input.text.toString()
                onTextEntered(newText)
            }
        }

        when (languageCode) {
            "es" -> builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            "en" -> builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            "de" -> builder.setNegativeButton("Abbrechen") { dialog, _ -> dialog.cancel() }
            "pl" -> builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
            else -> builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        }

        builder.show()
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

    private fun showPictureDialog() {
        val sharedPreferences = getSharedPreferences("prefs_file", MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("language_code", "es")

        val pictureDialog = AlertDialog.Builder(this)

        when (languageCode) {
            "es" -> {
                pictureDialog.setTitle("Seleccionar acción")
                val pictureDialogItems = arrayOf("Seleccionar foto desde la galería", "Capturar foto desde la cámara")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
            }
            "en" -> {
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
            }
            "de" -> {
                pictureDialog.setTitle("Aktion auswählen")
                val pictureDialogItems = arrayOf("Foto aus der Galerie auswählen", "Foto mit der Kamera aufnehmen")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
            }
            "pl" -> {
                pictureDialog.setTitle("Wybierz akcję")
                val pictureDialogItems = arrayOf("Wybierz zdjęcie z galerii", "Zrób zdjęcie aparatem")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
            }
            else -> {
                pictureDialog.setTitle("Seleccionar acción")
                val pictureDialogItems = arrayOf("Seleccionar foto desde la galería", "Capturar foto desde la cámara")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openCamera()
                    }
                }
            }
        }
        pictureDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                val selectedImageUri = data?.data
                val selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                profileImage.setImageBitmap(selectedImageBitmap)

                val filePath = saveImageToStorage(selectedImageBitmap)
                saveImageToDatabase(filePath)
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                val photo = data?.extras?.get("data") as Bitmap
                profileImage.setImageBitmap(photo)

                val filePath = saveImageToStorage(photo)
                saveImageToDatabase(filePath)
            }
        }
    }

    private fun saveImageToStorage(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val file = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfileImages")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file)

        uri?.let {
            contentResolver.openOutputStream(it)?.write(byteArray)
        }

        return uri.toString()
    }

    private fun saveImageToDatabase(imagePath: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("profile_image_path", imagePath)
        }
        db.update("Users", values, "user_id = ?", arrayOf(auth.currentUser?.uid))
    }
}
