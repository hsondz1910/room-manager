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
import com.lastterm.finalexam.data.repositories.RequestRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.util.Log;

public class RequestManagementAdapter extends RecyclerView.Adapter<RequestManagementAdapter.RequestViewHolder> {
    private List<DepositRequest> depositRequestsList;
    private Context context;

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
        RequestRepository repository = new RequestRepository();
        DepositRequest request = depositRequestsList.get(position);

        // Fetch room title
        repository.fetchRoomTitle(request.getRoomId(), new RequestRepository.RoomCallback() {
            @Override
            public void onSuccess(String roomTitle) {
                holder.roomTitle.setText(roomTitle != null ? roomTitle : "Tên phòng: Không có sẵn");
            }

            @Override
            public void onError(String errorMessage) {
                holder.roomTitle.setText("Tên phòng: Không có sẵn");
            }
        });

        // Fetch user name
        repository.fetchUserName(request.getUserId(), new RequestRepository.UserCallback() {
            @Override
            public void onSuccess(String userName) {
                holder.userName.setText("Người dùng: " + (userName != null ? userName : "Không xác định"));
            }

            @Override
            public void onError(String errorMessage) {
                holder.userName.setText("Người dùng: Không xác định");
            }
        });

        holder.depositAmount.setText("Số tiền gửi: " + String.format("%,.2f VNĐ", request.getDepositAmount()));
        holder.roomStatus.setText("Trạng thái yêu cầu: " + getStatusInVietnamese(request.getStatus()));

        // Load room image using Glide
        if (request.getRoomImageUrls() != null && !request.getRoomImageUrls().isEmpty()) {
            String imageUrl = request.getRoomImageUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.error_image)
                    .into(holder.roomImage);
        }

        // Update status on reject
        holder.rejectButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                repository.updateRequestStatus(request.getRequestId(), "rejected", new RequestRepository.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Yêu cầu đã bị từ chối.", Toast.LENGTH_SHORT).show();
                        request.setStatus("rejected");
                        notifyItemChanged(currentPosition); // Update UI
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(context, "Lỗi khi từ chối yêu cầu: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Handle create contract button click
        holder.createContractButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                showCreateContractDialog(request);
            }
        });
    }

    private String getStatusInVietnamese(String status) {
        switch (status) {
            case "approved":
                return "Đã duyệt";
            case "rejected":
                return "Bị từ chối";
            case "pending":
                return "Đang chờ";
            case "contract_created":
                return "Hợp đồng đã tạo";
            default:
                return "Chưa xác định";
        }
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
            contract.setRequestId(request.getRequestId());
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
                        db.collection("depositRequests").document(request.getRequestId())
                                .update("status", "contract_created")
                                .addOnSuccessListener(updateVoid -> {
                                    Toast.makeText(context, "Hợp đồng đã được tạo thành công.", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Hợp đồng được tạo nhưng không thể cập nhật yêu cầu.", Toast.LENGTH_SHORT).show();
                                    Log.e("Contract", "Lỗi khi cập nhật yêu cầu gửi tiền: " + e.getMessage(), e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi tạo hợp đồng.", Toast.LENGTH_SHORT).show();
                        Log.e("Contract", "Lỗi khi tạo hợp đồng: " + e.getMessage(), e);
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
        TextView roomTitle, depositAmount, userName, roomStatus;
        Button createContractButton, rejectButton;
        ImageView roomImage;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            depositAmount = itemView.findViewById(R.id.depositAmount);
            userName = itemView.findViewById(R.id.userName);
            roomStatus = itemView.findViewById(R.id.status);
            createContractButton = itemView.findViewById(R.id.createContractButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            roomImage = itemView.findViewById(R.id.roomImageManagement);
        }
    }
}
