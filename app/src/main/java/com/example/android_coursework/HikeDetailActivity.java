package com.example.android_coursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class HikeDetailActivity extends AppCompatActivity {

    private ImageView ivHikeImage;
    private TextView tvHikeName, tvLocation, tvDate, tvParking, tvLength,
            tvDifficulty, tvWeather, tvCompanions, tvDescription;
    private Button btnUpdate, btnDelete;
    private int hikeId, authorId;
    private String imageUri;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);
        // connect database
        dbHelper = new DatabaseHelper(this);

        // mapping
        ivHikeImage = findViewById(R.id.ivHikeImage);
        tvHikeName = findViewById(R.id.tvHikeName);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        tvParking = findViewById(R.id.tvParking);
        tvLength = findViewById(R.id.tvLength);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvWeather = findViewById(R.id.tvWeather);
        tvCompanions = findViewById(R.id.tvCompanions);
        tvDescription = findViewById(R.id.tvDescription);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        // get data from intent, where I config in HikeAdapter
        Intent intent = getIntent();
        if (intent != null) { // if intent is not null,set data
            hikeId = intent.getIntExtra("hike_id", -1);
            authorId = intent.getIntExtra("user_id", -1);
            imageUri = intent.getStringExtra("image_uri");
            String name = intent.getStringExtra("hike_name");
            String location = intent.getStringExtra("location");
            String date = intent.getStringExtra("date");
            String parking = intent.getStringExtra("parking");
            double length = intent.getDoubleExtra("length", 0);
            String difficulty = intent.getStringExtra("difficulty");
            String weather = intent.getStringExtra("weather");
            String companions = intent.getStringExtra("companions");
            String description = intent.getStringExtra("description");

            // Display image
            if (imageUri != null && !imageUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(imageUri))
                        .centerCrop()
                        .placeholder(R.drawable.hero1)
                        .error(R.drawable.hero2)
                        .into(ivHikeImage);
            } else {
                ivHikeImage.setImageResource(R.drawable.hero1);
            }

            // display data
            tvHikeName.setText(name);
            tvLocation.setText(location);
            tvDate.setText(date);
            tvParking.setText(parking);
            tvLength.setText(String.format("%.1f km", length));
            tvDifficulty.setText(difficulty);
            tvWeather.setText(weather);
            tvCompanions.setText(companions);
            tvDescription.setText(description);
        }


        Log.d("DEBUG", "currentUserId=" + userId + ", authorId=" + authorId);

        // check author id and hikeId is exist
        if (authorId == -1 && hikeId != -1) {
            Cursor cursor = dbHelper.getHikeById(hikeId);
            if (cursor != null && cursor.moveToFirst()) {
                authorId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                cursor.close();
                Log.d("DEBUG", "authorId fetched from DB = " + authorId);
            } else {
                Log.d("DEBUG", "No hike found for hikeId=" + hikeId);
            }
        }

        // check if user is not authorId, disable edit button and delete button
        if (userId != authorId) {

            Log.d("DEBUG_AUTH", "Not Author. userId=" + userId + ", authorId=" + authorId);
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        } else {
            Log.d("DEBUG_AUTH", "Is Author. userId=" + userId + ", authorId=" + authorId);
            btnUpdate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        }



        // Update, send all data to update page
        btnUpdate.setOnClickListener(v -> {
            Intent updateIntent = new Intent(this, UpdateHikeActivity.class);
            updateIntent.putExtra("hike_id", hikeId);
            updateIntent.putExtra("image_uri", imageUri);
            updateIntent.putExtra("title", tvHikeName.getText().toString());
            updateIntent.putExtra("location", tvLocation.getText().toString());
            updateIntent.putExtra("date", tvDate.getText().toString());
            updateIntent.putExtra("parking", tvParking.getText().toString());
            updateIntent.putExtra("length", parseLength(tvLength.getText().toString()));
            updateIntent.putExtra("difficulty", tvDifficulty.getText().toString());
            updateIntent.putExtra("weather", tvWeather.getText().toString());
            updateIntent.putExtra("companions", tvCompanions.getText().toString());
            updateIntent.putExtra("description", tvDescription.getText().toString());
            startActivityForResult(updateIntent, 2001);
        });

        // Delete
        btnDelete.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteHike(hikeId);
            if (deleted) {
                Toast.makeText(this, "Deleted successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete hike", Toast.LENGTH_SHORT).show();
            }
        });

        // Display Observations and Comments
        loadObservations();
        loadComments();

        // add new comment
        EditText etComment = findViewById(R.id.etComment);
        Button btnSendComment = findViewById(R.id.btnSendComment);

        btnSendComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Please enter a comment!", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            long result = dbHelper.insertComment(hikeId, userId, commentText, time);

            if (result > 0) {
                Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show();
                etComment.setText("");
                loadComments(); // refresh
            } else {
                Toast.makeText(this, "Failed to add comment.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // convert to double
    private double parseLength(String text) {
        try {
            return Double.parseDouble(text.replace(" km", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // Load list observation
    private void loadObservations() {
        LinearLayout container = findViewById(R.id.observationContainer); // mapping
        container.removeAllViews();
        Cursor cursor = dbHelper.getObservationsByHike(hikeId);

        if (cursor != null && cursor.moveToFirst()) { // check data is null
            do {
                int observationId = cursor.getInt(cursor.getColumnIndexOrThrow("observation_id"));
                String observation = cursor.getString(cursor.getColumnIndexOrThrow("observation"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));

                // display layout of observation
                LinearLayout observationLayout = new LinearLayout(this);
                observationLayout.setOrientation(LinearLayout.VERTICAL);
                observationLayout.setPadding(8, 8, 8, 8);
                observationLayout.setBackgroundColor(0xFFF5F5F5);
                observationLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // display text observation
                TextView tvObs = new TextView(this);
                tvObs.setText("• " + observation + " (" + time + ")\n💬 " + comment);
                tvObs.setTextSize(15);
                tvObs.setPadding(0, 8, 0, 8);

                observationLayout.addView(tvObs);

                // if user author, edit observation
                if (userId == authorId) {
                    LinearLayout btnLayout = new LinearLayout(this);
                    btnLayout.setOrientation(LinearLayout.HORIZONTAL);

                    Button btnEdit = new Button(this);
                    btnEdit.setText("✏️ Edit");
                    btnEdit.setTextSize(12);
                    btnEdit.setPadding(10, 0, 10, 0);

                    Button btnDeleteObs = new Button(this);
                    btnDeleteObs.setText("🗑 Delete");
                    btnDeleteObs.setTextSize(12);
                    btnDeleteObs.setPadding(10, 0, 10, 0);

                    btnLayout.addView(btnEdit);
                    btnLayout.addView(btnDeleteObs);
                    observationLayout.addView(btnLayout);

                    // update button
                    btnEdit.setOnClickListener(v -> {
                        showUpdateObservationDialog(observationId, observation, comment);
                    });

                    // Delete button
                    btnDeleteObs.setOnClickListener(v -> {
                        boolean deleted = dbHelper.deleteObservation(observationId);
                        if (deleted) {
                            Toast.makeText(this, "Observation deleted!", Toast.LENGTH_SHORT).show();
                            loadObservations();
                        } else {
                            Toast.makeText(this, "Failed to delete observation", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                container.addView(observationLayout);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TextView none = new TextView(this);
            none.setText("No observations yet.");
            container.addView(none);
        }
    }
    // Display dialog to accept update observation in detail page
    private void showUpdateObservationDialog(int observationId, String oldObservation, String oldComment) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Observation");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText etObs = new EditText(this);
        etObs.setHint("Observation");
        etObs.setText(oldObservation);
        layout.addView(etObs);

        EditText etComment = new EditText(this);
        etComment.setHint("Comment");
        etComment.setText(oldComment);
        layout.addView(etComment);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newObs = etObs.getText().toString().trim();
            String newComment = etComment.getText().toString().trim();
            String newTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            if (newObs.isEmpty()) {
                Toast.makeText(this, "⚠Observation cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateObservation(observationId, newObs, newTime, newComment);
            if (updated) {
                Toast.makeText(this, "Observation updated!", Toast.LENGTH_SHORT).show();
                loadObservations();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    // Load list of comment
    private void loadComments() {
        LinearLayout commentContainer = findViewById(R.id.commentContainer);
        commentContainer.removeAllViews();
        Cursor cursor = dbHelper.getCommentsByHike(hikeId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int commentId = cursor.getInt(cursor.getColumnIndexOrThrow("comment_id"));
                int commentUserId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));

                // set name of author
                String displayName = (commentUserId == userId) ? "You" : (username != null ? username : "User " + commentUserId);

                // layout of each comment
                LinearLayout commentLayout = new LinearLayout(this);
                commentLayout.setOrientation(LinearLayout.VERTICAL);
                commentLayout.setPadding(8, 8, 8, 8);
                commentLayout.setBackgroundColor(0xFFF5F5F5);
                commentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                //Display content of comment
                TextView tvComment = new TextView(this);
                tvComment.setText("👤 " + displayName + ": " + comment + "\n🕒 " + time);
                tvComment.setTextSize(15);
                tvComment.setPadding(0, 8, 0, 8);
                commentLayout.addView(tvComment);

                // if author display Edit button and delete button
                if (commentUserId == userId) {
                    LinearLayout btnLayout = new LinearLayout(this);
                    btnLayout.setOrientation(LinearLayout.HORIZONTAL);

                    Button btnEdit = new Button(this);
                    btnEdit.setText("✏️ Edit");
                    btnEdit.setTextSize(12);
                    btnEdit.setPadding(10, 0, 10, 0);

                    Button btnDelete = new Button(this);
                    btnDelete.setText("🗑 Delete");
                    btnDelete.setTextSize(12);
                    btnDelete.setPadding(10, 0, 10, 0);

                    btnLayout.addView(btnEdit);
                    btnLayout.addView(btnDelete);
                    commentLayout.addView(btnLayout);

                    // Edit button event
                    btnEdit.setOnClickListener(v -> showUpdateCommentDialog(commentId, comment));

                    // delete button event
                    btnDelete.setOnClickListener(v -> {
                        boolean deleted = dbHelper.deleteComment(commentId);
                        if (deleted) {
                            Toast.makeText(this, "Comment deleted!", Toast.LENGTH_SHORT).show();
                            loadComments();
                        } else {
                            Toast.makeText(this, "Failed to delete comment.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                commentContainer.addView(commentLayout);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TextView none = new TextView(this);
            none.setText("No comments yet.");
            commentContainer.addView(none);
        }
    }
    // Display dialog to accept edit comment in detail page
    private void showUpdateCommentDialog(int commentId, String oldComment) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Comment");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        EditText etComment = new EditText(this);
        etComment.setHint("Edit your comment...");
        etComment.setText(oldComment);
        layout.addView(etComment);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newComment = etComment.getText().toString().trim();
            if (newComment.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String newTime = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            boolean updated = dbHelper.updateComment(commentId, newComment, newTime);
            if (updated) {
                Toast.makeText(this, "Comment updated!", Toast.LENGTH_SHORT).show();
                loadComments();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    // After update success in UpdateActivity, user will return to HikeDetail
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Hike updated!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }
}
