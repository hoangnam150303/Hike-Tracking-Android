package com.example.android_coursework;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class UpdateHikeActivity extends AppCompatActivity {

    private ImageView ivHikeImage;
    private EditText etTitle, etLocation, etDate, etParking, etLength, etDifficulty, etWeather, etCompanions, etDescription;
    private EditText etObservation, etComment;
    private Button btnChooseImage, btnUpdateHike, btnAddObservation;
    private Uri selectedImageUri;
    private DatabaseHelper dbHelper;
    private int hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_hike);
        // connect database
        dbHelper = new DatabaseHelper(this);
        // mapping
        ivHikeImage = findViewById(R.id.ivHikeImage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etParking = findViewById(R.id.etParking);
        etLength = findViewById(R.id.etLength);
        etDifficulty = findViewById(R.id.etDifficulty);
        etWeather = findViewById(R.id.etWeather);
        etCompanions = findViewById(R.id.etCompanions);
        etDescription = findViewById(R.id.etDescription);
        etObservation = findViewById(R.id.etObservation);
        etComment = findViewById(R.id.etComment);
        btnUpdateHike = findViewById(R.id.btnUpdateHike);
        btnAddObservation = findViewById(R.id.btnAddObservation);

        // get data from detail page
        Intent intent = getIntent();
        if (intent != null) {
            hikeId = intent.getIntExtra("hike_id", -1);
            etTitle.setText(intent.getStringExtra("title"));
            etLocation.setText(intent.getStringExtra("location"));
            etDate.setText(intent.getStringExtra("date"));
            etParking.setText(intent.getStringExtra("parking"));
            etLength.setText(String.valueOf(intent.getDoubleExtra("length", 0)));
            etDifficulty.setText(intent.getStringExtra("difficulty"));
            etWeather.setText(intent.getStringExtra("weather"));
            etCompanions.setText(intent.getStringExtra("companions"));
            etDescription.setText(intent.getStringExtra("description"));

            String imageUri = intent.getStringExtra("image_uri");
            if (imageUri != null && !imageUri.isEmpty()) {
                selectedImageUri = Uri.parse(imageUri);
                Glide.with(this).load(selectedImageUri).into(ivHikeImage);
            }
        }

        // select new image
        btnChooseImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setType("image/*");
            startActivityForResult(pick, 100);
        });

        // update hike
        btnUpdateHike.setOnClickListener(v -> {
            Log.d("UpdateHike", "🔹 Trying to update hike id=" + hikeId);
            Log.d("UpdateHike", "Title=" + etTitle.getText());
            Log.d("UpdateHike", "Location=" + etLocation.getText());
            Log.d("UpdateHike", "Date=" + etDate.getText());
            Log.d("UpdateHike", "Parking=" + etParking.getText());
            Log.d("UpdateHike", "Length=" + etLength.getText());
            Log.d("UpdateHike", "Difficulty=" + etDifficulty.getText());
            Log.d("UpdateHike", "Weather=" + etWeather.getText());
            Log.d("UpdateHike", "Companions=" + etCompanions.getText());
            Log.d("UpdateHike", "Description=" + etDescription.getText());
            Log.d("UpdateHike", "Image=" + selectedImageUri);

            // get all data from form
            boolean updated = dbHelper.updateHike( // call updateHike in dbHelper to update data
                    hikeId,
                    etTitle.getText().toString(),
                    etLocation.getText().toString(),
                    etDate.getText().toString(),
                    etParking.getText().toString(),
                    Double.parseDouble(etLength.getText().toString()),
                    etDifficulty.getText().toString(),
                    etDescription.getText().toString(),
                    etWeather.getText().toString(),
                    etCompanions.getText().toString(),
                    selectedImageUri != null ? selectedImageUri.toString() : ""
            );
            // if success, display toast
            if (updated) {
                Toast.makeText(this, "Hike updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // if failed, display toat
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });


        // Add observation
        btnAddObservation.setOnClickListener(v -> {
            // get observation from UI
            String observation = etObservation.getText().toString();
            String comment = etComment.getText().toString();
            // set time now
            String time = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            long result = dbHelper.insertObservation(hikeId, observation, time, comment);
            if (result != -1) {
                Toast.makeText(this, "Observation saved", Toast.LENGTH_SHORT).show();
                etObservation.setText("");
                etComment.setText("");
            } else {
                Toast.makeText(this, "Failed to save observation", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivHikeImage.setImageURI(selectedImageUri);
        }
    }
}
