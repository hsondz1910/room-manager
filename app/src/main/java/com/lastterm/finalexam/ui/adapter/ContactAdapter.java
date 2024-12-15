package com.lastterm.finalexam.ui.adapter;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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
    RoomRepository repository;

    public ContactAdapter(Context context, String role, List<ChatRoom> chatRooms) {
        this.role = role;
        this.context = context;
        this.chatRooms = chatRooms;
        repository = new RoomRepository();
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
        repository.getRoomById(chatRoom.getRoomId(), room -> {
            if(room!= null){
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
            }else {
                holder.text_room_name.setText("Phòng không còn tồn tại");
                holder.text_room_owwner.setText("");
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
        });
        holder.itemView.setOnClickListener(v -> {
            Fragment fragment = new ChatRoomFragment(chatRoom.getRoomId(),chatRoom.getUsers().get(0), chatRoom.getUsers().get(1));
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        });

        holder.btn_img_Del.setOnLongClickListener(e -> {
            showDialog(position, chatRoom.getRoomId());
            return true;
        });
        final int[] check = {0};

        repository.listenToMessages(chatRoom.getId(), (newMessages) -> {
            if(!newMessages.isEmpty() && check[0] == 1){
                chatRoom.setMessages(newMessages);
                holder.text_room_name.setTextColor(ContextCompat.getColor(context, R.color.sender));
            }
            if(check[0] != 1) check[0]++;
        }, (e) -> {});

    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text_room_owwner, text_room_name;
        ImageButton btn_img_Del;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_contact);
            text_room_owwner = itemView.findViewById(R.id.text_contact_room_owwner);
            text_room_name = itemView.findViewById(R.id.text_contact_room_name);
            btn_img_Del = itemView.findViewById(R.id.img_btn_del_contact);
        }

    }

    private void showDialog(int position,String id) {

        new AlertDialog.Builder(context)
                .setTitle("Confirm Deletion")
                .setMessage("Bạn có muốn xóa cuộc hội thoại này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    repository.removeChatRoom(id, chatRoom -> {
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    }, e -> {});
                    chatRooms.remove(position);
                    notifyItemRemoved(position);
                })
                .setNegativeButton("Không", null)
                .create().show();
    }


}