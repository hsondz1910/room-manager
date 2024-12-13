package com.lastterm.finalexam.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.room.RoomDetailActivity;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {
    private Context context;
    private List<Room> favoriteRooms;
    private RoomRepository repository;

    public FavoritesAdapter(Context context, List<Room> favoriteRooms) {
        repository = new RoomRepository();
        this.context = context;
        this.favoriteRooms = favoriteRooms;
    }

    @NonNull
    @Override
    public FavoritesAdapter.FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoritesAdapter.FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesAdapter.FavoritesViewHolder holder, int position) {
        Room room = favoriteRooms.get(position);
        holder.text_favorite_title.setText(room.getTitle());
        holder.text_favorite_description.setText(room.getDescription());
        holder.itemView.setOnClickListener(view -> {showPopupMenu(view, position);});
        Log.d("Error", " image :" + room.getImgUrls().isEmpty());
        if(!room.getImgUrls().isEmpty()){
            try {
                Glide.with(context).load(room.getImgUrls().get(0)).into(holder.image_favorite);
                Log.d("Error", "Error loading image :" + Glide.with(context).load(room.getImgUrls().get(0)));
            } catch (Exception e) {
                Log.d("Error", "Error loading image :" + e.getMessage());
            }

        }


    }

    @Override
    public int getItemCount() {
        return favoriteRooms.size();
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder {
        TextView text_favorite_title, text_favorite_description;
        ImageView image_favorite;
        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            text_favorite_title = itemView.findViewById(R.id.text_favorite_title);
            text_favorite_description = itemView.findViewById(R.id.text_favorite_description);
            image_favorite = itemView.findViewById(R.id.image_favorite);
        }
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_menu_favorite);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                // Xử lý xóa
                showDialog(position);
                Toast.makeText(context, "Đã xóa phòng " + favoriteRooms.get(position).getTitle() + "khỏi mục yêu thích", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (item.getItemId() == R.id.action_detail) {
                Fragment fragment = new RoomDetailActivity(favoriteRooms.get(position));
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
            return false;
        });

        popupMenu.show();
    }
    private void showDialog(int position) {
        String roomTile = favoriteRooms.get(position).getTitle();
        String roomId = favoriteRooms.get(position).getId();
        new AlertDialog.Builder(context)
                .setTitle("Confirm Deletion")
                .setMessage("Bạn có muốn xóa phòng " + roomTile + " ra khởi mục yêu thích không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    repository.removeFromFavorites(roomId, auth.getCurrentUser().getUid(), null, null);
                    favoriteRooms.remove(position);
                    notifyItemRemoved(position);
                })
                .setNegativeButton("Không", null)
                .create().show();
    }
}
