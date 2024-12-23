package com.lastterm.finalexam.ui.fragments.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.User;
import com.lastterm.finalexam.ui.adapter.UserManagementAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> userList;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    private UserManagementAdapter userManagementAdapter;
    private String userId;
    private boolean allChecked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setHomeButtonEnabled(false);

        toolbar.inflateMenu(R.menu.menu_user_management);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userManagementAdapter = new UserManagementAdapter(userList, requireContext());
        recyclerView.setAdapter(userManagementAdapter);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            Log.d("UserManagementFragment", "Current User ID: " + userId);
        } else {
            Log.d("UserManagementFragment", "No user is currently signed in");
        }

        loadUsers();

        return view;
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            user.setUserId(document.getId());
                            Log.d("UserManagement", "document.getId(): " + user.getUserId());

                            // Handle default isActive value if not present
                            if (document.get("isActive") == null) {
                                user.setActive(false);
                            }

                            String imgUrl = user.getUrlAvatar();
                            if (imgUrl != null) {
                                Log.d("UserManagement", "User: " + user.getEmail());
                                Log.d("RoomManagement", "Imgage URL: " + imgUrl);

                            } else {
                                Log.d("UserManagement", "No image url for user: " + user.getEmail());
                            }

                            userList.add(user);
                        }

                        userManagementAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to laod users", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_user_management, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.check_all) {
            toggleSelectAllUsers();
            return true;
        } else if (item.getItemId() == R.id.delete_selected) {
            deleteSelectedUsers();
            return true;
        } else if (item.getItemId() == R.id.delete_all) {
            deleteAllUsers();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllUsers() {
    }

    private void deleteSelectedUsers() {
    }

    private void toggleSelectAllUsers() {
        if (!allChecked) {
            for (User user1 : userList) {
                user1.setSelected(true);
            }
        } else {
            for (User user2 : userList) {
                user2.setSelected(false);
            }
        }
        allChecked = !allChecked;
        userManagementAdapter.notifyDataSetChanged();
    }

}
