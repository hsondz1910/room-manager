package com.lastterm.finalexam.ui.room;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.repositories.RoomRepository;

public class RoomDetailActivity extends Fragment {
    TextView roomTitleTextView, roomPriceTextView, roomAreaTextView, roomDescriptionTextView;
    TextView bookButton, addToFavoritesButton;
    ImageView roomImage;

    Room room;

    RoomRepository repository;

    public RoomDetailActivity(Room room) {
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
        roomImage = view.findViewById(R.id.room_image);

        repository = new RoomRepository();

        roomTitleTextView.setText(room.getTitle());
        roomPriceTextView.setText("Giá: " + String.format("%,.2f VND", room.getPrice()));
        roomAreaTextView.setText("Khu vực: " + room.getAddress());
        roomDescriptionTextView.setText(room.getDescription());

        if (room.isFavorite()) {
            addToFavoritesButton.setText("Xóa khỏi mục yêu thích");
            addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(room);

                }
            });

        }
        else {
            addToFavoritesButton.setText("Thêm vào yêu thích");
            addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    RoomRepository roomRepository = new RoomRepository();

                    roomRepository.addToFavorites(room.getId(),auth.getCurrentUser().getUid(), (s) -> {
                        if (s) {
                            Toast.makeText(getContext(), "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }, (e) -> {
                        Log.d("fail: ", e.getMessage());});
                    room.setFavorite(true);
                    addToFavoritesButton.setText("Xóa khỏi mục yêu thích");
                }
            });
        }


        return view;
    }

    private void showDialog(Room room) {
        String roomTile = room.getTitle();
        String roomId = room.getId();
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Bạn có muốn xóa phòng " + roomTile + " ra khởi mục yêu thích không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    repository.removeFromFavorites(roomId, auth.getCurrentUser().getUid(), null, null);
                    Toast.makeText(getContext(), "Đã xóa phòng " + room.getTitle() + "khỏi mục yêu thích", Toast.LENGTH_SHORT).show();
                    room.setFavorite(false);
                    addToFavoritesButton.setText("Thêm vào yêu thích");
                })
                .setNegativeButton("Không", null)
                .create().show();
    }
}
