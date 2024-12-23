package com.lastterm.finalexam.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.MainActivity;
import com.lastterm.finalexam.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private TextView forgotPasswordLink;
    private Button loginButton, registerButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        // Check if the user is already logged in and redirect them to MainActivity if they are
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserStatus(currentUser.getUid());
        }

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sign in with Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // If sign-in is successful, check user status
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserStatus(user.getUid());
                            }
                        } else {
                            // If sign-in fails, display a message
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Kiểm tra lại email và mật khẩu.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Handle "forgot password" link click
        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Handle register button click
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void checkUserStatus(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isActive = documentSnapshot.getBoolean("isActive");
                        if (isActive != null && isActive) {
                            // User is active, redirect to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // User is inactive, show message and sign out
                            Toast.makeText(LoginActivity.this, "Tài khoản này đã bị xoá! Vui lòng đăng ký tài khoản mới.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra trạng thái tài khoản!", Toast.LENGTH_SHORT).show();
                });
    }
}
