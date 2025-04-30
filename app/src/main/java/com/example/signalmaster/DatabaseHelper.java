package com.example.signalmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "signalmaster.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER = "user_data";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_LETTER_SCORE = "letter_score";
    public static final String COLUMN_WORD_SCORE = "word_score";
    public static final String COLUMN_LINE_SCORE = "line_score";

    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +  // prevent duplicates
                    COLUMN_LETTER_SCORE + " INTEGER DEFAULT 0, " +
                    COLUMN_WORD_SCORE + " INTEGER DEFAULT 0, " +
                    COLUMN_LINE_SCORE + " INTEGER DEFAULT 0);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public boolean addUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);

        long result = db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();

        return result != -1;  // true if inserted, false if already existed
    }

    private int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_USER +
                " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return id;
    }

    public int getScore(String username, String mode) {
        String column = getColumnForMode(mode);
        if (column == null) return 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + column + " FROM " + TABLE_USER +
                " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        int score = 0;
        if (cursor.moveToFirst()) {
            score = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return score;
    }

    public void updateHighScore(String username, String mode, int newScore) {
        String column = getColumnForMode(mode);
        if (column == null) return;

        int currentScore = getScore(username, mode);
        if (newScore > currentScore) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("UPDATE " + TABLE_USER + " SET " + column + " = ? WHERE " + COLUMN_USERNAME + " = ?",
                    new Object[]{newScore, username});
            db.close();
        }
    }

    private String getColumnForMode(String mode) {
        switch (mode.toLowerCase()) {
            case "letter": return COLUMN_LETTER_SCORE;
            case "word": return COLUMN_WORD_SCORE;
            case "line": return COLUMN_LINE_SCORE;
            default: return null;
        }
    }

}

