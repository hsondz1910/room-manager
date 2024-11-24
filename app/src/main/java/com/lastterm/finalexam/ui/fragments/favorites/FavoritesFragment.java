package com.lastterm.finalexam.ui.fragments.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.ui.adapter.FavoritesAdapter;
import com.lastterm.finalexam.data.entities.Room;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerFavorites;
    private FavoritesAdapter favoritesAdapter;
    private List<Room> favoriteRooms;
    private FirebaseFirestore db;
    private TextView textNoFavorites;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return null;
    }

    private void loadFavoriteRooms() {

    }

}
