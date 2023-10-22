package com.example.calculatortest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CalculatorDatabaseHelper extends SQLiteOpenHelper {
    // 实在懒得写get set方法，直接使用public噜，嘻嘻
    private static final String DATABASE_NAME = "calculator_history.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HISTORY = "calculation_history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_FORMULA = "formula";
    public static final String COLUMN_RESULT = "result";

    public CalculatorDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_FORMULA + " TEXT, " +
                COLUMN_RESULT + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public long insertHistoryRecord(String formula, String result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FORMULA, formula);
        values.put(COLUMN_RESULT, result);
        long newRowId = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return newRowId;
    }
}
