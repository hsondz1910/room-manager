package com.lastterm.finalexam.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.lastterm.finalexam.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    Button resetPasswordButton;
    EditText emailEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        emailEditText = findViewById(R.id.emailEditText);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });

    }

    public void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle errors
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "Email không tồn tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ResetPassword", "Error: " + task.getException().getMessage());
                            Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

