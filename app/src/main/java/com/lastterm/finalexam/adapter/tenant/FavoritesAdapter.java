package com.lastterm.finalexam.adapter.tenant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.model.Room;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private List<Room> favoriteRooms;

    public FavoritesAdapter(List<Room> favoriteRooms) {
        this.favoriteRooms = favoriteRooms;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        Room room = favoriteRooms.get(position);
        holder.title.setText(room.getTitle());
        holder.address.setText(room.getAddress());
        holder.price.setText(String.valueOf(room.getPrice()));

        // Lấy URL của hình ảnh và sử dụng Glide để tải ảnh
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            String imageUrl = room.getImages().get(0); // Lấy ảnh đầu tiên trong danh sách
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)  // Tải ảnh từ URL
                    .placeholder(R.drawable.placeholder_img)  // Hình ảnh placeholder trong lúc tải
                    .error(R.drawable.error_image)  // Hình ảnh hiển thị khi có lỗi
                    .into(holder.roomImage);  // Đưa vào ImageView
        }
    }

    @Override
    public int getItemCount() {
        return favoriteRooms.size();
    }

    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, price;
        ImageView roomImage;  // ImageView để hiển thị ảnh

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.roomTitle);
            address = itemView.findViewById(R.id.roomAddress);
            price = itemView.findViewById(R.id.roomPrice);
            roomImage = itemView.findViewById(R.id.roomImage);  // Khai báo ImageView để hiển thị ảnh
        }
    }
}
