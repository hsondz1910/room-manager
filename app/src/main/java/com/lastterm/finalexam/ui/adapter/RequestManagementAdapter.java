package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.DepositRequest;
import com.lastterm.finalexam.data.entities.Room;

import java.util.List;

public class RequestManagementAdapter extends RecyclerView.Adapter<RequestManagementAdapter.RequestViewHolder> {
    private List<DepositRequest> depositRequestsList;
    private Context context;
    private FirebaseFirestore db;

    public RequestManagementAdapter(List<DepositRequest> depositRequestsList) {
        this.depositRequestsList = depositRequestsList;
        this.context = context;
        this.db = db;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_management, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DepositRequest request = depositRequestsList.get(position);

        // Bind data to UI elements
        holder.roomTitle.setText("Room " + request.getRoomId());
        holder.depositAmount.setText("Deposit: $" + request.getDepositAmount());
        holder.userName.setText("User: " + request.getUserId());

        // Glide to load image into ImageView (using the room's image URL)
        Glide.with(holder.roomImage.getContext())
                .load(request.getRoomImageUrls())
                .placeholder(R.drawable.placeholder_img)
                .into(holder.roomImage);

        // Load room image using Glide
        if (request.getRoomImageUrls() != null && !request.getRoomImageUrls().isEmpty()) {
            String imageUrl = request.getRoomImageUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.error_image)
                    .into(holder.roomImage);
        }

        holder.createContractButton.setOnClickListener(v -> {
            // Handle contract creation
        });

        holder.rejectButton.setOnClickListener(v -> {
            // Handle rejection
        });
    }

    @Override
    public int getItemCount() {
        return depositRequestsList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView roomTitle, depositAmount, userName;
        Button createContractButton, rejectButton;
        ImageView roomImage;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            depositAmount = itemView.findViewById(R.id.depositAmount);
            userName = itemView.findViewById(R.id.userName);
            createContractButton = itemView.findViewById(R.id.createContractButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            roomImage = itemView.findViewById(R.id.roomImageManagement);
        }
    }
}
