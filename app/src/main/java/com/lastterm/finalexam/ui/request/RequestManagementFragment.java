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
import com.lastterm.finalexam.data.repositories.RequestRepository;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_management, container, false);

        // Initialize RecyclerView and TextView
        recyclerView = view.findViewById(R.id.recyclerView);
        tvStatusMessage = view.findViewById(R.id.tvStatusMessage);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize RequestRepository
        requestRepository = new RequestRepository();

        // Fetch data from Firestore
        fetchDepositRequests();

        return view;
    }

    private void fetchDepositRequests() {
        requestRepository.fetchDepositRequests(new RequestRepository.Callback() {
            @Override
            public void onSuccess(List<DepositRequest> depositRequests) {
                // Tạo adapter mới và set dữ liệu vào RecyclerView
                requestAdapter = new RequestManagementAdapter(depositRequests);
                recyclerView.setAdapter(requestAdapter);  // Set adapter sau khi nhận được dữ liệu

                // Cập nhật thông báo trạng thái
                tvStatusMessage.setText("You have " + depositRequests.size() + " requests pending.");
            }

            @Override
            public void onError(String errorMessage) {
                // Xử lý lỗi và cập nhật giao diện
                tvStatusMessage.setText(errorMessage);
            }
        });
    }
}
