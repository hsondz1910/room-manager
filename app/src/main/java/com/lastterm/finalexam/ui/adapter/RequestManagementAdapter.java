package com.lastterm.finalexam.ui.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Contract;
import com.lastterm.finalexam.data.entities.DepositRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RequestManagementAdapter extends RecyclerView.Adapter<RequestManagementAdapter.RequestViewHolder> {
    private List<DepositRequest> depositRequestsList;
    private Context context;

    public RequestManagementAdapter(List<DepositRequest> depositRequestsList) {
        this.depositRequestsList = depositRequestsList;
    }

    public RequestManagementAdapter(List<DepositRequest> depositRequestsList, Context context) {
        this.depositRequestsList = depositRequestsList;
        this.context = context;
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
                        holder.roomTitle.setText("Tên phòng: " + (roomTitle != null ? roomTitle : "Không có sẵn"));
                    } else {
                        holder.roomTitle.setText("Tên phòng: Không có sẵn");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.userName.setText("Tên phòng: Không có sẵn");
                });

        db.collection("users") // Assuming user data is in "users" collection
                .document(request.getUserId()) // Fetch document by userId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("username");
                        holder.userName.setText("Người dùng: " + (userName != null ? userName : "Không xác định"));
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

        holder.createContractButton.setOnClickListener(v -> {
            // Show the dialog to create contract
            showCreateContractDialog(request);
        });
    }

    public void updateData(List<DepositRequest> newRequests) {
        this.depositRequestsList.clear();
        this.depositRequestsList.addAll(newRequests);
        notifyDataSetChanged();
    }

    private void showCreateContractDialog(DepositRequest request) {
        if (context == null) {
            // Fallback if context is null
            Toast.makeText(context, "Context is null!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_contract, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        EditText startDateInput = dialogView.findViewById(R.id.startDateInput);
        EditText endDateInput = dialogView.findViewById(R.id.endDateInput);
        EditText rentAmountInput = dialogView.findViewById(R.id.rentAmountInput);
        EditText contractTermsInput = dialogView.findViewById(R.id.contractTermsInput);
        Button createButton = dialogView.findViewById(R.id.createContractButton);

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        // Setup DatePicker for start date
        startDateInput.setOnClickListener(v -> showDatePickerDialog(startDateInput));

        // Setup DatePicker for end date
        endDateInput.setOnClickListener(v -> showDatePickerDialog(endDateInput));

        createButton.setOnClickListener(v -> {
            String startDate = startDateInput.getText().toString();
            String endDate = endDateInput.getText().toString();
            String rentAmountStr = rentAmountInput.getText().toString();
            String contractTerms = contractTermsInput.getText().toString();

            if (startDate.isEmpty() || endDate.isEmpty() || rentAmountStr.isEmpty() || contractTerms.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            double rentAmount = Double.parseDouble(rentAmountStr);
            // Create the contract object
            Contract contract = new Contract();
            contract.setContractId(UUID.randomUUID().toString()); // generate unique contract ID
            contract.setTenantId(request.getUserId());
            contract.setOwnerId(request.getOwnerId());
            contract.setRoomId(request.getRoomId());
            contract.setStartDate(parseDate(startDate)); // set start date
            contract.setEndDate(parseDate(endDate));   // set end date
            contract.setRentAmount(rentAmount);
            contract.setContractTerms(contractTerms);
            contract.setActive(true); // Set it active

            // Save the contract to Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("contracts").document(contract.getContractId())
                    .set(contract)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Hợp đồng đã được tạo thành công.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi tạo hợp đồng.", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void showDatePickerDialog(EditText editText) {
        // Get the current date to use as the default
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format the date to "dd/MM/yyyy"
            String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            editText.setText(formattedDate);  // Update EditText with the selected date
        }, year, month, day);

        datePickerDialog.show();
    }

    // Method to parse date from String to Date
    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
