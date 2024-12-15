package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
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

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Contract;
import com.lastterm.finalexam.data.entities.Room;

import java.util.List;

public class ContractManagementAdapter extends RecyclerView.Adapter<ContractManagementAdapter.ContractViewHolder> {

    private List<Contract> contractList;
    private Context context;

    public ContractManagementAdapter(List<Contract> contractList, Context context) {
        this.contractList = contractList;
        this.context = context;
    }

    // Display popup menu and handle actions
    private void showPopupMenu(View view, Contract contract, ContractViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
        popupMenu.inflate(R.menu.popup_contract_management_menu);

        // Update "Select" menu item title based on contract selection state
        MenuItem selectItem = popupMenu.getMenu().findItem(R.id.action_select);
        selectItem.setTitle(contract.isSelected() ? "Unselect" : "Select");

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_select) {
                contract.setSelected(!contract.isSelected());
                holder.checkBox.setChecked(contract.isSelected());
                selectItem.setTitle(contract.isSelected() ? "Unselect" : "Select");
                return true;
            } else if (id == R.id.action_delete) {
                Toast.makeText(context, "Deleted Contract: " + contract.getContractId(), Toast.LENGTH_SHORT).show();
                contractList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                return true;
            }
            return false;
        });

        popupMenu.show();
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

        // Bind contract data
        holder.tvContractTitle.setText(contract.getContractId()); // Contract ID or title
        holder.tvTenantName.setText("Tenant: " + contract.getTenantId()); // Tenant Name
        holder.tvRentAmount.setText("Rent: $" + contract.getRentAmount()); // Rent amount
        holder.tvContractStatus.setText(contract.isActive() ? "Active" : "Expired"); // Contract Status

        // Set CheckBox state
        holder.checkBox.setChecked(contract.isSelected());

        // Handle checkbox selection change
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contract.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    public class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView tvContractTitle, tvTenantName, tvRentAmount, tvContractStatus;
        CheckBox checkBox;
        ImageView roomImageManagement, menuIcon;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContractTitle = itemView.findViewById(R.id.contractTitle);
            tvTenantName = itemView.findViewById(R.id.tenantName);
            tvRentAmount = itemView.findViewById(R.id.rentAmount);
            tvContractStatus = itemView.findViewById(R.id.contractStatus);
            checkBox = itemView.findViewById(R.id.item_checkbox);
            roomImageManagement = itemView.findViewById(R.id.roomImageManagement);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}
