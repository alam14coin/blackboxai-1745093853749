package com.example.quizapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "quiz.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_QUESTIONS = "questions";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_QUESTION = "question";
    private static final String COLUMN_OPTION1 = "option1";
    private static final String COLUMN_OPTION2 = "option2";
    private static final String COLUMN_OPTION3 = "option3";
    private static final String COLUMN_OPTION4 = "option4";
    private static final String COLUMN_ANSWER = "answer";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CATEGORY + " INTEGER,"
                    + COLUMN_QUESTION + " TEXT NOT NULL,"
                    + COLUMN_OPTION1 + " TEXT NOT NULL,"
                    + COLUMN_OPTION2 + " TEXT NOT NULL,"
                    + COLUMN_OPTION3 + " TEXT NOT NULL,"
                    + COLUMN_OPTION4 + " TEXT NOT NULL,"
                    + COLUMN_ANSWER + " INTEGER NOT NULL"
                    + ")";
            db.execSQL(CREATE_QUESTIONS_TABLE);

            // Insert sample questions
            insertSampleQuestions(db);
            Log.i(TAG, "Database and sample questions created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage());
        }
    }

    private void insertSampleQuestions(SQLiteDatabase db) {
        // Math Questions (Category 1)
        insertQuestion(db, 1, "What is 2 + 2?", "3", "4", "5", "6", 1);
        insertQuestion(db, 1, "What is 5 * 3?", "15", "10", "20", "25", 0);
        insertQuestion(db, 1, "What is 100 ÷ 4?", "20", "25", "15", "30", 1);
        insertQuestion(db, 1, "What is 7 * 8?", "54", "56", "58", "60", 1);
        insertQuestion(db, 1, "What is 15 - 7?", "6", "7", "8", "9", 2);

        // Science Questions (Category 2)
        insertQuestion(db, 2, "What planet is known as the Red Planet?", "Earth", "Mars", "Jupiter", "Saturn", 1);
        insertQuestion(db, 2, "What gas do plants absorb?", "Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen", 2);
        insertQuestion(db, 2, "What is the largest organ in the human body?", "Heart", "Brain", "Liver", "Skin", 3);
        insertQuestion(db, 2, "What is the hardest natural substance on Earth?", "Gold", "Iron", "Diamond", "Platinum", 2);
        insertQuestion(db, 2, "Which planet is closest to the Sun?", "Mercury", "Venus", "Earth", "Mars", 0);

        // General Knowledge (Category 3)
        insertQuestion(db, 3, "Which country is home to the kangaroo?", "New Zealand", "South Africa", "Australia", "India", 2);
        insertQuestion(db, 3, "What is the capital of Japan?", "Seoul", "Beijing", "Shanghai", "Tokyo", 3);
        insertQuestion(db, 3, "Who painted the Mona Lisa?", "Leonardo da Vinci", "Pablo Picasso", "Vincent van Gogh", "Michelangelo", 0);
        insertQuestion(db, 3, "What is the largest ocean on Earth?", "Atlantic", "Indian", "Arctic", "Pacific", 3);
        insertQuestion(db, 3, "Which is the longest river in the world?", "Amazon", "Nile", "Mississippi", "Yangtze", 1);
    }

    private void insertQuestion(SQLiteDatabase db, int category, String question, String opt1, String opt2, String opt3, String opt4, int answer) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_QUESTION, question);
            values.put(COLUMN_OPTION1, opt1);
            values.put(COLUMN_OPTION2, opt2);
            values.put(COLUMN_OPTION3, opt3);
            values.put(COLUMN_OPTION4, opt4);
            values.put(COLUMN_ANSWER, answer);
            
            long result = db.insert(TABLE_QUESTIONS, null, values);
            if (result == -1) {
                Log.e(TAG, "Error inserting question: " + question);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting question: " + e.getMessage());
        }
    }

    public List<Question> getQuestionsByCategory(int category) {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_QUESTIONS,
                    null,
                    COLUMN_CATEGORY + "=?",
                    new String[]{String.valueOf(category)},
                    null, null, "RANDOM()"); // Randomize questions order

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String questionText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUESTION));
                    String option1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION1));
                    String option2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION2));
                    String option3 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION3));
                    String option4 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPTION4));
                    int answer = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANSWER));

                    questions.add(new Question(questionText, new String[]{option1, option2, option3, option4}, answer));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting questions: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (questions.isEmpty()) {
            Log.w(TAG, "No questions found for category: " + category);
        }

        return questions;
    }

    public int getCategoryQuestionCount(int category) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_QUESTIONS + 
                               " WHERE " + COLUMN_CATEGORY + "=?", 
                               new String[]{String.valueOf(category)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting question count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        }
    }
}
