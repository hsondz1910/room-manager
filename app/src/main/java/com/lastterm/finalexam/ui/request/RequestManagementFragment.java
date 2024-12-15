package com.lastterm.finalexam.ui.request;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.DepositRequest;
import com.lastterm.finalexam.ui.adapter.RequestManagementAdapter;
import com.lastterm.finalexam.data.repositories.RequestRepository;

import java.util.ArrayList;
import java.util.List;

public class RequestManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvStatusMessage;
    private RequestManagementAdapter requestAdapter;
    private RequestRepository requestRepository;

    public RequestManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_management, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setHomeButtonEnabled(false);

        requestRepository = new RequestRepository();

        recyclerView = view.findViewById(R.id.recyclerView);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter rỗng và gán cho RecyclerView
        requestAdapter = new RequestManagementAdapter(new ArrayList<>());
        recyclerView.setAdapter(requestAdapter);

        fetchDepositRequests();

        return view;
    }

    private void fetchDepositRequests() {
        if (requestRepository == null) {
            tvStatusMessage.setText("Error: Request repository is not initialized.");
            return;
        }

        requestRepository.fetchDepositRequests(new RequestRepository.Callback() {
            @Override
            public void onSuccess(List<DepositRequest> depositRequests) {
                // Update adapter with new data
                requestAdapter.updateData(depositRequests);

                // Update status message
                tvStatusMessage.setText("Bạn có các yêu cầu \" + depositRequests.size() + \" đang chờ xử lý.");
            }

            @Override
            public void onError(String errorMessage) {
                // Display error message
                tvStatusMessage.setText(errorMessage);
                Log.e("RequestManagementFragment", "Error fetching deposit requests: " + errorMessage);
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_room_management, menu);
    }

}
