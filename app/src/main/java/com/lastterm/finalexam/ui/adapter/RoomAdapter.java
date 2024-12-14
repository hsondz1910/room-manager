package com.lastterm.finalexam.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private final List<Room> roomList;
    private final Context context;

    public RoomAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomTitle.setText(room.getTitle());
        holder.roomPrice.setText(String.format("%,.2f VND", room.getPrice()));
        holder.roomAddress.setText(room.getAddress());

        if (room.isFavorite()) {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
        }

        holder.heartIcon.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            RoomRepository roomRepository = new RoomRepository();

            roomRepository.addToFavorites(room.getId(),auth.getCurrentUser().getUid(), (s) -> {
                if (s) {
                    holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
                    Toast.makeText(context, "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    holder.heartIcon.setImageResource(R.drawable.ic_heart_empty);
                    roomRepository.removeFromFavorites(room.getId(), auth.getCurrentUser().getUid(), (e) -> {}, (e) -> {});
                    Toast.makeText(context, "Đã xóa khỏi mục yêu thích", Toast.LENGTH_SHORT).show();
                }
            }, (e) -> {Log.d("fail: ", e.getMessage());});

        });

        holder.itemView.setOnClickListener(view -> {
            Fragment fragment = new RoomDetailActivity(room);
            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        });

        holder.detailButton.setOnClickListener(view -> {
            Fragment fragment = new RoomDetailActivity(room);

            // Unwrap ContextWrapper to get the Activity
            Activity activity = unwrap(view.getContext());

            if (activity instanceof AppCompatActivity) {
                // If the Activity is an instance of AppCompatActivity, use getSupportFragmentManager
                ((AppCompatActivity) activity).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                // Handle the case where the Activity is not an instance of AppCompatActivity
                Log.e("RoomAdapter", "Activity is not an instance of AppCompatActivity");
                Toast.makeText(context, "Error: Unable to open details", Toast.LENGTH_SHORT).show();
            }
        });

        if(!room.getImgUrls().isEmpty()){
            try {
                Glide.with(context).load(room.getImgUrls().get(0)).into(holder.roomImg);
                Log.d("Error", "Error loading image :" + Glide.with(context).load(room.getImgUrls().get(0)));
            } catch (Exception e) {
                Log.d("Error", "Error loading image :" + e.getMessage());
            }
        }
    }

    private static Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return (Activity) context;
    }


    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView roomTitle, roomPrice, roomAddress;
        ImageView heartIcon, roomImg;
        Button detailButton;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTitle = itemView.findViewById(R.id.roomTitle);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            roomAddress = itemView.findViewById(R.id.roomAddress);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            roomImg = itemView.findViewById(R.id.roomImage);
            detailButton = itemView.findViewById(R.id.detailButton);
        }
    }
}
