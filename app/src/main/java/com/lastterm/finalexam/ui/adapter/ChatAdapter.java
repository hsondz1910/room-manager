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
import com.lastterm.finalexam.data.entities.MessageClass;
import com.lastterm.finalexam.data.repositories.RoomRepository;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter{
    List<MessageClass> messages ;
    Context context;
    int item_Send = 0;


    public ChatAdapter(List<MessageClass> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == item_Send){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new senderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recei, parent, false);
            return new receiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageClass msg = messages.get(position);
        RoomRepository repository = new RoomRepository();

        if(msg.getsenderID() == "support"){
            if(holder.getClass() == senderViewHolder.class){
                senderViewHolder viewHolder = (senderViewHolder) holder;
                repository.getNameByUserID(msg.getsenderID(), (name) -> {
                    viewHolder.name.setText(name);
                    if (msg.getMessage() == "") viewHolder.msg.setVisibility(View.GONE);
                    viewHolder.msg.setText(msg.getMessage());
                    viewHolder.date.setText(msg.getDate());
                    if(msg.getImg() != null && msg.getImg() != ""){
                        Glide.with(context).load(msg.getImg()).into(viewHolder.img);
                        viewHolder.img.setVisibility(View.VISIBLE);
                    }
                });
            }
            else {
                receiverViewHolder viewHolder = (receiverViewHolder) holder;
                repository.getNameByUserID(msg.getsenderID(), (name) -> {
                    viewHolder.name.setText(name);
                    if (msg.getMessage() == "") viewHolder.msg.setVisibility(View.GONE);
                    viewHolder.msg.setText(msg.getMessage());
                    viewHolder.date.setText(msg.getDate());
                    if(msg.getImg() != null && msg.getImg() != ""){
                        Glide.with(context).load(msg.getImg()).into(viewHolder.img);
                        viewHolder.img.setVisibility(View.VISIBLE);
                    }
                });
            }
        }else {
            if(holder.getClass() == senderViewHolder.class){
                senderViewHolder viewHolder = (senderViewHolder) holder;
                repository.getNameByUserID(msg.getsenderID(), (name) -> {
                    viewHolder.name.setText(name);
                    if (msg.getMessage() == "") viewHolder.msg.setVisibility(View.GONE);
                    viewHolder.msg.setText(msg.getMessage());
                    viewHolder.date.setText(msg.getDate());
                    if(msg.getImg() != null && msg.getImg() != ""){
                        Glide.with(context).load(msg.getImg()).into(viewHolder.img);
                        viewHolder.img.setVisibility(View.VISIBLE);
                    }
                });

            }
            else {
                receiverViewHolder viewHolder = (receiverViewHolder) holder;
                repository.getNameByUserID(msg.getsenderID(), (name) -> {
                    viewHolder.name.setText(name);
                    if (msg.getMessage() == "") viewHolder.msg.setVisibility(View.GONE);
                    viewHolder.msg.setText(msg.getMessage());
                    viewHolder.date.setText(msg.getDate());
                    if(msg.getImg() != null && msg.getImg() != ""){
                        Glide.with(context).load(msg.getImg()).into(viewHolder.img);
                        viewHolder.img.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

    }



    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        RoomRepository roomRepository = new RoomRepository();

        if(roomRepository.isCurrentUser(messages.get(position).getsenderID())){

            return 0;
        }
        else{

            return 1;
        }

    }

    class senderViewHolder extends RecyclerView.ViewHolder{
        TextView name, msg, date;
        ImageView img;
        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_mes_sent);
            msg = itemView.findViewById(R.id.sent_text);
            date = itemView.findViewById(R.id.date_mes_sent);
            img = itemView.findViewById(R.id.sent_image);
        }
    }
    class receiverViewHolder extends RecyclerView.ViewHolder{
        TextView name, msg, date;
        ImageView img;
        public receiverViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nam_mes_rec);
            msg = itemView.findViewById(R.id.rec_text);
            date = itemView.findViewById(R.id.date_mes_rec);
            img = itemView.findViewById(R.id.rec_image);
        }
    }
}
