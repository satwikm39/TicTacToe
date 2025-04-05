package com.example.tictactoe.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TicTacToeDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tictactoe.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "game_results"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_WINNER = "winner"
        private const val COLUMN_MODE = "mode"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT,
                $COLUMN_WINNER TEXT,
                $COLUMN_MODE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertGameResult(date: String, winner: String, mode: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_WINNER, winner)
            put(COLUMN_MODE, mode)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllGameResults(): List<GameResult> {
        val gameResults = mutableListOf<GameResult>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_DATE DESC"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
            val winner = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WINNER))
            val difficulty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODE))
            gameResults.add(GameResult(date, winner, difficulty))
        }

        cursor.close()
        db.close()
        return gameResults
    }
}
