package com.example.android_coursework;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Hiking.db";
    public static final int DATABASE_VERSION = 4;

    // -------------------- USER TABLE -------------------- //
    public static final String TABLE_USER = "User";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_EMAIL = "email";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // -------------------- HIKE TABLE -------------------- //
    public static final String TABLE_HIKE = "Hike";
    public static final String COL_HIKE_ID = "hike_id";
    public static final String COL_HIKE_NAME = "hike_name";
    public static final String COL_LOCATION = "location";
    public static final String COL_DATE = "date";
    public static final String COL_PARKING = "parking";
    public static final String COL_LENGTH = "length";
    public static final String COL_DIFFICULTY = "difficulty";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_WEATHER = "weather";
    public static final String COL_COMPANIONS = "companions";
    public static final String COL_PHOTO_URI = "photo_uri";
    public static final String COL_AUTHOR_ID = "user_id";

    // -------------------- OBSERVATION TABLE -------------------- //
    public static final String TABLE_OBSERVATION = "Observation";
    public static final String COL_OBSERVATION_ID = "observation_id";
    public static final String COL_OBS_HIKE_ID = "hike_id";
    public static final String COL_OBSERVATION_TEXT = "observation";
    public static final String COL_OBSERVATION_TIME = "time";
    public static final String COL_OBSERVATION_COMMENT = "comment";

    // -------------------- COMMENT TABLE -------------------- //
    public static final String TABLE_COMMENT = "Comment";
    public static final String COL_COMMENT_ID = "comment_id";
    public static final String COL_COMMENT_HIKE_ID = "hike_id";
    public static final String COL_COMMENT_USER_ID = "user_id";
    public static final String COL_COMMENT_CONTENT = "content";
    public static final String COL_COMMENT_TIMESTAMP = "timestamp";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USER
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT)");

        // HIKE
        db.execSQL("CREATE TABLE " + TABLE_HIKE + " (" +
                COL_HIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HIKE_NAME + " TEXT NOT NULL, " +
                COL_LOCATION + " TEXT NOT NULL, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_PARKING + " TEXT NOT NULL, " +
                COL_LENGTH + " REAL NOT NULL, " +
                COL_DIFFICULTY + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_WEATHER + " TEXT, " +
                COL_COMPANIONS + " TEXT, " +
                COL_PHOTO_URI + " TEXT, " +
                COL_AUTHOR_ID + " INTEGER, " +
                "FOREIGN KEY(" + COL_AUTHOR_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "))");

        // OBSERVATION
        db.execSQL("CREATE TABLE " + TABLE_OBSERVATION + " (" +
                COL_OBSERVATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OBS_HIKE_ID + " INTEGER, " +
                COL_OBSERVATION_TEXT + " TEXT NOT NULL, " +
                COL_OBSERVATION_TIME + " TEXT NOT NULL, " +
                COL_OBSERVATION_COMMENT + " TEXT, " +
                "FOREIGN KEY(" + COL_OBS_HIKE_ID + ") REFERENCES " + TABLE_HIKE + "(" + COL_HIKE_ID + "))");

        // COMMENT
        db.execSQL("CREATE TABLE " + TABLE_COMMENT + " (" +
                COL_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COMMENT_HIKE_ID + " INTEGER, " +
                COL_COMMENT_USER_ID + " INTEGER, " +
                COL_COMMENT_CONTENT + " TEXT, " +
                COL_COMMENT_TIMESTAMP + " TEXT, " +
                "FOREIGN KEY(" + COL_COMMENT_HIKE_ID + ") REFERENCES " + TABLE_HIKE + "(" + COL_HIKE_ID + "), " +
                "FOREIGN KEY(" + COL_COMMENT_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COL_USER_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // -------------------- USER FUNCTIONS -------------------- //

    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean insertUser(String email, String password, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (checkEmail(email)) return false;

        ContentValues cv = new ContentValues();
        cv.put(COL_EMAIL, email);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_USERNAME, username);
        long result = db.insert(TABLE_USER, null, cv);
        return result != -1;
    }

    public boolean login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE email=? AND password=?", new String[]{email, password});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    public String getUsernameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT username FROM User WHERE email=?", new String[]{email});
        if (cursor.moveToFirst()) {
            String username = cursor.getString(0);
            cursor.close();
            return username;
        }
        cursor.close();
        return null;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM User WHERE email=?", new String[]{email});
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    // -------------------- HIKE CRUD -------------------- //

    public long insertHike(String name, String location, String date, String parking,
                           double length, String difficulty, String description,
                           String weather, String companions, String photoUri, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HIKE_NAME, name);
        cv.put(COL_LOCATION, location);
        cv.put(COL_DATE, date);
        cv.put(COL_PARKING, parking);
        cv.put(COL_LENGTH, length);
        cv.put(COL_DIFFICULTY, difficulty);
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_WEATHER, weather);
        cv.put(COL_COMPANIONS, companions);
        cv.put(COL_PHOTO_URI, photoUri);
        cv.put(COL_AUTHOR_ID, userId);
        long result = db.insert(TABLE_HIKE, null, cv);
        return result;
    }

    public Cursor getHikesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HIKE + " WHERE " + COL_AUTHOR_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public Cursor getAllHikes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HIKE, null);
    }

    // update hike
    public boolean updateHike(int id, String name, String location, String date, String parking,
                              double length, String difficulty, String description,
                              String weather, String companions, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HIKE_NAME, name);
        cv.put(COL_LOCATION, location);
        cv.put(COL_DATE, date);
        cv.put(COL_PARKING, parking);
        cv.put(COL_LENGTH, length);
        cv.put(COL_DIFFICULTY, difficulty);
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_WEATHER, weather);
        cv.put(COL_COMPANIONS, companions);
        cv.put(COL_PHOTO_URI, photoUri);

        Log.d("DB_UPDATE", "Updating Hike id=" + id + " with values: " + cv);

        int rows = db.update(TABLE_HIKE, cv, COL_HIKE_ID + "=?", new String[]{String.valueOf(id)});
        Log.d("DB_UPDATE", "Rows affected: " + rows);

        if (rows == 0) {
            Log.e("DB_UPDATE", "Update failed — maybe hikeId is invalid or no matching row");
        }

        return rows > 0;
    }

    // Delete all hikes by user
    public boolean deleteAllHikesByUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_HIKE, COL_AUTHOR_ID + "=?", new String[]{String.valueOf(userId)});
        Log.d("DB_DELETE", "Deleted " + rows + " hikes for userId=" + userId);
        return rows > 0;
    }

    public boolean deleteHike(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_HIKE, COL_HIKE_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
    // 🔍 Tìm kiếm hike theo tên, địa điểm, hoặc độ khó
    public Cursor searchHikes(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_HIKE +
                " WHERE " + COL_HIKE_NAME + " LIKE ? " +
                " OR " + COL_LOCATION + " LIKE ? " +
                " OR " + COL_DIFFICULTY + " LIKE ? " +
                " ORDER BY " + COL_DATE + " DESC";

        String likeKeyword = "%" + keyword + "%";
        return db.rawQuery(query, new String[]{likeKeyword, likeKeyword, likeKeyword});
    }
    public Cursor searchHikesByUser(String keyword, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("DB_SEARCH_USER", "🔍 Query = " + userId);
        String likeKeyword = "%" + keyword + "%";

        String query = "SELECT * FROM " + TABLE_HIKE +
                " WHERE (" + COL_HIKE_NAME + " LIKE ? " +
                " OR " + COL_LOCATION + " LIKE ? " +
                " OR " + COL_DIFFICULTY + " LIKE ?) " +
                " AND " + COL_AUTHOR_ID + " = ? " +
                " ORDER BY " + COL_DATE + " DESC";

        // 🧩 Log truy vấn và tham số
        Log.d("DB_SEARCH_USER", "🔍 Query = " + query);
        Log.d("DB_SEARCH_USER", "🔍 Params = [" + likeKeyword + ", " + likeKeyword + ", " + likeKeyword + ", userId=" + userId + "]");

        Cursor cursor = db.rawQuery(query, new String[]{
                likeKeyword, likeKeyword, likeKeyword, String.valueOf(userId)
        });

        // 🧠 Log kết quả
        if (cursor != null) {
            Log.d("DB_SEARCH_USER", "✅ Rows found for userId=" + userId + " → " + cursor.getCount());
        } else {
            Log.e("DB_SEARCH_USER", "❌ Cursor is null for userId=" + userId);
        }

        return cursor;
    }



    public Cursor getHikeById(int hikeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HIKE + " WHERE " + COL_HIKE_ID + "=?",
                new String[]{String.valueOf(hikeId)});
    }
    // -------------------- OBSERVATION CRUD -------------------- //

    public long insertObservation(int hikeId, String observation, String time, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OBS_HIKE_ID, hikeId);
        cv.put(COL_OBSERVATION_TEXT, observation);
        cv.put(COL_OBSERVATION_TIME, time);
        cv.put(COL_OBSERVATION_COMMENT, comment);
        return db.insert(TABLE_OBSERVATION, null, cv);
    }

    public Cursor getObservationsByHike(int hikeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_OBSERVATION + " WHERE " + COL_OBS_HIKE_ID + "=?",
                new String[]{String.valueOf(hikeId)});
    }

    public boolean updateObservation(int id, String observation, String time, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OBSERVATION_TEXT, observation);
        cv.put(COL_OBSERVATION_TIME, time);
        cv.put(COL_OBSERVATION_COMMENT, comment);
        int rows = db.update(TABLE_OBSERVATION, cv, COL_OBSERVATION_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteObservation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_OBSERVATION, COL_OBSERVATION_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // -------------------- COMMENT CRUD -------------------- //

    public long insertComment(int hikeId, int userId, String content, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_COMMENT_HIKE_ID, hikeId);
        cv.put(COL_COMMENT_USER_ID, userId);
        cv.put(COL_COMMENT_CONTENT, content);
        cv.put(COL_COMMENT_TIMESTAMP, timestamp);
        return db.insert(TABLE_COMMENT, null, cv);
    }

    public Cursor getCommentsByHike(int hikeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT c.*, u.username FROM " + TABLE_COMMENT + " c " +
                        "LEFT JOIN " + TABLE_USER + " u ON c.user_id = u.user_id " +
                        "WHERE c.hike_id = ? ORDER BY c.comment_id DESC",
                new String[]{String.valueOf(hikeId)});
    }

    public boolean deleteComment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_COMMENT, COL_COMMENT_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
    // 📝 Update comment
    public boolean updateComment(int id, String newContent, String newTimestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_COMMENT_CONTENT, newContent);
        cv.put(COL_COMMENT_TIMESTAMP, newTimestamp);

        int rows = db.update(TABLE_COMMENT, cv, COL_COMMENT_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

}
