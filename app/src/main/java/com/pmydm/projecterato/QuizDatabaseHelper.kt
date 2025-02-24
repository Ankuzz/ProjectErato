package com.pmydm.projecterato


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Question(
    val question: String,
    val userAnswer: String?,
    val correct: Boolean
)

class QuizDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "quiz.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        val createGamesTable = """
            CREATE TABLE Games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                date TEXT,
                region TEXT,
                style TEXT,
                mode TEXT
            )
        """
        val createQuestionsTable = """
            CREATE TABLE Questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER,
                question TEXT,
                user_answer TEXT,
                correct INTEGER CHECK(correct IN (0,1)),
                FOREIGN KEY (game_id) REFERENCES Games(id) ON DELETE CASCADE
            )
        """
        val createUsersTable = """
            CREATE TABLE Users (
                user_id TEXT PRIMARY KEY,
                username TEXT UNIQUE,
                bio TEXT,
                region TEXT,
                profile_image_path TEXT
            )
        """
        db.execSQL(createGamesTable)
        db.execSQL(createQuestionsTable)
        db.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si deseas actualizar o eliminar tablas, puedes hacerlo aquí
        db.execSQL("DROP TABLE IF EXISTS Games")
        db.execSQL("DROP TABLE IF EXISTS Questions")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db) // Recrear las tablas
    }

    // Método para borrar la base de datos manualmente
    fun deleteDatabase(context: Context) {
        context.deleteDatabase("quiz.db")
    }
}
