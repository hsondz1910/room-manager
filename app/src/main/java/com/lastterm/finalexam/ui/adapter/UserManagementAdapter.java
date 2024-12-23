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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.User;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;

    private FirebaseFirestore db;

    public UserManagementAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserManagementAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserManagementAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.username.setText(user.getUsername() != null ? "Username: " + user.getUsername() : "Unknown");
        holder.userEmail.setText(user.getEmail() != null ? "Email: " + user.getEmail() : "Unknown");
        holder.role.setText(user.getRole() != null ? "Vai trò: " + user.getRole() : "Unknown");

        // Log.d("BindDebug", "Binding User: " + user.getUsername() + ", isActive: " + user.isActive());

        /*
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("FirestoreDebug", "Document data: " + document.getData());
                }
            } else {
                Log.e("FirestoreError", "Error getting documents: ", task.getException());
            }
        });
        */

        holder.status.setText("Trạng thái: " + ((user.isActive() == true) ? "Hoạt động" : "Không hoạt động"));

        if (user.getUrlAvatar() != null && !user.getUrlAvatar().isEmpty()) {
            String imgUrl = user.getUrlAvatar();
            Glide.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.error_image)
                    .into(holder.userImageManagement);
        }

        holder.menuIcon.setOnClickListener(v -> showPopupMenu(v, user, holder));

        holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> user.setSelected(isChecked));

        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, user, holder);
            return true;
        });
    }

    private void showPopupMenu(View v, User user, UserViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.menuIcon);
        popupMenu.inflate(R.menu.popup_user_management_menu);

        MenuItem selectedItem = popupMenu.getMenu().findItem(R.id.action_select);
        selectedItem.setTitle(user.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.action_select) {
                user.setSelected(!user.isSelected());
                holder.itemCheckbox.setChecked(user.isSelected());
                selectedItem.setTitle(user.isSelected() ? "Bỏ lựa chọn" : "Lựa chọn");
                return true;
            } else if (id == R.id.action_edit) {
                Toast.makeText(context, "Chỉnh sửa " + user.getUsername(), Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            } else if (id == R.id.action_delete) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Xác nhận xoá")
                        .setMessage("Bạn có chắc chắn muốn xoá quyền đăng nhập của tài khoản này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            db.collection("users").document(user.getUserId())
                                    .update("isActive", false)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Đã xoá quyền đăng nhập của " + user.getUsername(), Toast.LENGTH_SHORT).show();
                                        Log.d("UserManagement", "User " + user.getUsername() + " set inactive");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Không thể xoá quyền đăng nhập của " + user.getUsername(), Toast.LENGTH_SHORT).show();
                                        Log.e("UserManagement", "Failed to update user " + user.getUsername(), e);
                                    });
                        })
                        .setNegativeButton("Không", null)
                        .show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView username, userEmail, role, status;
        ImageView userImageManagement, menuIcon;
        CheckBox itemCheckbox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.txtUsername);
            userEmail = itemView.findViewById(R.id.userEmail);
            role = itemView.findViewById(R.id.userRole);
            status = itemView.findViewById(R.id.userStatus);
            userImageManagement = itemView.findViewById(R.id.userImageManagement);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
        }
    }
}
