package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.lastterm.finalexam.data.entities.Room;

import java.util.List;

public class ContractManagementAdapter extends RecyclerView.Adapter<ContractManagementAdapter.ContractViewHolder> {

    private List<Contract> contractList;
    private Context context;
    private FirebaseFirestore db;

    public ContractManagementAdapter(List<Contract> contractList, Context context) {
        this.contractList = contractList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contract_management, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        Contract contract = contractList.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        holder.tvContractTitle.setText("Hợp đồng cho thuê phòng trọ");

        // Bind contract data
        db.collection("users")
                .document(contract.getTenantId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String tenantName = documentSnapshot.getString("username");
                        holder.tvTenantName.setText("Người thuê nhà: " + (tenantName != null ? tenantName : "Không có sẵn"));
                    } else {
                        holder.tvTenantName.setText("Người thuê nhà: Không có sẵn");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.tvTenantName.setText("Người thuê nhà: Không có sẵn");
                });

        holder.tvRentAmount.setText(String.format("Tiền thuê: %, .2f VNĐ", contract.getRentAmount()));
        holder.tvContractStatus.setText("Trạng thái: " + (contract.isActive() ? "Hoạt động" : "Hết hạn"));

        // Set up the popup menu
        holder.menuIcon.setOnClickListener(v -> showPopupMenu(v, contract, holder));

        // Handle checkbox state change
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> contract.setSelected(isChecked));

        // Handle long press to show the popup menu
        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, contract, holder);
            return true;
        });
    }

    // Display popup menu and handle actions
    private void showPopupMenu(View view, Contract contract, ContractManagementAdapter.ContractViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
        popupMenu.inflate(R.menu.popup_contract_management_menu);

        // Update "Select" menu item title based on room selection state
        MenuItem selectItem = popupMenu.getMenu().findItem(R.id.action_select);
        selectItem.setTitle(contract.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_select) {
                contract.setSelected(!contract.isSelected());
                holder.checkBox.setChecked(contract.isSelected());
                selectItem.setTitle(contract.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");
                return true;
            } else if (id == R.id.action_edit) {
                showEditDialog(contract, holder.getAdapterPosition());
                return true;
            } else if (id == R.id.action_delete) {
                deleteContract(contract, holder.getAdapterPosition());
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Method to show dialog for editing contract
    private void showEditDialog(Contract contract, int position) {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_contract, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        // Initialize dialog views
        EditText rentAmountInput = dialogView.findViewById(R.id.editRentAmount);
        CheckBox activeCheckbox = dialogView.findViewById(R.id.editActiveCheckbox);
        Button saveButton = dialogView.findViewById(R.id.saveEditButton);

        // Populate fields with current contract data
        rentAmountInput.setText(String.valueOf(contract.getRentAmount()));
        activeCheckbox.setChecked(contract.isActive());

        // Set up dialog
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String rentAmountStr = rentAmountInput.getText().toString();

            if (rentAmountStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập số tiền thuê.", Toast.LENGTH_SHORT).show();
                return;
            }

            double newRentAmount = Double.parseDouble(rentAmountStr);
            boolean newActiveStatus = activeCheckbox.isChecked();

            // Ensure db is initialized
            if (db == null) {
                db = FirebaseFirestore.getInstance();
            }

            // Update contract object
            contract.setRentAmount(newRentAmount);
            contract.setActive(newActiveStatus);

            // Update Firestore
            db.collection("contracts").document(contract.getContractId())
                    .update("rentAmount", newRentAmount, "active", newActiveStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Hợp đồng đã được cập nhật.", Toast.LENGTH_SHORT).show();
                        notifyItemChanged(position); // Refresh the updated item
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi cập nhật hợp đồng.", Toast.LENGTH_SHORT).show();
                        Log.e("ContractManagement", "Error updating contract: " + e.getMessage(), e);
                    });
        });

        dialog.show();
    }

    // Method to delete contract
    private void deleteContract(Contract contract, int position) {
        Toast.makeText(context, "Xóa bỏ " + contract.getContractId(), Toast.LENGTH_SHORT).show();

        // Remove from list and notify adapter
        contractList.remove(position);
        notifyItemRemoved(position);
        Log.d("ContractManagement", "Contract ID to delete: " + contract.getContractId());

        // Delete from Firestore
        db.collection("contracts").document(contract.getContractId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Hợp đồng đã bị xóa khỏi Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa hợp đồng khỏi Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView tvContractTitle, tvTenantName, tvRentAmount, tvContractStatus;
        CheckBox checkBox;
        ImageView menuIcon;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContractTitle = itemView.findViewById(R.id.contractTitle);
            tvTenantName = itemView.findViewById(R.id.tenantName);
            tvRentAmount = itemView.findViewById(R.id.rentAmount);
            tvContractStatus = itemView.findViewById(R.id.contractStatus);
            checkBox = itemView.findViewById(R.id.item_checkbox);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}
