package com.lastterm.finalexam.ui.room;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.MainActivity;
import com.lastterm.finalexam.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddRoomFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText edtTitle, edtAddress, edtPrice, edtArea, edtDescription;
    private Button btnUpload, btnChooseImage;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private List<Uri> imageUris = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_room, container, false);

        edtTitle = view.findViewById(R.id.edtTitle);
        edtAddress = view.findViewById(R.id.edtAddress);
        edtPrice = view.findViewById(R.id.edtPrice);
        edtArea = view.findViewById(R.id.edtArea);
        edtDescription = view.findViewById(R.id.edtDescription);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnChooseImage = view.findViewById(R.id.btnChooseImage);
        imgPreview = view.findViewById(R.id.imgPreview);
        progressBar = view.findViewById(R.id.progressBar);  // Khởi tạo ProgressBar

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnChooseImage.setOnClickListener(v -> chooseImage());
        btnUpload.setOnClickListener(v -> uploadRoom());

        return view;
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // Allow multiple images
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) { // Single image selected
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
            // Update image preview (just showing the first image for now)
            imgPreview.setImageURI(imageUris.get(0));
            imgPreview.setVisibility(View.VISIBLE);
        }
    }

    private void uploadRoom() {
        String title = edtTitle.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String areaStr = edtArea.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(address) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(areaStr) || TextUtils.isEmpty(description) || imageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin và chọn hình ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        double area = Double.parseDouble(areaStr);

        progressBar.setVisibility(View.VISIBLE);

        // Upload images to Imgbb
        uploadImagesToImgbb(imageUris, title, address, price, area, description);
    }

    private void uploadImagesToImgbb(List<Uri> imageUris, String title, String address, double price, double area, String description) {
        OkHttpClient client = new OkHttpClient();
        List<String> imageUrls = new ArrayList<>();

        for (Uri imageUri : imageUris) {
            try {
                InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                byte[] byteArray = getBytes(imageStream);

                String url = "https://api.imgbb.com/1/upload?key=1165e7081f483b04dfd91bc3a1a70d3a";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "room_image.jpg", RequestBody.create(byteArray, MediaType.parse("image/jpeg")))
                        .addFormDataPart("name", title) // Tên ảnh
                        .addFormDataPart("description", "Room Image for " + title) // Mô tả
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        handleImageUploadFailure(e);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.body().string());
                                String imageUrl = jsonResponse.getJSONObject("data").getString("url");
                                imageUrls.add(imageUrl);

                                if (imageUrls.size() == imageUris.size()) {
                                    handleImageUploadSuccess(imageUrls, title, address, price, area, description);
                                }
                            } catch (JSONException e) {
                                handleImageUploadFailure(e);
                            }
                        } else {
                            handleImageUploadFailure(new IOException("Failed to upload image: " + response.message()));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                handleImageUploadFailure(e);
            }
        }
    }

    private void handleImageUploadSuccess(List<String> imageUrls, String title, String address, double price, double area, String description) {
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("title", title);
        roomData.put("address", address);
        roomData.put("price", price);
        roomData.put("area", area);
        roomData.put("description", description);

        // Chú ý: Đảm bảo đúng tên trường ở đây
        roomData.put("imgUrls", imageUrls);  // Đúng tên trường

        roomData.put("ownerId", userId);
        roomData.put("isFavorite", false);
        roomData.put("isSelected", false);

        // Log để kiểm tra
        for (String url : imageUrls) {
            Log.d("AddRoom", "Uploading image URL: " + url);
        }

        db.collection("rooms")
                .add(roomData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AddRoom", "Room added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    navigateBackToRoomManagement();
                });
    }

    private void handleImageUploadFailure(Exception e) {
        getActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Tải lên hình ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateBackToRoomManagement() {
        if (getActivity() instanceof MainActivity) {
            RoomManagementFragment fragment = new RoomManagementFragment();
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
