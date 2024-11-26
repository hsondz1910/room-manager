package com.lastterm.finalexam.ui.room;

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

        roomTitleTextView.setText(room.getTitle());
        roomPriceTextView.setText("Giá: " + String.format("%,.2f VND", room.getPrice()));
        roomAreaTextView.setText("Khu vực: " + room.getAddress());
        roomDescriptionTextView.setText(room.getDescription());



        addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                RoomRepository roomRepository = new RoomRepository();

                roomRepository.addToFavorites(room.getId(),auth.getCurrentUser().getUid(), (s) -> {
                    if (s) {
                        Toast.makeText(getContext(), "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Đã có trong mục yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }, (e) -> {
                    Log.d("fail: ", e.getMessage());});
            }
        });



        return view;
    }
}
