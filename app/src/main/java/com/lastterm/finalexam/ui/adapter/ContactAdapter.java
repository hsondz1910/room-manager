package com.lastterm.finalexam.ui.adapter;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.ChatRoom;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.fragments.contact.ChatRoomFragment;
import com.lastterm.finalexam.ui.room.RoomDetailFragment;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{
    Context context;
    String role;
    List<ChatRoom> chatRooms;

    public ContactAdapter(Context context, String role, List<ChatRoom> chatRooms) {
        this.role = role;
        this.context = context;
        this.chatRooms = chatRooms;
    }

    @NonNull
    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        RoomRepository repository = new RoomRepository();
        repository.getRoomById(chatRoom.getRoomId(), room -> {
            holder.text_room_name.setText(room.getTitle());
            if(role.equals("owner")) {
                repository.getNameByUserID(room.getOwnerId(), name -> {
                    holder.text_room_owwner.setText(name);
                });
            }
            else{
                String user = chatRoom.getUsers().get(0);
                if(user.equals(repository.getCurrentUser())){
                    repository.getNameByUserID(chatRoom.getUsers().get(1), name -> {
                        holder.text_room_owwner.setText(name);
                    });
                }else {
                    repository.getNameByUserID(user, name -> {
                        holder.text_room_owwner.setText("Người nhắn: " + name);
                    });
                }
            }

            Glide.with(context).load(room.getImgUrls().get(0)).into(holder.image);
        });
        holder.itemView.setOnClickListener(v -> {
            Fragment fragment = new ChatRoomFragment(chatRoom.getRoomId(),chatRoom.getUsers().get(0), chatRoom.getUsers().get(1));
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text_room_owwner, text_room_name;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_contact);
            text_room_owwner = itemView.findViewById(R.id.text_contact_room_owwner);
            text_room_name = itemView.findViewById(R.id.text_contact_room_name);
        }
    }
}
