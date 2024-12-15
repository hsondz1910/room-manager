package com.lastterm.finalexam.ui.fragments.room;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomPostFragment extends Fragment {

    private TextInputEditText titleInput, addressInput, priceInput, areaInput, descriptionInput;
    private Button addImagesButton, postButton;
    private RecyclerView imagesRecyclerView;
    private FirebaseFirestore db;
    private List<String> imageUrls = new ArrayList<>();

    public RoomPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_room, container, false);

        titleInput = view.findViewById(R.id.titleInput);
        addressInput = view.findViewById(R.id.addressInput);
        priceInput = view.findViewById(R.id.priceInput);
        areaInput = view.findViewById(R.id.areaInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        addImagesButton = view.findViewById(R.id.addImagesButton);
        postButton = view.findViewById(R.id.postButton);
        imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);

        db = FirebaseFirestore.getInstance();

        postButton.setOnClickListener(v -> postRoom());

        return view;
    }

    // Sử Dụng Default Constructor:
    // Nếu bạn muốn sử dụng constructor mặc định của lớp Room (không có tham số),
    // bạn có thể thiết lập các giá trị sau khi tạo đối tượng Room.
    // Điều này yêu cầu sử dụng setter để gán các giá trị cho các thuộc tính của Room:
    private void postRoom() {
        // Lấy thông tin người dùng nhập vào
        String roomTitle = titleInput.getText().toString();
        String roomAddress = addressInput.getText().toString();
        double roomPrice = Double.parseDouble(priceInput.getText().toString());
        double roomArea = Double.parseDouble(areaInput.getText().toString());
        String roomDescription = descriptionInput.getText().toString();

        // Tạo đối tượng Room bằng constructor mặc định
        Room newRoom = new Room();
        newRoom.setTitle(roomTitle);
        newRoom.setAddress(roomAddress);
        newRoom.setPrice(roomPrice);
        newRoom.setArea(roomArea);
        newRoom.setImgUrls(imageUrls);
        newRoom.setDescription(roomDescription);

        // Nếu cần, thêm `id` và `utilities`
        newRoom.setId(""); // Tạo id tự động hoặc lấy từ Firestore
        newRoom.setUtilities(null); // Đặt giá trị cho utilities nếu cần

        // Lưu vào Firebase Firestore
        db.collection("rooms")
                .add(newRoom)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Đăng phòng thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                });
    }
}
