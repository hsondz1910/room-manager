package com.lastterm.finalexam.room.fragmentItem;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.lastterm.finalexam.room.RoomManagementFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
    private ProgressBar progressBar;  // Thêm ProgressBar
    private Uri imageUri;
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
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri);
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
                TextUtils.isEmpty(areaStr) || TextUtils.isEmpty(description) || imageUri == null) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin và chọn hình ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        double area = Double.parseDouble(areaStr);

        progressBar.setVisibility(View.VISIBLE);

        // Upload ảnh lên Imgbb
        uploadImageToImgbb(imageUri, title, address, price, area, description);
    }

    private void uploadImageToImgbb(Uri imageUri, String title, String address, double price, double area, String description) {
        try {
            InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
            byte[] byteArray = getBytes(imageStream);

            String url = "https://api.imgbb.com/1/upload?key=1165e7081f483b04dfd91bc3a1a70d3a";  // Thay thế YOUR_API_KEY bằng API key của bạn
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "image.jpg", RequestBody.create(byteArray, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar
                        Toast.makeText(getContext(), "Tải lên hình ảnh thất bại!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Lấy URL của hình ảnh từ Imgbb
                        String responseString = response.body().string();
                        String imageUrl = null;
                        try {
                            imageUrl = new JSONObject(responseString).getJSONObject("data").getString("url");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        // Tiến hành lưu thông tin vào Firestore
                        Map<String, Object> roomData = new HashMap<>();
                        roomData.put("title", title);
                        roomData.put("address", address);
                        roomData.put("price", price);
                        roomData.put("area", area);
                        roomData.put("description", description);
                        roomData.put("imageUrl", imageUrl);  // Lưu URL ảnh
                        roomData.put("ownerId", userId);

                        // Lưu thông tin vào Firestore
                        db.collection("rooms")
                                .add(roomData)
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar
                                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();

                                    // Load lại danh sách phòng trong RoomManagementFragment
                                    if (getActivity() instanceof MainActivity) {
                                        RoomManagementFragment fragment = (RoomManagementFragment) getActivity().getSupportFragmentManager()
                                                .findFragmentByTag(RoomManagementFragment.class.getSimpleName());
                                        if (fragment != null) {
                                            fragment.loadRooms();  // Gọi lại phương thức loadRooms() để cập nhật dữ liệu
                                        }
                                    }

                                    getParentFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar
                                    Toast.makeText(getContext(), "Đăng bài thất bại!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar
                            Toast.makeText(getContext(), "Tải lên hình ảnh thất bại!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Convert InputStream to byte array
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
