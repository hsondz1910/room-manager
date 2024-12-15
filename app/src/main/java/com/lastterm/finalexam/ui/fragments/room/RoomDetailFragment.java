package com.lastterm.finalexam.ui.fragments.room;

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

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.uComment;
import com.lastterm.finalexam.data.repositories.RoomRepository;


import com.lastterm.finalexam.ui.fragments.contact.ChatRoomFragment;
import com.lastterm.finalexam.ui.adapter.CommentApdapter;
import com.lastterm.finalexam.ui.adapter.ImageSliderAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    FirebaseFirestore db;

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
        roomPriceTextView.setText("Price: " + String.format("%,.2f VND", room.getPrice()));
        roomAreaTextView.setText("Area: " + room.getAddress());
        roomDescriptionTextView.setText(room.getDescription());

        if (room.isFavorite()) {
            addToFavoritesButton.setText("Remove from favorites");
            addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(room);
                }
            });

        }
        else {
            addToFavoritesButton.setText("Add to favorites");
            addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    RoomRepository roomRepository = new RoomRepository();

                    roomRepository.addToFavorites(room.getId(),auth.getCurrentUser().getUid(), (s) -> {
                        if (s) {
                            Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                        }
                    }, (e) -> {
                        Log.d("fail: ", e.getMessage());});
                    room.setFavorite(true);
                    addToFavoritesButton.setText("Remove from favorites");
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
        labelGood.setText("Good");
        labelNormal.setText("Normal");
        labelBad.setText("Bad");

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
                    case "Good":
                        cmAdapter.setComments(goodComments);
                        break;
                    case "Normal":
                        cmAdapter.setComments(normalComments);
                        break;
                    case "Bad":
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

        bookButton.setOnClickListener(view1 -> {
            Log.d("Click button", "Clicked book button");
            showBookingDialog();
        });

        return view;
    }

    // Updated code for creating DepositRequest in Dialog

    private void showBookingDialog() {
        // Calculate the minimum deposit amount (30% of the room price)
        double minDepositAmount = room.getPrice() * 0.30; // 30% of room price
        String minDepositText = String.format("%,.2f VND", minDepositAmount);

        // Create dialog for the user to enter the deposit amount
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_contract, null);

        // Initialize the elements in the dialog
        TextView contractTermsTextView = dialogView.findViewById(R.id.contract_terms_text);
        TextView depositTextView = dialogView.findViewById(R.id.deposit_amount_text);
        EditText depositInputEditText = dialogView.findViewById(R.id.deposit_input_edit_text);

        // Display the contract terms and minimum deposit amount
        contractTermsTextView.setText("Room description: " + room.getDescription());
        depositTextView.setText("Minimum deposit: " + minDepositText);

        builder.setView(dialogView)
                .setTitle("Confirm Contract")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Get the deposit amount entered by the user
                    String enteredDeposit = depositInputEditText.getText().toString();
                    if (enteredDeposit.isEmpty()) {
                        // If the user hasn't entered anything, use the minimum deposit amount
                        enteredDeposit = String.valueOf(minDepositAmount);
                    }

                    double depositAmount = Double.parseDouble(enteredDeposit);

                    if (depositAmount < minDepositAmount) {
                        // If the deposit amount is less than the minimum, show a message
                        Toast.makeText(getContext(), "Deposit must be greater than or equal to " + minDepositText, Toast.LENGTH_SHORT).show();
                    } else {
                        // Send the deposit request to Firestore (host will approve)
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
        String ownerId = room.getOwnerId(); // Room owner's ID

        // Create deposit request to send to Firestore
        Map<String, Object> depositRequest = new HashMap<>();
        depositRequest.put("userId", userId);
        depositRequest.put("roomId", roomId);
        depositRequest.put("depositAmount", depositAmount);
        depositRequest.put("status", "pending"); // Status: awaiting approval
        depositRequest.put("timestamp", System.currentTimeMillis());
        depositRequest.put("ownerId", ownerId); // Room owner's ID

        // Get the list of room image URLs (take all images in the room)
        List<String> roomImageUrls = room.getImgUrls(); // Ensure that you have `imgUrls` property in `Room`

        if (roomImageUrls != null && !roomImageUrls.isEmpty()) {
            depositRequest.put("roomImageUrls", roomImageUrls); // Save image list into the request
        }

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
                    Toast.makeText(getContext(), "Removed room " + room.getTitle() + " from favorites", Toast.LENGTH_SHORT).show();
                    room.setFavorite(false);
                    addToFavoritesButton.setText("Add to favorites");
                })
                .setNegativeButton("No", null)
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
        repository.getCommentByRoomId(room.getId(), "bad", (comment) -> {
            badComments.addAll(comment);
            comments.addAll(comment);
            comments.sort(Comparator.comparing(cm -> cm.getDate()));
            cmAdapter.notifyDataSetChanged();
        });
    }
}
