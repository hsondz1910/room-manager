package com.lastterm.finalexam.bottomMenu.fragmentItem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.adapter.tenant.FavoritesAdapter;
import com.lastterm.finalexam.model.Room;

import java.util.ArrayList;
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
