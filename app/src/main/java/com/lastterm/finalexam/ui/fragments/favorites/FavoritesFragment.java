package com.lastterm.finalexam.ui.fragments.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.FavoritesAdapter;
import com.lastterm.finalexam.data.entities.Room;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerFavorites;
    private FavoritesAdapter favoritesAdapter;
    private List<Room> favoriteRooms;
    private FirebaseFirestore db;
    private TextView textNoFavorites;
    private RoomRepository repository;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        auth = FirebaseAuth.getInstance();
        repository = new RoomRepository();
        recyclerFavorites = view.findViewById(R.id.recycler_favorites);
        textNoFavorites = view.findViewById(R.id.text_no_favorites);

        favoriteRooms = new ArrayList<>();
        loadFavoriteRooms();
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadFavoriteRooms() {
        String userId = auth.getCurrentUser().getUid();

        try {
            repository.getFavorites(userId, rooms -> {
                if (rooms.isEmpty()) {
                    return;
                } else {

                    favoriteRooms = rooms;

                    favoritesAdapter = new FavoritesAdapter(getContext(), rooms);
                    recyclerFavorites.setAdapter(favoritesAdapter);
                }
            });

        } catch (Exception e) {
            Log.d("Favorite", e.getMessage());
        }

    }

}
