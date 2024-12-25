package com.lastterm.finalexam.ui.fragments.search;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.chip.Chip;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Location;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.RoomAdapter;
import com.lastterm.finalexam.data.entities.RoomFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        roomRepository.listenForRoomUpdates(rooms -> {
            if (!rooms.isEmpty()) {
                adapter = new RoomAdapter(rooms, getContext());
                recyclerView.setAdapter(adapter);
            }
        }, newRoom -> {
            showNotification("Phòng mới", "Phòng mới được tải lên: " + newRoom.getTitle());
            roomRepository.getAllRooms(rooms -> {
                if (rooms.isEmpty()) {
                    Toast.makeText(getContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                } else {
                    adapter = new RoomAdapter(rooms, getContext());
                    recyclerView.setAdapter(adapter);
                }
            });
        });

        chipPrice.setOnClickListener(v -> {
            // Tạo LinearLayout với hướng dọc (Vertical)
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            EditText inputMin = new EditText(requireContext());
            inputMin.setHint("Nhập giá tối thiểu");
            inputMin.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputMin.setText(String.valueOf(filter.getMinPrice()==0?"":filter.getMinPrice()));
            layout.addView(inputMin);


            EditText inputMax = new EditText(requireContext());
            inputMax.setHint("Nhập giá tối đa");
            inputMax.setInputType(InputType.TYPE_CLASS_NUMBER);
            inputMax.setText(String.valueOf(filter.getMaxPrice()==0?"":filter.getMaxPrice()));
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

            final Map<String, List<String>> locations = Location.ListCityanDistrict();

            Spinner citySpinner = new Spinner(requireContext());
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    new ArrayList<>(locations.keySet())
            );
            citySpinner.setAdapter(cityAdapter);

            Spinner districtSpinner = new Spinner(requireContext());
            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    new ArrayList<>()
            );
            districtSpinner.setAdapter(districtAdapter);
            districtSpinner.setPadding(10, 20, 50, 20);

            String selectedCity = filter.getLocation().getCity();
            final String[] selectedDistrict = {filter.getLocation().getDistrict()};

            if (selectedCity != "") {
                int cityPosition = new ArrayList<>(locations.keySet()).indexOf(selectedCity);
                citySpinner.setSelection(cityPosition);
                List<String> districts = locations.getOrDefault(selectedCity, new ArrayList<>());
                districtAdapter.addAll(districts);
                districtAdapter.notifyDataSetChanged();

                if (!selectedDistrict[0].isEmpty()) {
                    int districtPosition = districts.indexOf(selectedDistrict[0]);
                    districtSpinner.setSelection(districtPosition);
                }
            }

            citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newCity = (String) citySpinner.getSelectedItem();
                    List<String> districts = locations.getOrDefault(newCity, new ArrayList<>());
                    districtAdapter.clear();
                    districtAdapter.addAll(districts);
                    districtAdapter.notifyDataSetChanged();

                    if (!newCity.equals(selectedCity)) {
                        selectedDistrict[0] = "";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            citySpinner.setPadding(10, 20, 50, 20);

            TextView cityTitle = new TextView(requireContext());
            cityTitle.setText("Thành phố");
            cityTitle.setPadding(0, 20, 50, 20);

            TextView districtTitle = new TextView(requireContext());
            districtTitle.setText("Quận/Huyện");
            districtTitle.setPadding(0, 20, 50, 20);

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(cityTitle);
            layout.addView(citySpinner);
            layout.addView(districtTitle);
            layout.addView(districtSpinner);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Chọn khu vực")
                    .setView(layout)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String city = citySpinner.getSelectedItem().toString();
                        String district = districtSpinner.getSelectedItem().toString();
                        filter.setLocation(new Location(city, district));

                        searchRooms();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();

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

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "new_rooms_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo phòng mới",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

}