package com.lastterm.finalexam.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.User;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameInput, usernameInput, passwordInput, emailInput, phoneInput;
    private Button registerButton;
    private RadioGroup roleGroup;
    private RadioButton roleOwner, roleTenant;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Mapping UI elements
        fullNameInput = findViewById(R.id.full_name_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        registerButton = findViewById(R.id.register_button);

        roleGroup = findViewById(R.id.role_group);
        roleOwner = findViewById(R.id.role_owner);
        roleTenant = findViewById(R.id.role_tenant);

        // Default role selection
        roleTenant.setChecked(true);

        registerButton.setOnClickListener(v -> {
            // Retrieve input values
            String fullName = fullNameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            Boolean status = true;

            // Retrieve selected role
            String role = (roleGroup.getCheckedRadioButtonId() == R.id.role_owner) ? "owner" : "tenant";

            // Validate inputs
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user via Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Registration successful, save user info to Firestore
                            String userId = mAuth.getCurrentUser().getUid();

                            // Create User object
                            User user = new User(
                                    userId,
                                    fullName,
                                    username,
                                    email,
                                    phone,
                                    role,
                                    "",  // Default avatar URL (empty)
                                    status  // Active status by default
                            );

                            // Save user to Firestore
                            db.collection("users").document(userId)
                                    .set(user.toMap())  // Convert to map before saving
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to login page
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Registration failed
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
