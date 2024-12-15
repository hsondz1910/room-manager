package com.lastterm.finalexam.ui.account;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.User;
import com.lastterm.finalexam.data.repositories.RoomRepository;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText edtFullName, edtUsername, edtEmail, edtPhone, edtRole;
    private ImageView imageEditProfile;
    private Button btnSave, btnChangePassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private RoomRepository repository;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        edtFullName = view.findViewById(R.id.edtFullName);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtRole = view.findViewById(R.id.edtRole);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        imageEditProfile = view.findViewById(R.id.image_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        repository = new RoomRepository();

        loadUserData();

        btnSave.setOnClickListener(v -> updateProfile());

        btnChangePassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            if (!TextUtils.isEmpty(email)) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Email đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Gửi email thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imageEditProfile.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        imageEditProfile.setOnClickListener((v) -> {
            showDialogPickImg();
        });

        return view;
    }

    private void loadUserData() {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtFullName.setText(documentSnapshot.getString("fullName"));
                edtUsername.setText(documentSnapshot.getString("username"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtPhone.setText(documentSnapshot.getString("phone"));
                edtRole.setText(documentSnapshot.getString("role"));
                if(documentSnapshot.getString("urlAvatar") != null){
                    try {
                        Glide.with(this).load(documentSnapshot.getString("urlAvatar")).into(imageEditProfile);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void updateProfile() {
        String fullName = edtFullName.getText().toString();
        String username = edtUsername.getText().toString();
        String email = edtEmail.getText().toString();
        String phone = edtPhone.getText().toString();
        String role = edtRole.getText().toString();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(fullName, username, email, phone, role,"");
        if(selectedImageUri != null){
            repository.uploadImageImgBBForChat(getContext(), repository.getCurrentUser(), selectedImageUri,(url) -> {
                user.setUrlAvatar(url);
                db.collection("users").document(userId).update(user.toMap())
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show());
            });
        }



    }

    private void showDialogPickImg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có muốn mở thư viện ảnh")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    openImagePicker();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setCancelable(false);
        builder.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}

