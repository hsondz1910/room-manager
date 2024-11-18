package com.lastterm.finalexam.adapter.owner;

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
import com.lastterm.finalexam.model.Room;
import com.lastterm.finalexam.R;

import java.util.List;

public class RoomManagementAdapter extends RecyclerView.Adapter<RoomManagementAdapter.RoomViewHolder> {
    private List<Room> roomList;
    private Context context;

    // Constructor to initialize roomList and context
    public RoomManagementAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_room_management for each item
        View view = LayoutInflater.from(context).inflate(R.layout.item_room_management, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomTitle.setText(room.getTitle());
        holder.roomPrice.setText(String.format("%,.2f VND", room.getPrice()));
        holder.roomAddress.setText(room.getAddress());
        holder.itemCheckbox.setChecked(room.isSelected());  // Set checkbox status

        // Set up the popup menu for the menu icon
        holder.menuIcon.setOnClickListener(v -> {
            showPopupMenu(v, room, holder);  // Show popup menu when clicked
        });

        // Handle checkbox check change
        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            room.setSelected(isChecked);  // Update room's selection state
        });

        // Handle long press event for showing popup menu
        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, room, holder);
            return true; // Indicate the event was handled
        });
    }

    // Method to show PopupMenu and handle select/unselect actions
    void showPopupMenu(View view, Room room, RoomViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
        popupMenu.inflate(R.menu.popup_room_management_menu);  // Inflate the menu

        // Get the "select" item from the menu and check room selection state
        MenuItem selectItem = popupMenu.getMenu().findItem(R.id.action_select);

        // Change menu item text based on room's selection state
        if (room.isSelected()) {
            selectItem.setTitle("Unselect");  // If selected, show "Unselect"
        } else {
            selectItem.setTitle("Select");  // If not selected, show "Select"
        }

        // Set click listener for menu items
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_select) {
                // Toggle the selection state of the room
                room.setSelected(!room.isSelected());

                // Update checkbox status based on selection state
                holder.itemCheckbox.setChecked(room.isSelected());

                // Change the menu item text after selection
                if (room.isSelected()) {
                    menuItem.setTitle("Unselect");
                } else {
                    menuItem.setTitle("Select");
                }

                return true;
            } else if (id == R.id.action_edit) {
                // Handle "edit" action
                Toast.makeText(context, "Edit " + room.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_delete) {
                // Handle "delete" action
                Toast.makeText(context, "Delete " + room.getTitle(), Toast.LENGTH_SHORT).show();
                roomList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());  // Remove item from list
                return true;
            }
            return false;
        });

        popupMenu.show();  // Show the popup menu
    }

    @Override
    public int getItemCount() {
        return roomList.size();  // Return the number of items in the list
    }

    // ViewHolder class to bind views
    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView roomTitle, roomPrice, roomAddress;
        ImageView roomImage, menuIcon;
        CheckBox itemCheckbox;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            roomAddress = itemView.findViewById(R.id.roomAddress);
            roomImage = itemView.findViewById(R.id.roomImage);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
        }
    }
}
