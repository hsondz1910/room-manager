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
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.R;

import java.util.List;

public class RoomManagementAdapter extends RecyclerView.Adapter<RoomManagementAdapter.RoomViewHolder> {
    private List<Room> roomList;
    private Context context;
    private FirebaseFirestore db;

    public RoomManagementAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room_management, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        Log.d("RoomManagement", "Room------: " + room.getId());

        // Bind room data to views
        holder.roomTitle.setText(room.getTitle());
        holder.roomPrice.setText(String.format("%,.2f VND", room.getPrice()));
        holder.roomAddress.setText(room.getAddress());
        holder.itemCheckbox.setChecked(room.isSelected());

        // Load room image using Glide
        if (room.getImgUrls() != null && !room.getImgUrls().isEmpty()) {
            String imageUrl = room.getImgUrls().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.error_image)
                    .into(holder.roomImageManagement);
        }

        // Set up the popup menu
        holder.menuIcon.setOnClickListener(v -> showPopupMenu(v, room, holder));

        // Handle checkbox state change
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> room.setSelected(isChecked));

        // Handle long press to show the popup menu
        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, room, holder);
            return true;
        });
    }

    // Display popup menu and handle actions
    private void showPopupMenu(View view, Room room, RoomViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
        popupMenu.inflate(R.menu.popup_room_management_menu);

        // Update "Select" menu item title based on room selection state
        MenuItem selectItem = popupMenu.getMenu().findItem(R.id.action_select);
        selectItem.setTitle(room.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_select) {
                room.setSelected(!room.isSelected());
                holder.itemCheckbox.setChecked(room.isSelected());
                selectItem.setTitle(room.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");
                return true;
            } else if (id == R.id.action_edit) {
                Toast.makeText(context, "Chỉnh sửa " + room.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_delete) {
                Toast.makeText(context, "Xoá bỏ " + room.getTitle(), Toast.LENGTH_SHORT).show();

                // Remove from list and notify adapter
                roomList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                Log.d("RoomManagement", "Room ID to delete: " + room.getId());
                // Delete from Firestore
                db.collection("rooms").document(room.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Phòng đã bị xóa khỏi Firestore", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi khi xóa phòng khỏi Firestore", Toast.LENGTH_SHORT).show();
                        });
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    // ViewHolder class to hold and bind views
    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView roomTitle, roomPrice, roomAddress;
        ImageView roomImageManagement, menuIcon;
        CheckBox itemCheckbox;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            roomAddress = itemView.findViewById(R.id.roomAddress);
            roomImageManagement = itemView.findViewById(R.id.roomImageManagement);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
        }
    }
}
