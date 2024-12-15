package com.lastterm.finalexam.ui.room;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.lastterm.finalexam.R;

import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.uComment;
import com.lastterm.finalexam.data.repositories.RoomRepository;


import com.lastterm.finalexam.ui.fragments.contact.ChatRoomFragment;
import com.lastterm.finalexam.ui.adapter.CommentApdapter;
import com.lastterm.finalexam.ui.adapter.ImageSliderAdapter;


import java.util.ArrayList;
import java.util.Comparator;

public class RoomDetailFragment extends Fragment {
    TextView roomTitleTextView, roomPriceTextView, roomAreaTextView, roomDescriptionTextView;
    TextView bookButton, addToFavoritesButton, contactButton;
    ViewPager2 roomViewPager;

    TextView labelGood, labelNormal, labelBad;
    RecyclerView recyclerView;
    CommentApdapter cmAdapter;

    Room room;
    ArrayList<uComment> comments;
    ArrayList<uComment> goodComments;
    ArrayList<uComment> normalComments;
    ArrayList<uComment> badComments;

    RoomRepository repository;

    public RoomDetailFragment(Room room) {
        this.room = room;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_room_detail, container, false);

        //TextView
        roomTitleTextView = view.findViewById(R.id.room_title);
        roomPriceTextView = view.findViewById(R.id.room_price);
        roomAreaTextView = view.findViewById(R.id.room_area);
        roomDescriptionTextView = view.findViewById(R.id.room_description);

        //Button
        bookButton = view.findViewById(R.id.button_book);
        addToFavoritesButton = view.findViewById(R.id.button_add_to_favorites);
        contactButton = view.findViewById(R.id.button_contact);

        //Image
        roomViewPager = view.findViewById(R.id.room_image);

        //Comment label
        labelGood = view.findViewById(R.id.label_good);
        labelNormal = view.findViewById(R.id.label_normal);
        labelBad = view.findViewById(R.id.label_bad);
        recyclerView = view.findViewById(R.id.comments_List);



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

        if(!room.getImgUrls().isEmpty()){

            try {
                ImageSliderAdapter adapter = new ImageSliderAdapter(getContext(), room.getImgUrls());
                roomViewPager.setAdapter(adapter);
            } catch (Exception e) {
                Log.d("Error", "Error loading image :" + e.getMessage());
            }
        }

        // Comment
        comments = new ArrayList<>();
        goodComments = new ArrayList<>();
        normalComments = new ArrayList<>();
        badComments = new ArrayList<>();
        labelGood.setText("Tốt");
        labelNormal.setText("Bình thường");
        labelBad.setText("Không tốt");

        cmAdapter = new CommentApdapter(comments, getContext());
        recyclerView.setAdapter(cmAdapter);

        loadCOmment();
        ArrayList<TextView> labels = new ArrayList<>();
        labels.add(labelGood);
        labels.add(labelNormal);
        labels.add(labelBad);
        labels.forEach(label -> {
            label.setOnClickListener(view1 -> {
                for (TextView lbl : labels) {
                    lbl.setSelected(false);
                }

                label.setSelected(true);
                switch (label.getText().toString()){
                    case "Tốt":
                        cmAdapter.setComments(goodComments);
                        break;
                    case "Bình thường":
                        cmAdapter.setComments(normalComments);
                        break;
                    case "Không tốt":
                        cmAdapter.setComments(badComments);
                        break;
                }

            });
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Contact
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment defaultFragment = new ChatRoomFragment(room.getId(),repository.getCurrentUser(), room.getOwnerId());
                if (defaultFragment != null) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, defaultFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });


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

    private void loadCOmment() {
        repository.getCommentByRoomId(room.getId(), "good", (comment) -> {
            goodComments.addAll(comment);
            comments.addAll(comment);
            comments.sort(Comparator.comparing(cm -> cm.getDate()));
            cmAdapter.notifyDataSetChanged();
        });
        repository.getCommentByRoomId(room.getId(), "normal", (comment) -> {
            normalComments.addAll(comment);
            comments.addAll(comment);
            comments.sort(Comparator.comparing(cm -> cm.getDate()));
            cmAdapter.notifyDataSetChanged();
        });
        repository.getCommentByRoomId(room.getId(), "bbd", (comment) -> {
            normalComments.addAll(comment);
            comments.addAll(comment);
            comments.sort(Comparator.comparing(cm -> cm.getDate()));
            cmAdapter.notifyDataSetChanged();
        });


    }
}