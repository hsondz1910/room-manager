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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameInput = findViewById(R.id.full_name_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        registerButton = findViewById(R.id.register_button);

        // Map RadioGroup and RadioButton
        roleGroup = findViewById(R.id.role_group);
        roleOwner = findViewById(R.id.role_owner);
        roleTenant = findViewById(R.id.role_tenant);

        // Default select Tenant
        roleTenant.setChecked(true);

        registerButton.setOnClickListener(v -> {
            // Get information from EditTexts
            String fullName = fullNameInput.getText().toString();
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String email = emailInput.getText().toString();
            String phone = phoneInput.getText().toString();

            // Get selected role from RadioGroup
            String role;  // Default is "tenant"
            if (roleGroup.getCheckedRadioButtonId() == R.id.role_owner) {
                role = "owner";  // If "Owner" is selected
            } else {
                role = "tenant";
            }

            // Register user via Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Registration successful, save user info to Firestore
                            String userId = mAuth.getCurrentUser().getUid();
                            User user = new User(fullName, username, email, phone, role);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to login screen after successful registration
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();  // Ensure no return to the registration page
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user info", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
