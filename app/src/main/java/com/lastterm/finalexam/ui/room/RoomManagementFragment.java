package com.lastterm.finalexam.ui.room;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.ui.adapter.RoomManagementAdapter;
import com.lastterm.finalexam.data.entities.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomManagementAdapter roomManagementAdapter;
    private List<Room> roomList;
    private FirebaseFirestore db;
    private String userId;
    private Button btnPostRoom;
    private boolean allChecked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_management, container, false);
        setHasOptionsMenu(true);

        // Initialize FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setHomeButtonEnabled(false);

        toolbar.inflateMenu(R.menu.menu_room_management);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomList = new ArrayList<>();
        roomManagementAdapter = new RoomManagementAdapter(roomList, requireContext());
        recyclerView.setAdapter(roomManagementAdapter);

        btnPostRoom = view.findViewById(R.id.btnPostRoom);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadRooms();

        btnPostRoom.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddRoomFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    public void loadRooms() {
        db.collection("rooms")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            room.setId(document.getId());

                            // Thêm log để kiểm tra URL
                            List<String> imageUrls = room.getImgUrls();
                            if (imageUrls != null) {
                                Log.d("RoomManagement", "Room: " + room.getTitle());
                                for (String url : imageUrls) {
                                    Log.d("RoomManagement", "Image URL: " + url);
                                }
                            } else {
                                Log.d("RoomManagement", "No image URLs for room: " + room.getTitle());
                            }

                            roomList.add(room);
                        }
                        roomManagementAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_room_management, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.check_all) {
            toggleSelectAllRooms();
            return true;
        } else if (item.getItemId() == R.id.delete_selected) {
            deleteSelectedRooms();
            return true;
        } else if (item.getItemId() == R.id.delete_all) {
            deleteAllRooms();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void toggleSelectAllRooms() {
        if (!allChecked) {
            for (Room room1 : roomList) {
                room1.setSelected(true);
            }
        } else {
            for (Room room1 : roomList) {
                room1.setSelected(false);
            }
        }
        allChecked = !allChecked;
        roomManagementAdapter.notifyDataSetChanged();
    }

    private void deleteSelectedRooms() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(requireContext());
        builder2.setTitle("Do you want to delete all selected rooms?");
        builder2.setMessage("You will lose all the selected rooms.");

        builder2.setPositiveButton("SURE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                List<Room> selectedItems = new ArrayList<>();
                List<String> roomIdsToDelete = new ArrayList<>();

                // Gather the room IDs of the selected rooms
                for (Room room : roomList) {
                    if (room.isSelected()) {
                        roomIdsToDelete.add(room.getId());
                    } else {
                        selectedItems.add(room);
                    }
                }

                // Remove selected items from the list
                roomList.clear();
                roomList.addAll(selectedItems);

                // Notify the adapter of the changes
                roomManagementAdapter.notifyDataSetChanged();

                // Delete rooms from Firestore
                for (String roomId : roomIdsToDelete) {
                    db.collection("rooms").document(roomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Room deleted from Firestore", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error deleting room from Firestore", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        builder2.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Don't do anything if user cancels
            }
        });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }

    private void deleteAllRooms() {
        AlertDialog.Builder builder3 = new AlertDialog.Builder(requireContext());
        builder3.setTitle("Do you want to delete all rooms?");
        builder3.setMessage("You will lose all the data.");

        builder3.setPositiveButton("SURE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Gather all room IDs to delete
                List<String> roomIdsToDelete = new ArrayList<>();
                for (Room room : roomList) {
                    roomIdsToDelete.add(room.getId());
                }

                // Clear the local room list
                roomList.clear();
                roomManagementAdapter.notifyDataSetChanged();

                // Delete rooms from Firestore
                for (String roomId : roomIdsToDelete) {
                    db.collection("rooms").document(roomId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "All rooms deleted from Firestore", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error deleting rooms from Firestore", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        builder3.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Don't do anything if user cancels
            }
        });

        AlertDialog dialog3 = builder3.create();
        dialog3.show();
    }
}

