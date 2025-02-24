package com.pmydm.projecterato

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

    private val STORAGE_PERMISSION_CODE = 101
    private val CAMERA_PERMISSION_CODE = 102
    private val STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    private val CAMERA_PERMISSION = Manifest.permission.CAMERA

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

        // Verificar permisos antes de permitir al usuario seleccionar imagen
        if (checkPermission(STORAGE_PERMISSION)) {
            profileImage.setOnClickListener {
                showPictureDialog()
            }
        } else {
            requestPermission(STORAGE_PERMISSION, STORAGE_PERMISSION_CODE)
        }

        if (checkPermission(CAMERA_PERMISSION)) {
            profileImage.setOnClickListener {
                showPictureDialog()
            }
        } else {
            requestPermission(CAMERA_PERMISSION, CAMERA_PERMISSION_CODE)
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

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Permiso requerido")
                .setMessage("Se necesita este permiso para acceder a la cámara o galería.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
                }
                .setNegativeButton("Cancelar") { _, _ -> }
                .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    profileImage.setOnClickListener {
                        showPictureDialog()
                    }
                } else {
                    Toast.makeText(this, "Permiso para acceder al almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    profileImage.setOnClickListener {
                        showPictureDialog()
                    }
                } else {
                    Toast.makeText(this, "Permiso para acceder a la cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
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
        val languageCode = sharedPreferences.getString("language_code", "es") // por defecto en español

        val builder = AlertDialog.Builder(this)

        when (languageCode) {
            "es" -> builder.setTitle("Editar $field")
            "en" -> builder.setTitle("Edit $field")
            "de" -> builder.setTitle("Bearbeiten $field")
            "pl" -> builder.setTitle("Edytuj $field")
            else -> builder.setTitle("Editar $field") // Valor por defecto
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
        val languageCode = sharedPreferences.getString("language_code", "es") // por defecto en español

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
                // En caso de que el language_code no esté en la lista, usar el valor por defecto (español)
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
