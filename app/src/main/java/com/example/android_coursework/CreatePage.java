package com.example.android_coursework;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class CreatePage extends AppCompatActivity {

    private EditText etName, etLocation, etLength, etDescription, etCompanions;
    private Spinner spDifficulty, spWeather;
    private RadioGroup rgParking;
    private TextView tvDate;
    private Button btnPickDate, btnPickPhoto, btnSubmit;
    private ImageView ivPhoto;

    private Uri imageUri = null;
    private DatabaseHelper dbHelper;
    private int userId;

    // select image
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();

                    try {

                        // get inputstream from original image in photo
                        InputStream inputStream = getContentResolver().openInputStream(selectedUri);
                        // create local file and store into database and write into this file
                        File file = new File(getFilesDir(), "hike_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream outputStream = new FileOutputStream(file);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        inputStream.close();
                        outputStream.close();

                        // URI thật, trỏ vào file của app
                        imageUri = Uri.fromFile(file);
                        ivPhoto.setImageURI(imageUri);
                        Log.d("CreatePage", "✅ Saved image locally: " + imageUri);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "❌ Failed to copy image", Toast.LENGTH_SHORT).show();
                    }
                }
            });



    // Mở thư viện ảnh (Storage Access Framework)
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_page);

        dbHelper = new DatabaseHelper(this);

        // 🧩 Ánh xạ view
        etName = findViewById(R.id.etHikeName);
        etLocation = findViewById(R.id.etLocation);
        etLength = findViewById(R.id.etLength);
        spDifficulty = findViewById(R.id.spDifficulty);
        rgParking = findViewById(R.id.rgParking);
        etDescription = findViewById(R.id.etDescription);
        spWeather = findViewById(R.id.spWeather);
        etCompanions = findViewById(R.id.etCompanions);
        tvDate = findViewById(R.id.tvDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivPhoto = findViewById(R.id.ivPhoto);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        //  Chọn ngày
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                String selectedDate = d + "/" + (m + 1) + "/" + y;
                tvDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 🖼 Chọn ảnh
        btnPickPhoto.setOnClickListener(v -> openGallery());

        //  Lưu hike
        btnSubmit.setOnClickListener(v -> saveHike());
    }

    private void saveHike() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = tvDate.getText().toString();
        String lengthStr = etLength.getText().toString().trim();
        String difficulty = spDifficulty.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String weather = spWeather.getSelectedItem().toString();
        String companions = etCompanions.getText().toString().trim();

        int selectedParkingId = rgParking.getCheckedRadioButtonId();
        String parking = (selectedParkingId != -1)
                ? ((RadioButton) findViewById(selectedParkingId)).getText().toString()
                : "";

        //  Kiểm tra dữ liệu
        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (location.isEmpty()) { etLocation.setError("Required"); return; }
        if (date.equals("Not selected")) { Toast.makeText(this, "Pick a date", Toast.LENGTH_SHORT).show(); return; }
        if (parking.isEmpty()) { Toast.makeText(this, "Select parking", Toast.LENGTH_SHORT).show(); return; }
        if (lengthStr.isEmpty()) { etLength.setError("Required"); return; }

        double length;
        try {
            length = Double.parseDouble(lengthStr);
        } catch (NumberFormatException e) {
            etLength.setError("Invalid number");
            return;
        }

        //  Thêm vào database
        long result = dbHelper.insertHike(
                name, location, date, parking, length,
                difficulty, description, weather, companions,
                (imageUri != null ? imageUri.toString() : null),
                userId
        );


        if (result != -1) {
            Toast.makeText(this, " Hike created successfully!", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {

            Toast.makeText(this, "Failed to create hike", Toast.LENGTH_SHORT).show();
        }
    }

    // Dọn form sau khi thêm
    private void clearForm() {
        etName.setText("");
        etLocation.setText("");
        etLength.setText("");
        etDescription.setText("");
        spWeather.setSelection(0);
        etCompanions.setText("");
        rgParking.clearCheck();
        tvDate.setText("Not selected");
        spDifficulty.setSelection(0);
        ivPhoto.setImageResource(android.R.color.darker_gray);
        imageUri = null;
    }
}
