package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Contract;
import com.lastterm.finalexam.data.repositories.RoomRepository;

import java.util.List;


public class TenantContractAdapter extends RecyclerView.Adapter<TenantContractAdapter.TenantContractViewHolder>{
    Context context;
    List<Contract> contractList;
    private RoomRepository repository = new RoomRepository();

    public TenantContractAdapter(Context context, List<Contract> contractList) {
        this.context = context;
        this.contractList = contractList;
    }

    @NonNull
    @Override
    public TenantContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contract_tenant, parent, false);
        return new TenantContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantContractViewHolder holder, int position) {
        Contract contract = contractList.get(position);
        repository.getRoomById(contract.getRoomId(),(room) -> {
            holder.txtTitle.setText("Phòng: " + room.getTitle());
        });
        repository.getNameByUserID(contract.getOwnerId(),(user) -> {
            holder.txtOwner.setText("Chủ trọ: " + user);
        });
        holder.txtStatus.setText("Trạng thái: " + (contract.isActive() ? "Hoạt động" : "Hết hạn"));

    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }


    public class TenantContractViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtOwner, txtStatus;


        public TenantContractViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.contract_tenant_title);
            txtOwner = itemView.findViewById(R.id.ownerName);
            txtStatus = itemView.findViewById(R.id.contract_tenant_Status);
        }
    }
}
