package com.example.android_coursework;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllHikesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView allHikesRecycler;
    private HikeAdapter adapter;
    private List<HikeModel> hikeList;
    private Button btnFilterLength, btnFilterDate, btnFilterParking;
    private Spinner spFilterDifficulty;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actity_all_hike_users);

        dbHelper = new DatabaseHelper(this);
        allHikesRecycler = findViewById(R.id.allHikesRecycler);
        allHikesRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        hikeList = new ArrayList<>();
        adapter = new HikeAdapter(this, hikeList);
        allHikesRecycler.setAdapter(adapter);

        // Get current userId
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        Log.d("AllHikesActivity", "👤 Current userId = " + userId);

        // UI mapping
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnFilterLength = findViewById(R.id.btnFilterLength);
        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnFilterParking = findViewById(R.id.btnFilterParking);
        spFilterDifficulty = findViewById(R.id.spFilterDifficulty);
        Button btnDeleteAll = findViewById(R.id.btnDeleteAll);

        // 🗑 Delete all hikes for user
        btnDeleteAll.setOnClickListener(v -> {
            if (userId == -1) {
                Log.e("DELETE_ALL", "⚠️ User not logged in");
                return;
            }

            // Toast confirm
            new androidx.appcompat.app.AlertDialog.Builder(AllHikesActivity.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete all your hikes? This action cannot be undone.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteAllHikesByUser(userId);
                        if (deleted) {
                            Toast.makeText(AllHikesActivity.this, "✅ All hikes deleted!", Toast.LENGTH_SHORT).show();
                            loadAllHikes();
                        } else {
                            Toast.makeText(AllHikesActivity.this, "⚠️ No hikes found to delete.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        Toast.makeText(AllHikesActivity.this, "❎ Deletion cancelled.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });


        // Search hikes (theo user)
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) loadAllHikes();
            else searchHikes(keyword);
        });

        // Filter buttons
        btnFilterLength.setOnClickListener(v -> toggleFilter("length"));
        btnFilterDate.setOnClickListener(v -> toggleFilter("date"));
        btnFilterParking.setOnClickListener(v -> toggleFilter("parking"));

        // Spinner filter Difficulty (theo user)
        spFilterDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equalsIgnoreCase("All")) {
                    loadAllHikes();
                } else {
                    filterBySingleDifficulty(selected);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Load first
        loadAllHikes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllHikes();
    }

    //Search hikes theo user
    private void searchHikes(String keyword) {
        hikeList.clear();
        Cursor cursor = dbHelper.searchHikesByUser(keyword, userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                hikeList.add(extractHikeFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "🔎 Found " + hikeList.size() + " hikes for userId=" + userId);
    }

    //  Sort
    private void sortHikesByLength() {
        Collections.sort(hikeList, Comparator.comparingDouble(HikeModel::getLength));
        adapter.notifyDataSetChanged();
    }

    private void sortHikesByDate() {
        Collections.sort(hikeList, (h1, h2) -> h2.getDate().compareTo(h1.getDate()));
        adapter.notifyDataSetChanged();
    }

    //  Filter Parking = Yes (theo user)
    private void filterByParking() {
        hikeList.clear();
        Cursor cursor = dbHelper.getHikesByUser(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String parking = cursor.getString(cursor.getColumnIndexOrThrow("parking"));
                if ("Yes".equalsIgnoreCase(parking.trim())) {
                    hikeList.add(extractHikeFromCursor(cursor));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "🚗 Filtered by parking (Yes): " + hikeList.size() + " hikes for userId=" + userId);
    }

    //  Filter by Difficulty (theo user)
    private void filterBySingleDifficulty(String level) {
        hikeList.clear();
        Cursor cursor = dbHelper.getHikesByUser(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
                if (difficulty.equalsIgnoreCase(level)) {
                    hikeList.add(extractHikeFromCursor(cursor));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "💪 Filtered by difficulty (" + level + ") → " + hikeList.size());
    }

    //  Load tất cả hikes của user
    private void loadAllHikes() {
        hikeList.clear();
        Cursor cursor = dbHelper.getHikesByUser(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                hikeList.add(extractHikeFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "📍 Reloaded hikes for userId=" + userId + ": " + hikeList.size());
    }

    //  Extract model
    private HikeModel extractHikeFromCursor(Cursor cursor) {
        return new HikeModel(
                cursor.getInt(cursor.getColumnIndexOrThrow("hike_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("hike_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("parking")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("length")),
                cursor.getString(cursor.getColumnIndexOrThrow("difficulty")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("weather")),
                cursor.getString(cursor.getColumnIndexOrThrow("companions")),
                cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"))
        );
    }

    //  Toggle filter + highlight
    private void toggleFilter(String type) {
        resetButtonStyle(btnFilterLength, android.R.color.holo_green_dark, false);
        resetButtonStyle(btnFilterDate, android.R.color.holo_orange_dark, false);
        resetButtonStyle(btnFilterParking, android.R.color.holo_blue_dark, false);

        switch (type) {
            case "length":
                sortHikesByLength();
                resetButtonStyle(btnFilterLength, android.R.color.holo_green_light, true);
                break;
            case "date":
                sortHikesByDate();
                resetButtonStyle(btnFilterDate, android.R.color.holo_orange_light, true);
                break;
            case "parking":
                filterByParking();
                resetButtonStyle(btnFilterParking, android.R.color.holo_blue_light, true);
                break;
        }
    }

    private void resetButtonStyle(Button button, int color, boolean active) {
        button.setBackgroundTintList(getColorStateList(color));
        button.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }
}
