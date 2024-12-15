package com.lastterm.finalexam.ui.fragments.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.chip.Chip;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.RoomAdapter;
import com.lastterm.finalexam.data.entities.RoomFilter;

public class SearchFragment extends Fragment {
    private RoomRepository roomRepository;
    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private SearchView searchView;
    private Chip chipPrice;
    private Chip chipArea;
    private Chip chipLocation;
    private RoomFilter filter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        roomRepository = new RoomRepository();

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        chipPrice = view.findViewById(R.id.chipPrice);
        chipArea = view.findViewById(R.id.chipArea);
        chipLocation = view.findViewById(R.id.chipLocation);

        filter = new RoomFilter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter.setSearch(query);
                searchRooms();
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

        chipPrice.setOnClickListener(v -> {
            // Tạo LinearLayout với hướng dọc (Vertical)
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            EditText inputMin = new EditText(requireContext());
            inputMin.setHint("Nhập giá tối thiểu");
            inputMin.setInputType(InputType.TYPE_CLASS_NUMBER);  // Chỉ cho phép nhập số
            inputMin.setText(String.valueOf(filter.getMinPrice()==0?"":filter.getMinPrice()));  // Hiển thị giá trị minPrice hiện tại
            layout.addView(inputMin);


            EditText inputMax = new EditText(requireContext());
            inputMax.setHint("Nhập giá tối đa");
            inputMax.setInputType(InputType.TYPE_CLASS_NUMBER);  // Chỉ cho phép nhập số
            inputMax.setText(String.valueOf(filter.getMaxPrice()==0?"":filter.getMaxPrice()));  // Hiển thị giá trị maxPrice hiện tại
            layout.addView(inputMax);


            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Nhập giá")
                    .setView(layout)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String minPrice = inputMin.getText().toString();
                        String maxPrice = inputMax.getText().toString();

                        if (!minPrice.isEmpty()) {
                            filter.setMinPrice(Long.parseLong(minPrice));
                        }else filter.setMinPrice(0);
                        if (!maxPrice.isEmpty()) {
                            filter.setMaxPrice(Long.parseLong(maxPrice));
                        }else filter.setMaxPrice(0);
                        searchRooms();

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

            builder.show();

        });

        chipArea.setOnClickListener(v -> {

            EditText area = new EditText(requireContext());
            area.setHint("Diện tích tối thiểu");
            area.setInputType(InputType.TYPE_CLASS_NUMBER);
            area.setText(String.valueOf(filter.getArea()==0?"":filter.getArea()));

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Nhập diện tích tối thiểu")
                    .setView(area)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String areaMin = area.getText().toString();


                        if (!areaMin.isEmpty()) {
                            filter.setArea(Long.parseLong(areaMin));

                        }else {
                            filter.setArea(0);
                        }

                        searchRooms();

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

            builder.show();

        });

        chipLocation.setOnClickListener(v -> {

            EditText Location = new EditText(requireContext());
            Location.setHint("Khu vực");
            Location.setText(String.valueOf(filter.getLocation()));

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Khu vực")
                    .setView(Location)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String location = Location.getText().toString();
                        filter.setLocation(location);

                        searchRooms();

                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

            builder.show();

        });

        return view;
    }

    private void searchRooms() {
        roomRepository.searchRooms(filter, rooms -> {
            if (rooms.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new RoomAdapter(rooms, getContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }
}