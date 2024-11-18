package com.lastterm.finalexam.room;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.adapter.owner.RoomManagementAdapter;
import com.lastterm.finalexam.model.Room;
import com.lastterm.finalexam.room.fragmentItem.AddRoomFragment;

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
                .whereEqualTo("ownerId", userId)  // Lọc theo ownerId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            room.setId(document.getId());
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
        // Sử dụng requireContext() thay cho 'this' để lấy context
        AlertDialog.Builder builder2 = new AlertDialog.Builder(requireContext());
        builder2.setTitle("Are you want to delete all of rooms which you selected?");
        builder2.setMessage("You'll lose all your rooms which you selected");
        builder2.setPositiveButton("SURE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        List<Room> selectedItems = new ArrayList<>();
                        for (Room room2 : roomList) {
                            if (!room2.isSelected()) {
                                selectedItems.add(room2);
                            }
                        }
                        roomList.clear();
                        roomList.addAll(selectedItems);
                        roomManagementAdapter.notifyDataSetChanged();
                    }
                });
        builder2.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // don't do anything
                    }
                });
        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }


    private void deleteAllRooms() {
        AlertDialog.Builder builder3 = new AlertDialog.Builder(requireContext());
        builder3.setTitle("Are you want to delete all of data?");
        builder3.setMessage("You'll lost all your data");
        builder3.setPositiveButton("SURE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        roomList.clear();
                        roomManagementAdapter.notifyDataSetChanged();
                    }
                });
        builder3.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // don't do anything
                    }
                });
        AlertDialog dialog3 = builder3.create();
        dialog3.show();
    }
}

