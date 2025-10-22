package com.example.android_coursework;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_hikes);

        dbHelper = new DatabaseHelper(this);
        allHikesRecycler = findViewById(R.id.allHikesRecycler);
        allHikesRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        hikeList = new ArrayList<>();
        adapter = new HikeAdapter(this, hikeList);
        allHikesRecycler.setAdapter(adapter);

        // 🔍 Ánh xạ UI
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnFilterLength = findViewById(R.id.btnFilterLength);
        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnFilterParking = findViewById(R.id.btnFilterParking);
        spFilterDifficulty = findViewById(R.id.spFilterDifficulty);

        // 🔎 Search
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) loadAllHikes();
            else searchHikes(keyword);
        });

        // ⚙️ Filter buttons
        btnFilterLength.setOnClickListener(v -> toggleFilter("length"));
        btnFilterDate.setOnClickListener(v -> toggleFilter("date"));
        btnFilterParking.setOnClickListener(v -> toggleFilter("parking"));

        // 💪 Spinner filter for Difficulty
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

        // 🔹 Load lần đầu
        loadAllHikes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllHikes();
    }

    // 🔍 Search hikes
    private void searchHikes(String keyword) {
        hikeList.clear();
        Cursor cursor = dbHelper.searchHikes(keyword);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                hikeList.add(extractHikeFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "🔎 Found hikes: " + hikeList.size());
    }

    // 📏 Sort functions
    private void sortHikesByLength() {
        Collections.sort(hikeList, Comparator.comparingDouble(HikeModel::getLength));
        adapter.notifyDataSetChanged();
    }

    private void sortHikesByDate() {
        Collections.sort(hikeList, (h1, h2) -> h2.getDate().compareTo(h1.getDate()));
        adapter.notifyDataSetChanged();
    }

    // 🚗 Filter Parking = Yes
    private void filterByParking() {
        hikeList.clear();
        Cursor cursor = dbHelper.getAllHikes();
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
        Log.d("AllHikesActivity", "🚗 Filtered by parking (Yes): " + hikeList.size());
    }

    // 💪 Filter by Difficulty (single Spinner)
    private void filterBySingleDifficulty(String level) {
        hikeList.clear();
        Cursor cursor = dbHelper.getAllHikes();

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
        Log.d("AllHikesActivity", "💪 Filtered by difficulty: " + level + " → " + hikeList.size());
    }

    // 🧩 Load tất cả hikes
    private void loadAllHikes() {
        hikeList.clear();
        Cursor cursor = dbHelper.getAllHikes();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                hikeList.add(extractHikeFromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
        Log.d("AllHikesActivity", "📍 Reloaded hikes: " + hikeList.size());
    }

    // 🧱 Helper - trích dữ liệu từ cursor
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

    // ⚙️ Toggle filters + highlight button
    private void toggleFilter(String type) {
        // Reset style
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
