package com.lastterm.finalexam.ui.fragments.search;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.RoomAdapter;
import com.lastterm.finalexam.data.entities.RoomFilter;

public class SearchFragment extends Fragment {
    private RoomRepository roomRepository;
    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        roomRepository = new RoomRepository();

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRooms(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        roomRepository.getAllRooms(rooms -> {
            if (rooms.isEmpty()) {
                Toast.makeText(getContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new RoomAdapter(rooms, getContext());
                recyclerView.setAdapter(adapter);
            }
        });

        return view;
    }

    private void searchRooms(String query) {
        RoomFilter filter = new RoomFilter(10000000, 20); // Max price and min area (test)
        roomRepository.searchRooms(filter, rooms -> {
            if (rooms.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new RoomAdapter(rooms, getContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
