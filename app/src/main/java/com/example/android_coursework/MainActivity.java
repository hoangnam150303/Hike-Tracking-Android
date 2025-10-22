package com.example.android_coursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CardView loginCard;
    private TextView textView2;
    private DatabaseHelper dbHelper;
    private RecyclerView hikeRecycler, allHikesRecycler;
    private HikeAdapter userAdapter, allAdapter;
    private List<HikeModel> userHikes, allHikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // this will help the content will display full screen
        setContentView(R.layout.activity_main); // connect MainActivity with XML

        dbHelper = new DatabaseHelper(this); // connect to database

        // Setup RecyclerView for Your Hikes
        hikeRecycler = findViewById(R.id.hikeSlider); // mapping recyclerView in xml with id is hikeSlider to hikeRecycler
        // use LinearLayoutManager to display horizontal
        hikeRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Create new Array
        userHikes = new ArrayList<>();
        // create adapter to manage and display the list of userHikes
        userAdapter = new HikeAdapter(this, userHikes);
        // set adapter to hikeRecycler
        hikeRecycler.setAdapter(userAdapter);
        // Animation when scroll each item
        new PagerSnapHelper().attachToRecyclerView(hikeRecycler);

        // Setup RecyclerView for All Hikes, this session is the same way with Your Hikes
        allHikesRecycler = findViewById(R.id.allHikesSlider);
        allHikesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        allHikes = new ArrayList<>();
        allAdapter = new HikeAdapter(this, allHikes);
        allHikesRecycler.setAdapter(allAdapter);
        new PagerSnapHelper().attachToRecyclerView(allHikesRecycler);

        // hero slider
        // I use library denzcoskun ImageSlider, I add it in build.gradle
        ImageSlider imageSlider = findViewById(R.id.imageSlider); // mapping ImageSlider with id imageSlider in XML
        ArrayList<SlideModel> slideModels = new ArrayList<>(); // create an array
        slideModels.add(new SlideModel(R.drawable.hero1, ScaleTypes.FIT)); // add image into array, image is stored at res/drawable
        slideModels.add(new SlideModel(R.drawable.hero2, ScaleTypes.FIT)); // ScaleTypes.FIT help the image will display fit with screen
        slideModels.add(new SlideModel(R.drawable.hero3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.hero4, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // Login / Logout setup
        setupUserUI(); // This function is implemented below

        // See More AllHikesActivity, find the id seeMore in XML and paginate user to see more page when onclick
        findViewById(R.id.seeMore).setOnClickListener(v ->
                // paginate from MainActivity to AllHikesActivity
                startActivity(new Intent(this, AllHikesActivity.class))
        );
        findViewById(R.id.seeMoreAll).setOnClickListener(v ->
                // The logic of this code is the same way with AllHikesActivity
                startActivity(new Intent(this, AllHikesUserActivity.class))
        );
        // Click button checkin to CreatePage
        findViewById(R.id.checkInButton).setOnClickListener(v ->
                startActivity(new Intent(this, CreatePage.class))
        );

        // load data of user hikes and all hikes
        loadUserHikes();
        loadAllHikes();
    }

    // Reload data again after return the page
    @Override
    protected void onResume() {
        super.onResume(); // this function onResume is different with onResume I create,
        // this function is inherited from AppCompatActivity
        loadUserHikes(); // run function loadUserHikes
        loadAllHikes(); // run function loadAllHikes
    }

    // data of user
    private void loadUserHikes() {
        userHikes.clear(); // when user go back to mainactivity, userHikes will clear old data in array to make sure don't duplicate data

        // get user's information from SharedPreferences
        // SharePreferences store small data like userId or isLoggedIn
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false); // if isLoggedIn, button login will replace to logout

        if (isLoggedIn && userId != -1) { // if user login and userId is not equal -1, it will query to database
            Cursor cursor = dbHelper.getHikesByUser(userId); // cursor is pointer help browse through query result get from database
            if (cursor != null && cursor.moveToFirst()) { // if true go to next logic
                do { // get id, hikeName, location, length, date, parking, difficulty, dcription, weather, comapnions, photoUri from database
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("hike_id"));
                    String hikeName = cursor.getString(cursor.getColumnIndexOrThrow("hike_name"));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                    double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String parking = cursor.getString(cursor.getColumnIndexOrThrow("parking"));
                    String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String weather = cursor.getString(cursor.getColumnIndexOrThrow("weather"));
                    String companions = cursor.getString(cursor.getColumnIndexOrThrow("companions"));
                    String photoUri = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"));
                    // add all data above to userHikes array
                    userHikes.add(new HikeModel(id, hikeName, location, date, parking, length,
                            difficulty, description, weather, companions, photoUri));
                } while (cursor.moveToNext()); // go to next row
                cursor.close(); // after complete add data to array, cursor will close
            }
        }

        userAdapter.notifyDataSetChanged(); // notificate to RecyclerView that the data has changed
    }

    //  data of all hikes, this logic is the same way with userHikes
    private void loadAllHikes() {
        allHikes.clear();
        Cursor cursor = dbHelper.getAllHikes();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("hike_id"));
                String hikeName = cursor.getString(cursor.getColumnIndexOrThrow("hike_name"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                double length = cursor.getDouble(cursor.getColumnIndexOrThrow("length"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String parking = cursor.getString(cursor.getColumnIndexOrThrow("parking"));
                String difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String weather = cursor.getString(cursor.getColumnIndexOrThrow("weather"));
                String companions = cursor.getString(cursor.getColumnIndexOrThrow("companions"));
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"));

                allHikes.add(new HikeModel(id, hikeName, location, date, parking, length,
                        difficulty, description, weather, companions, photoUri));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (allHikes.isEmpty()) {
            allHikes.add(new HikeModel(null, "No hikes available", 0));
        }

        allAdapter.notifyDataSetChanged();
    }

    // This function is use for update UI
    private void setupUserUI() {
        loginCard = findViewById(R.id.loginCard);
        CardView logoutCard = findViewById(R.id.logoutCard);
        TextView logoutButton = findViewById(R.id.logoutButton);
        textView2 = findViewById(R.id.textView2);

        // get isLoggedIn and username from sharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String username = prefs.getString("username", null);

        if (isLoggedIn && username != null) {
            loginCard.setVisibility(View.GONE); //if user login, login button will disable
            logoutCard.setVisibility(View.VISIBLE); // after login disable, logout button will appear
            textView2.setText("Hello " + username + " 👋"); // get username in prefs and set slogan to hello + username

            logoutButton.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit(); // open edit mode
                editor.clear(); // clear all data in prefs
                editor.apply(); // save change
                Intent intent = new Intent(this, MainActivity.class); // create intent to paginate to MainActivity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // delete old stack activity
                startActivity(intent);// open MainActivity
                finish();
            });
        } else {
            loginCard.setVisibility(View.VISIBLE); // display login button
            logoutCard.setVisibility(View.GONE); // disappear logout button
            textView2.setText("Take a hike,\nfind yourself"); // display slogan

            findViewById(R.id.loginButton).setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)) // paginate to login page when user click button login
            );
        }
    }
}
