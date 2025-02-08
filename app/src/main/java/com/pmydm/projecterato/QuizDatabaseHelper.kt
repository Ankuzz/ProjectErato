package com.pmydm.projecterato

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Question(
    val question: String,
    val userAnswer: String?,
    val correct: Boolean
)

class QuizDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "quiz.db", null, 2) { // Cambié la versión de 1 a 2

    override fun onCreate(db: SQLiteDatabase) {
        val createGamesTable = """
            CREATE TABLE Games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT,
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
                correct INTEGER,
                FOREIGN KEY (game_id) REFERENCES Games(id)
            )
        """
        db.execSQL(createGamesTable)
        db.execSQL(createQuestionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE Games ADD COLUMN style TEXT")
            db.execSQL("ALTER TABLE Games ADD COLUMN mode TEXT")
        }
    }
}

