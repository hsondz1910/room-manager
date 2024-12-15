package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        holder.tvRentAmount.setText("Tiền thuê: " + contract.getRentAmount() + "VNĐ");
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
                Toast.makeText(context, "Chỉnh sửa " + contract.getContractId(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_delete) {
                Toast.makeText(context, "Xóa bỏ " + contract.getContractId(), Toast.LENGTH_SHORT).show();

                // Remove from list and notify adapter
                contractList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
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
                return true;
            }
            return false;
        });

        popupMenu.show();
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
