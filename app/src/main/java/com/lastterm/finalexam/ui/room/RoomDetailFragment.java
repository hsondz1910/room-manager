package com.lastterm.finalexam.ui.room;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.uComment;
import com.lastterm.finalexam.data.entities.Contract;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.CommentApdapter;
import com.lastterm.finalexam.ui.adapter.ImageSliderAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RoomDetailFragment extends Fragment {
    TextView roomTitleTextView, roomPriceTextView, roomAreaTextView, roomDescriptionTextView;
    TextView bookButton, addToFavoritesButton;
    ViewPager2 roomViewPager;

    TextView labelGood, labelNormal, labelBad;
    RecyclerView recyclerView;
    CommentApdapter cmAdapter;

    Room room;
    ArrayList<uComment> comments;
    ArrayList<uComment> goodComments;
    ArrayList<uComment> normalComments;
    ArrayList<uComment> bedComments;

    RoomRepository repository;
    FirebaseFirestore db;

    public RoomDetailFragment(Room room) {
        this.room = room;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_room_detail, container, false);

        roomTitleTextView = view.findViewById(R.id.room_title);
        roomPriceTextView = view.findViewById(R.id.room_price);
        roomAreaTextView = view.findViewById(R.id.room_area);
        roomDescriptionTextView = view.findViewById(R.id.room_description);
        bookButton = view.findViewById(R.id.button_book);
        addToFavoritesButton = view.findViewById(R.id.button_add_to_favorites);

        roomViewPager = view.findViewById(R.id.room_image);

        labelGood = view.findViewById(R.id.label_good);
        labelNormal = view.findViewById(R.id.label_normal);
        labelBad = view.findViewById(R.id.label_bad);
        recyclerView = view.findViewById(R.id.comments_List);

        comments = new ArrayList<>();
        goodComments = new ArrayList<>();
        normalComments = new ArrayList<>();
        bedComments = new ArrayList<>();

        repository = new RoomRepository();
        db = FirebaseFirestore.getInstance();

        roomTitleTextView.setText(room.getTitle());
        roomPriceTextView.setText("Price: " + String.format("%,.2f VND", room.getPrice()));
        roomAreaTextView.setText("Area: " + room.getAddress());
        roomDescriptionTextView.setText(room.getDescription());

        if (room.isFavorite()) {
            addToFavoritesButton.setText("Remove from Favorites");
            addToFavoritesButton.setOnClickListener(view1 -> showDialog(room));
        } else {
            addToFavoritesButton.setText("Add to Favorites");
            addToFavoritesButton.setOnClickListener(view1 -> addToFavorites());
        }

        if (!room.getImgUrls().isEmpty()) {
            try {
                ImageSliderAdapter adapter = new ImageSliderAdapter(getContext(), room.getImgUrls());
                roomViewPager.setAdapter(adapter);
            } catch (Exception e) {
                Log.d("Error", "Error loading image: " + e.getMessage());
            }
        }

        loadComment();
        setupCommentLabels();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookButton.setOnClickListener(view1 -> {
            Log.d("Click button", "Clicked book button");
            showBookingDialog();
        });


        return view;
    }

    private void addToFavorites() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        repository.addToFavorites(room.getId(), auth.getCurrentUser().getUid(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                room.setFavorite(true);
                addToFavoritesButton.setText("Remove from Favorites");
            }
        }, error -> Log.d("fail: ", error.getMessage()));
    }

    private void setupCommentLabels() {
        ArrayList<TextView> labels = new ArrayList<>();
        labels.add(labelGood);
        labels.add(labelNormal);
        labels.add(labelBad);
        labels.forEach(label -> {
            label.setOnClickListener(view -> {
                for (TextView lbl : labels) {
                    lbl.setSelected(false);
                }
                label.setSelected(true);
                switch (label.getText().toString()) {
                    case "Good":
                        cmAdapter.setComments(goodComments);
                        break;
                    case "Normal":
                        cmAdapter.setComments(normalComments);
                        break;
                    case "Not good":
                        cmAdapter.setComments(bedComments);
                        break;
                }
            });
        });
    }

    private void loadComment() {
        repository.getCommentByRoomId(room.getId(), "good", comments -> goodComments.addAll(comments));
        repository.getCommentByRoomId(room.getId(), "normal", comments -> normalComments.addAll(comments));
        repository.getCommentByRoomId(room.getId(), "bbd", comments -> bedComments.addAll(comments));

        comments.addAll(goodComments);
        comments.addAll(normalComments);
        comments.addAll(bedComments);
        comments.sort(Comparator.comparing(comment -> comment.getDate()));
        cmAdapter = new CommentApdapter(comments);
        recyclerView.setAdapter(cmAdapter);
    }

    // Đoạn mã cập nhật cho việc tạo DepositRequest trong Dialog

    private void showBookingDialog() {
        // Tính toán số tiền đặt cọc tối thiểu (30% giá phòng)
        double minDepositAmount = room.getPrice() * 0.30; // 30% của giá phòng
        String minDepositText = String.format("%,.2f VND", minDepositAmount);

        // Tạo dialog cho người dùng nhập số tiền đặt cọc
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_contract, null);

        // Khởi tạo các phần tử trong dialog
        TextView contractTermsTextView = dialogView.findViewById(R.id.contract_terms_text);
        TextView depositTextView = dialogView.findViewById(R.id.deposit_amount_text);
        EditText depositInputEditText = dialogView.findViewById(R.id.deposit_input_edit_text);

        // Hiển thị các điều khoản hợp đồng và số tiền đặt cọc tối thiểu
        contractTermsTextView.setText("Room description: " + room.getDescription());
        depositTextView.setText("Minimum deposit: " + minDepositText);

        builder.setView(dialogView)
                .setTitle("Confirm Contract")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Lấy số tiền đặt cọc mà người dùng đã nhập
                    String enteredDeposit = depositInputEditText.getText().toString();
                    if (enteredDeposit.isEmpty()) {
                        // Nếu người dùng không nhập gì, sử dụng số tiền đặt cọc tối thiểu
                        enteredDeposit = String.valueOf(minDepositAmount);
                    }

                    double depositAmount = Double.parseDouble(enteredDeposit);

                    if (depositAmount < minDepositAmount) {
                        // Nếu số tiền đặt cọc nhỏ hơn tối thiểu, hiển thị thông báo
                        Toast.makeText(getContext(), "Deposit must be greater than or equal to " + minDepositText, Toast.LENGTH_SHORT).show();
                    } else {
                        // Gửi yêu cầu đặt cọc đến Firestore (chủ phòng sẽ phê duyệt)
                        sendDepositRequest(depositAmount);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    private void sendDepositRequest(double depositAmount) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        String roomId = room.getId();
        String ownerId = room.getOwnerId(); // ID chủ phòng

        // Tạo yêu cầu đặt cọc để gửi tới Firestore
        Map<String, Object> depositRequest = new HashMap<>();
        depositRequest.put("userId", userId);
        depositRequest.put("roomId", roomId);
        depositRequest.put("depositAmount", depositAmount);
        depositRequest.put("status", "pending"); // Chờ chủ phòng phê duyệt
        depositRequest.put("timestamp", System.currentTimeMillis());
        depositRequest.put("ownerId", ownerId); // ID chủ phòng

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("depositRequests")
                .add(depositRequest)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Deposit successful! Waiting for host approval.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error sending deposit request.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDialog(Room room) {
        String roomTile = room.getTitle();
        String roomId = room.getId();
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Do you want to remove the room " + roomTile + " from favorites?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    repository.removeFromFavorites(roomId, auth.getCurrentUser().getUid(), null, null);
                    Toast.makeText(getContext(), "Room " + room.getTitle() + " removed from favorites", Toast.LENGTH_SHORT).show();
                    room.setFavorite(false);
                    addToFavoritesButton.setText("Add to Favorites");
                })
                .setNegativeButton("No", null)
                .create().show();
    }
}
