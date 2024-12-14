package com.lastterm.finalexam.ui.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.DepositRequest;
import com.lastterm.finalexam.ui.adapter.RequestManagementAdapter;

import java.util.ArrayList;
import java.util.List;

public class RequestManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvStatusMessage;
    private List<DepositRequest> depositRequests;
    private RequestManagementAdapter requestAdapter;

    public RequestManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_management, container, false);

        // Initialize RecyclerView and TextView
        recyclerView = view.findViewById(R.id.recyclerView);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data (in a real app, this would come from a database or API)
        depositRequests = new ArrayList<>();
        depositRequests.add(new DepositRequest("1", "user_123", "room_456", 100.00, "pending", System.currentTimeMillis(), "owner_789"));
        depositRequests.add(new DepositRequest("2", "user_124", "room_457", 200.00, "approved", System.currentTimeMillis(), "owner_790"));
        depositRequests.add(new DepositRequest("3", "user_125", "room_458", 150.00, "rejected", System.currentTimeMillis(), "owner_791"));

        // Initialize the adapter and set it to the RecyclerView
        requestAdapter = new RequestManagementAdapter(depositRequests);
        recyclerView.setAdapter(requestAdapter);

        // Set status message (can be dynamic based on conditions)
        tvStatusMessage.setText("You have " + depositRequests.size() + " requests pending.");

        return view;
    }
}
