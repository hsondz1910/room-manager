package com.lastterm.finalexam.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.DepositRequest;

import java.util.List;

public class RequestManagementAdapter extends RecyclerView.Adapter<RequestManagementAdapter.RequestViewHolder> {

    private List<DepositRequest> depositRequests;

    public RequestManagementAdapter(List<DepositRequest> depositRequests) {
        this.depositRequests = depositRequests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_management, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DepositRequest request = depositRequests.get(position);

        // Bind data to UI elements
        holder.roomTitle.setText("Room " + request.getRoomId());
        holder.depositAmount.setText("Deposit: $" + request.getDepositAmount());
        holder.userName.setText("User: " + request.getUserId());

        holder.createContractButton.setOnClickListener(v -> {
            // Handle contract creation
        });

        holder.rejectButton.setOnClickListener(v -> {
            // Handle rejection
        });
    }

    @Override
    public int getItemCount() {
        return depositRequests.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView roomTitle, depositAmount, userName;
        Button createContractButton, rejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            depositAmount = itemView.findViewById(R.id.depositAmount);
            userName = itemView.findViewById(R.id.userName);
            createContractButton = itemView.findViewById(R.id.createContractButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
