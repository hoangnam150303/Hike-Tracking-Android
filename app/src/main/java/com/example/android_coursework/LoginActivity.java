package com.example.android_coursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // mapping
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // connect database
        db = new DatabaseHelper(this);

        // login button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim(); // get email and password from UI
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) { // if is empty, display toast
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // check account
            boolean checkLogin = db.login(email, password);

            if (checkLogin) {
                // if account is exist
                String username = db.getUsernameByEmail(email); // get username and userId
                int userId = db.getUserIdByEmail(email);

                // Save information to sharedPreferences, it will use in other page
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("username", username);
                editor.putString("email", email);
                editor.putInt("user_id", userId);
                editor.apply();

                Toast.makeText(this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();

                // Paginate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        // paginate to register page
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
