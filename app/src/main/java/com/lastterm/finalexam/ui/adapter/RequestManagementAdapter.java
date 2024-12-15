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

import java.util.List;

public class RequestManagementAdapter extends RecyclerView.Adapter<RequestManagementAdapter.RequestViewHolder> {
    private List<DepositRequest> depositRequestsList;

    public RequestManagementAdapter(List<DepositRequest> depositRequestsList) {
        this.depositRequestsList = depositRequestsList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_management, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        // Fetch userName from Firestore using userId
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DepositRequest request = depositRequestsList.get(position);

        // Bind data to UI elements
        db.collection("rooms") // Assuming user data is in "rooms" collection
                .document(request.getRoomId()) // Fetch document by roomId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String roomTitle = documentSnapshot.getString("title");
                        holder.roomTitle.setText("Tên trọ: " + (roomTitle != null ? roomTitle : "Không có"));
                    } else {
                        holder.roomTitle.setText("Tên trọ: Không có");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.userName.setText("Tên trọ: Không có");
                });

        db.collection("users") // Assuming user data is in "users" collection
                .document(request.getUserId()) // Fetch document by userId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("username");
                        holder.userName.setText("Người dùng: " + (userName != null ? userName : "Không rõ"));
                    } else {
                        holder.userName.setText("Người dùng: Không xác định");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.userName.setText("Người dùng: Không xác định");
                });

        // Load room image using Glide
        if (request.getRoomImageUrls() != null && !request.getRoomImageUrls().isEmpty()) {
            String imageUrl = request.getRoomImageUrls().get(0);
            Glide.with(holder.roomImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.error_image)
                    .into(holder.roomImage);
        } else {
            holder.roomImage.setImageResource(R.drawable.placeholder_img);
        }
    }

    public void updateData(List<DepositRequest> newRequests) {
        this.depositRequestsList.clear();
        this.depositRequestsList.addAll(newRequests);
        notifyDataSetChanged();
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
